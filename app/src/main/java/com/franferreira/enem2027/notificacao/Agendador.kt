package com.franferreira.enem2027.notificacao

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationManagerCompat
import com.franferreira.enem2027.data.repo.Repositorio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Agenda o lembrete de estudo.
 *
 * O alarm dispara, o `BroadcastReceiver` acorda e pergunta ao repositório
 * quantas questões estão vencendo. Só mostra notificação se houver
 * alguma — senão seria um aviso para tocar numa tela vazia.
 *
 * O horário vem da Agenda do usuário: o primeiro período que ele marcou
 * para o dia de hoje.
 */
object Agendador {

    private const val REQUEST_CODE = 4200
    private const val EXTRA_PERIODO = "periodo"

    /** Agenda o próximo lembrete, com base na agenda que o usuário montou. */
    fun agendar(context: Context) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val repo = Repositorio(context)
            val agenda = repo.agenda()
            if (agenda.isEmpty()) {
                cancelar(context)
                return@launch
            }

            val agora = Calendar.getInstance()
            val hoje = agora.get(Calendar.DAY_OF_WEEK) - 2  # Calendar: domingo=1
            val weekday = if (hoje < 0) 6 else hoje

            val periodo = agenda
                .firstOrNull { it.first == weekday }
                ?.second
                ?: agenda.first().second

            val hora = when (periodo) {
                "manha" -> 8
                "tarde" -> 15
                else -> 20
            }

            val alvo = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hora)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val intent = Intent(context, LembreteReceiver::class.java).apply {
                putExtra(EXTRA_PERIODO, periodo)
            }
            val pending = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val alarm = context.getSystemService(AlarmManager::class.java)
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarm,
                alvo.timeInMillis,
                pending,
            )
        }
    }

    fun cancelar(context: Context) {
        val intent = Intent(context, LembreteReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val alarm = context.getSystemService(AlarmManager::class.java)
        alarm?.cancel(pending)
    }

    fun temPermissao(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()
}
