package com.franferreira.enem2027.data.repo

import android.content.Context

/**
 * Quais aulas a pessoa já abriu para assistir.
 *
 * Fica em SharedPreferences, não no Room: é um marcador de leitura, leve e sem
 * relação com o SRS. Serve para a lista mostrar o que já foi visto e para o
 * \"continuar assistindo\" não repetir o começo.
 */
object AulasAssistidas {

    private const val ARQUIVO = "aulas"
    private const val CHAVE = "assistidas"

    private fun prefs(context: Context) =
        context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    fun assistidas(context: Context): Set<String> =
        prefs(context).getStringSet(CHAVE, emptySet())?.toSet() ?: emptySet()

    fun marcar(context: Context, videoId: String) {
        val atual = assistidas(context).toMutableSet()
        if (atual.add(videoId)) {
            prefs(context).edit().putStringSet(CHAVE, atual).apply()
        }
    }

    fun foiAssistida(context: Context, videoId: String): Boolean =
        videoId in assistidas(context)
}