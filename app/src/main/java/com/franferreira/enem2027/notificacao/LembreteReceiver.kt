package com.franferreira.enem2027.notificacao

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.franferreira.enem2027.data.repo.Repositorio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Recebe o alarme do [Agendador] e mostra o aviso, se houver o que avisar.
 *
 * Três tipos chegam aqui: o lembrete do período marcado na Agenda, o fim de
 * um bloco de foco (pomodoro) e o resumo do domingo.
 */
class LembreteReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_PERIODO = "periodo"
        const val EXTRA_TENTATIVA = "tentativa"
        const val EXTRA_FOCO = "foco"
        const val EXTRA_RESUMO = "resumo"

        /** Quantas vezes o lembrete insiste (1 = só o primeiro aviso). */
        private const val MAX_TENTATIVAS = 2

        /** Quanto tempo depois o lembrete volta, se ninguém abriu o app. */
        private const val INTERVALO_INSISTENCIA_MS = 60L * 60 * 1000
    }

    override fun onReceive(context: Context, intent: Intent) {
        val periodo = intent.getStringExtra(EXTRA_PERIODO)
        val tentativa = intent.getIntExtra(EXTRA_TENTATIVA, 1)
        val foco = intent.getBooleanExtra(EXTRA_FOCO, false)
        val resumo = intent.getBooleanExtra(EXTRA_RESUMO, false)
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = Repositorio(context)
                when {
                    foco -> Notificador.avisarFoco(context, 25)
                    resumo -> {
                        val questoes = runCatching { repo.revisadasNaSemana() }.getOrDefault(0)
                        val gam = runCatching { repo.gamificacao() }.getOrNull()
                        // Dias com estudo na semana: aproxima pelo streak quando
                        // ele é menor que 7 (streak conta dias seguidos).
                        val dias = (gam?.streak ?: 0).coerceIn(0, 7)
                        Notificador.avisarResumo(context, questoes, dias, gam?.streak ?: 0)
                    }
                    else -> {
                        val pendentes = repo.pendentes()
                        if (pendentes > 0) {
                            Notificador.avisar(context, pendentes, periodo)
                            // Insiste uma vez, uma hora depois, se ainda houver
                            // o que revisar. Duas no máximo: não vira spam.
                            if (tentativa < MAX_TENTATIVAS) {
                                insistir(context, periodo, tentativa + 1)
                            }
                        }
                    }
                }
            } finally {
                if (!foco && !resumo) {
                    // Reagenda os próximos: o alarme é de disparo único.
                    Agendador.agendar(context)
                }
                pending.finish()
            }
        }
    }

    private fun insistir(context: Context, periodo: String?, tentativa: Int) {
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, LembreteReceiver::class.java).apply {
            putExtra(EXTRA_PERIODO, periodo)
            putExtra(EXTRA_TENTATIVA, tentativa)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            9000 + tentativa,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val quando = System.currentTimeMillis() + INTERVALO_INSISTENCIA_MS
        runCatching {
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, quando, pending)
        }
    }
}

/**
 * Reagenda os lembretes depois de o aparelho reiniciar.
 *
 * O `MY_PACKAGE_REPLACED` importa tanto quanto o boot: instalar uma versão nova
 * cancela os alarmes do app, e o manifesto já declarava o filtro sem tratar.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val relevante = intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        if (!relevante) return
        // goAsync: `agendar` lê o Room numa corrotina; sem segurar o processo,
        // o sistema podia matá-lo antes de os alarmes serem recriados.
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Agendador.agendar(context)
            } finally {
                pending.finish()
            }
        }
    }
}