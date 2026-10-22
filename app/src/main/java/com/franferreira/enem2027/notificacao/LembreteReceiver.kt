package com.franferreira.enem2027.notificacao

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.franferreira.enem2027.data.repo.Repositorio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Recebe o alarme do [Agendador] e mostra o aviso, se houver o que avisar.
 */
class LembreteReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_PERIODO = "periodo"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val periodo = intent.getStringExtra(EXTRA_PERIODO)
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = Repositorio(context)
                val pendentes = repo.pendentes()
                if (pendentes > 0) {
                    Notificador.avisar(context, pendentes, periodo)
                }
            } finally {
                // Reagenda os próximos: o alarme é de disparo único.
                Agendador.agendar(context)
                pending.finish()
            }
        }
    }
}

/**
 * Reagenda os lembretes depois de o aparelho reiniciar.
 *
 * Sem isso, todo reinício do celular apagava os alarmes e a Agenda deixava de
 * avisar — a tela continuava mostrando os períodos, mas nada acontecia.
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
