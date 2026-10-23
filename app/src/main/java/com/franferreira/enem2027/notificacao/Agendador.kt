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
            agendarResumoSemanal(context)
            val repo = Repositorio(context)
            val agenda = repo.agenda()
            if (agenda.isEmpty()) {
                cancelar(context)
                return@launch
            }

            val alarm = context.getSystemService(AlarmManager::class.java) ?: return@launch
            val agora = System.currentTimeMillis()
            // Índice FIXO por período (0..2): é o que entra no REQUEST_CODE.
            // Usar a posição na lista atual fazia o cancelamento errar o alvo
            // quando a agenda encolhia, e sobrava alarme órfão tocando.
            val indices = HORAS.keys.withIndex().associate { (i, periodo) -> periodo to i }

            // Limpa a faixa inteira antes de recriar: a agenda pode ter mudado
            // e o alarme antigo aponta para um período que não existe mais.
            for (dia in 0 until DIAS_A_FRENTE) {
                for (indice in 0 until HORAS.size) {
                    alarm.cancel(pendingIntent(context, dia, indice))
                }
            }

            // Um alarme por (dia, período). Antes nascia um por dia — o mais
            // cedo —, então quem marcava manhã E tarde só era avisado de manhã,
            // ao contrário do que o comentário do topo prometia.
            for (dia in 0 until DIAS_A_FRENTE) {
                for (item in agenda) {
                    if (item.weekday != weekdayDe(dia, agora)) continue
                    val hora = HORAS[item.period] ?: continue
                    val quando = Calendar.getInstance().apply {
                        timeInMillis = agora
                        add(Calendar.DAY_OF_YEAR, dia)
                        set(Calendar.HOUR_OF_DAY, hora)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    // O horário de hoje já passou: não agenda no passado.
                    if (quando <= agora) continue

                    val indice = indices[item.period] ?: continue
                    val intent = Intent(context, LembreteReceiver::class.java).apply {
                        putExtra(LembreteReceiver.EXTRA_PERIODO, item.period)
                    }
                    setAlarme(alarm, context, quando, intent, dia, indice)
                }
            }
        }
    }

    /** Dia da semana (0 = domingo) de hoje + [diasAFrente], no fuso do aparelho. */
    private fun weekdayDe(diasAFrente: Int, agoraMillis: Long): Int {
        val base = Calendar.getInstance().apply {
            timeInMillis = agoraMillis
            add(Calendar.DAY_OF_YEAR, diasAFrente)
        }
        // Calendar: domingo = 1, segunda = 2 ... sábado = 7
        return (base.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7
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

    /** Alarme do fim de um bloco de foco (pomodoro). */
    fun agendarFoco(context: Context, minutos: Int) {
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, LembreteReceiver::class.java).apply {
            putExtra(LembreteReceiver.EXTRA_FOCO, true)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            9600,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val quando = System.currentTimeMillis() + minutos * 60_000L
        runCatching {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, quando, pending)
        }.onFailure {
            runCatching { alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, quando, pending) }
        }
    }

    fun cancelarFoco(context: Context) {
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, LembreteReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            9600,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarm.cancel(pending)
    }

    /**
     * Resumo do domingo, 20h. Semanal e repetido: o próprio sistema reagenda,
     * então não depende de o app abrir.
     */
    fun agendarResumoSemanal(context: Context) {
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, LembreteReceiver::class.java).apply {
            putExtra(LembreteReceiver.EXTRA_RESUMO, true)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            9700,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarm.cancel(pending)
        val quando = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // se o domingo das 20h já passou, vai para a semana que vem
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.WEEK_OF_YEAR, 1)
        }.timeInMillis
        runCatching {
            alarm.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                quando,
                AlarmManager.INTERVAL_DAY * 7,
                pending,
            )
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
