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

    override fun onReceive(context: Context, intent: Intent) {
        val periodo = intent.getStringExtra("periodo")
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = Repositorio(context)
                val pendentes = repo.pendentes()
                if (pendentes > 0) {
                    Notificador.avisar(context, pendentes, periodo)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
