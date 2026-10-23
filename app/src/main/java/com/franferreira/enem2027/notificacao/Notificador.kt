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

    /**
     * True se o aviso realmente vai aparecer na tela.
     *
     * No Android 13+ sem a permissão de POST_NOTIFICATIONS o `notify` é
     * descartado em silêncio — não lança exceção, não avisa ninguém. O try/
     * catch do [avisar] não salva nada nesse caso; é esta checagem que diz à
     * tela da Agenda que falta pedir a permissão.
     */
    fun podeNotificar(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concedida = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!concedida) return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /** Mostra o aviso. `pendentes` é o número de questões na fila do SRS. */
    fun avisar(context: Context, pendentes: Int, periodo: String? = null) {
        if (pendentes <= 0) return
        if (Silencio.agoraEstaSilencioso(context)) return

        criarCanal(context)

        val quando = quandoNoDia(periodo)
        val texto = if (quando != null) {
            "$pendentes questões te esperando $quando"
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

    /** Fim de um bloco de foco (pomodoro). */
    fun avisarFoco(context: Context, minutos: Int) {
        criarCanal(context)
        notificar(
            context,
            titulo = "Fim do foco",
            texto = "Você focou $minutos minutos. Levanta, bebe água, cinco de pausa.",
            id = 1002,
        )
    }

    /** Resumo do domingo: o que a semana rendeu. */
    fun avisarResumo(context: Context, questoes: Int, dias: Int, streak: Int) {
        if (Silencio.agoraEstaSilencioso(context)) return
        criarCanal(context)
        val texto = buildString {
            append("$questoes questões e $dias dia(s) de estudo nesta semana.")
            if (streak > 1) append(" Sequência de $streak dias.")
        }
        notificar(context, titulo = "Sua semana", texto = texto, id = 1003)
    }

    private fun notificar(context: Context, titulo: String, texto: String, id: Int) {
        if (!podeNotificar(context)) return
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
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, notificacao)
        } catch (e: SecurityException) {
            // sem permissão: o sistema descarta em silêncio
        }
    }

    /** "de manhã", "à tarde", "à noite" — o período como se fala. */
    private fun quandoNoDia(periodo: String?): String? = when (periodo?.trim()?.lowercase()) {
        null, "" -> null
        "manha", "manhã", "morning" -> "de manhã"
        "tarde" -> "à tarde"
        "noite" -> "à noite"
        else -> null
    }
}
