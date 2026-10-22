package com.franferreira.enem2027.notificacao

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.franferreira.enem2027.data.repo.Repositorio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Agenda os lembretes de estudo.
 *
 * O alarme dispara, o [LembreteReceiver] acorda e pergunta ao repositório
 * quantas questões estão vencendo. Só mostra notificação se houver alguma —
 * senão seria um aviso para tocar numa tela vazia.
 *
 * Três coisas que a versão anterior fazia errado:
 *
 * 1. Um único alarme, com um único REQUEST_CODE. Quem marcava manhã e tarde
 *    recebia aviso só da manhã. Agora cada período marcado tem o seu alarme.
 * 2. O alvo era o período do dia corrente, ou o primeiro da lista se hoje não
 *    tivesse nada — o que agendava o lembrete no horário de outro dia. Agora
 *    procuramos o próximo período que realmente existe.
 * 3. O alarme era de disparo único e ninguém reagendava. Agora agendamos
 *    vários dias à frente, e o receiver reagenda ao disparar.
 */
object Agendador {

    /** Hora de cada período, no fuso do aparelho. */
    private val HORAS = mapOf(
        "manha" to 8,
        "tarde" to 15,
        "noite" to 20,
    )

    /** Quantos dias à frente agendamos. */
    private const val DIAS_A_FRENTE = 7

    /**
     * O REQUEST_CODE precisa ser diferente por alarme, senão um `PendingIntent`
     * substitui o outro. Base 100 por dia + índice do período.
     */
    private fun requestCode(diasAFrente: Int, indice: Int) = 1000 + diasAFrente * 100 + indice

    /** Agenda os lembretes, com base na agenda que o usuário montou. */
    fun agendar(context: Context) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val repo = Repositorio(context)
            val agenda = repo.agenda()
            if (agenda.isEmpty()) {
                cancelar(context)
                return@launch
            }

            val alarm = context.getSystemService(AlarmManager::class.java) ?: return@launch
            val agora = System.currentTimeMillis()
            val periodos = agenda
                .map { it.period to HORAS[it.period] }
                .filter { it.second != null }
                .distinctBy { it.first }

            // Limpa os alarmes dos próximos dias antes de recriar (a agenda
            // pode ter mudado e o alarme antigo aponta para um período que
            // não existe mais).
            for (dia in 0 until DIAS_A_FRENTE) {
                for (indice in periodos.indices) {
                    alarm.cancel(pendingIntent(context, dia, indice))
                }
            }

            for (dia in 0 until DIAS_A_FRENTE) {
                val alvo = proximoAlarme(agenda, dia, agora) ?: continue
                val periodo = alvo.second
                val indice = periodos.indexOfFirst { it.first == periodo }
                if (indice < 0) continue

                val intent = Intent(context, LembreteReceiver::class.java).apply {
                    putExtra(LembreteReceiver.EXTRA_PERIODO, periodo)
                }
                setAlarme(alarm, context, alvo.first.timeInMillis, intent, dia, indice)
            }
        }
    }

    /**
     * O próximo alarme dentro da janela `diasAFrente`.
     *
     * Devolve (calendario, periodo). Percorre os períodos daquele dia em
     * ordem de hora e pega o primeiro que ainda vai acontecer.
     */
    private fun proximoAlarme(
        agenda: List<com.franferreira.enem2027.data.local.AgendaEntity>,
        diasAFrente: Int,
        agoraMillis: Long,
    ): Pair<Calendar, String>? {
        val base = Calendar.getInstance().apply {
            timeInMillis = agoraMillis
            add(Calendar.DAY_OF_YEAR, diasAFrente)
        }
        // Calendar: domingo = 1, segunda = 2 ... sábado = 7
        val weekday = (base.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7

        val doDia = agenda.filter { it.weekday == weekday }
        if (doDia.isEmpty()) return null

        val candidatos = doDia.mapNotNull { item ->
            val hora = HORAS[item.period] ?: return@mapNotNull null
            val cal = Calendar.getInstance().apply {
                timeInMillis = agoraMillis
                add(Calendar.DAY_OF_YEAR, diasAFrente)
                set(Calendar.HOUR_OF_DAY, hora)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (cal.timeInMillis > agoraMillis) cal to item.period else null
        }
        return candidatos.minByOrNull { it.first.timeInMillis }
    }

    private fun pendingIntent(
        context: Context,
        diasAFrente: Int,
        indice: Int,
    ): PendingIntent {
        val intent = Intent(context, LembreteReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            requestCode(diasAFrente, indice),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun setAlarme(
        alarm: AlarmManager,
        context: Context,
        quando: Long,
        intent: Intent,
        diasAFrente: Int,
        indice: Int,
    ) {
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode(diasAFrente, indice),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, quando, pending)
            } else {
                @Suppress("DEPRECATION")
                alarm.setExact(AlarmManager.RTC_WAKEUP, quando, pending)
            }
        } catch (seguranca: SecurityException) {
            // Sem a permissão de alarme exato (o usuário pode negar no
            // Android 12+). O alarme impreciso acorda na mesma janela — já
            // não é exato ao minuto, mas avisa.
            try {
                alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, quando, pending)
            } catch (falha: Exception) {
                Log.w("Agendador", "não consegui agendar o lembrete: ${falha.message}")
            }
        } catch (falha: Exception) {
            Log.w("Agendador", "não consegui agendar o lembrete: ${falha.message}")
        }
    }

    fun cancelar(context: Context) {
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        for (dia in 0 until DIAS_A_FRENTE) {
            for (indice in 0 until HORAS.size) {
                alarm.cancel(pendingIntent(context, dia, indice))
            }
        }
    }
}
