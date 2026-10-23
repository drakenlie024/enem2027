package com.franferreira.enem2027.notificacao

import android.content.Context

/**
 * Modo silencioso: uma faixa de horas em que o app não manda notificação.
 *
 * Estudar à noite e ser avisado "12 questões te esperando" às 23h é ruído.
 * A faixa padrão é 22h–7h; a pessoa liga e desliga na Agenda.
 */
object Silencio {

    private const val ARQUIVO = "notificacoes"
    private const val CHAVE_ATIVO = "silencio_ativo"
    private const val CHAVE_INICIO = "silencio_inicio"
    private const val CHAVE_FIM = "silencio_fim"

    private fun prefs(context: Context) =
        context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    fun ativo(context: Context): Boolean = prefs(context).getBoolean(CHAVE_ATIVO, true)

    fun inicio(context: Context): Int = prefs(context).getInt(CHAVE_INICIO, 22)

    fun fim(context: Context): Int = prefs(context).getInt(CHAVE_FIM, 7)

    fun definir(context: Context, ativo: Boolean) {
        prefs(context).edit().putBoolean(CHAVE_ATIVO, ativo).apply()
    }

    /** Está dentro da faixa de silêncio agora? */
    fun agoraEstaSilencioso(context: Context): Boolean {
        if (!ativo(context)) return false
        val hora = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val ini = inicio(context)
        val fim = fim(context)
        return if (ini <= fim) hora in ini until fim else (hora >= ini || hora < fim)
    }
}