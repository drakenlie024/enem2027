package com.franferreira.enem2027.notificacao

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.franferreira.enem2027.MainActivity
import com.franferreira.enem2027.R

/**
 * Notificação local de lembrete de estudo.
 *
 * A ideia: o app pergunta "quando você pode?" na Agenda e usa a resposta
 * para chamar a atenção no horário certo — só quando há questão vencendo.
 *
 * É notificação LOCAL de propósito: não precisa de servidor de push, não
 * gasta Firebase, e funciona sem internet. Se o usuário não estiver
 * conectado, não tem o que sincronizar de qualquer forma.
 */
object Notificador {

    private const val CANAL_ID = "estudo"
    private const val ID_LEMBRETE = 1001

    fun criarCanal(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val canal = NotificationChannel(
            CANAL_ID,
            "Lembrete de estudo",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Avisa quando há questões vencendo para revisar"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(canal)
    }

    /** Mostra o aviso. `pendentes` é o número de questões na fila do SRS. */
    fun avisar(context: Context, pendentes: Int, tema: String? = null) {
        if (pendentes <= 0) return

        criarCanal(context)

        val texto = if (tema != null && tema.isNotBlank()) {
            "$pendentes questões te esperando em $tema"
        } else {
            "$pendentes questões te esperando"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notificacao = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacao)
            .setContentTitle("Hora de revisar")
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(ID_LEMBRETE, notificacao)
        } catch (e: SecurityException) {
            // Falta a permissão de POST_NOTIFICATIONS (Android 13+). Sem alarde.
        }
    }
}
