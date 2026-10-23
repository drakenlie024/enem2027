package com.franferreira.enem2027.data.repo

import android.content.Context

/**
 * Estado pessoal das aulas: assistidas, favoritas e a última aberta.
 *
 * Fica em SharedPreferences porque é marcador de leitura, não dado de estudo.
 */
object AulasAssistidas {

    private const val ARQUIVO = "aulas"
    private const val CHAVE_ASSISTIDAS = "assistidas"
    private const val CHAVE_FAVORITAS = "favoritas"
    private const val CHAVE_ULTIMA_ID = "ultima_id"
    private const val CHAVE_ULTIMA_TITULO = "ultima_titulo"
    private const val CHAVE_ULTIMA_AREA = "ultima_area"

    private fun prefs(context: Context) =
        context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    fun assistidas(context: Context): Set<String> =
        prefs(context).getStringSet(CHAVE_ASSISTIDAS, emptySet())?.toSet() ?: emptySet()

    fun marcar(context: Context, videoId: String) {
        val atual = assistidas(context).toMutableSet()
        if (atual.add(videoId)) {
            prefs(context).edit().putStringSet(CHAVE_ASSISTIDAS, atual).apply()
        }
    }

    fun foiAssistida(context: Context, videoId: String): Boolean =
        videoId in assistidas(context)

    // ---- favoritas

    fun favoritas(context: Context): Set<String> =
        prefs(context).getStringSet(CHAVE_FAVORITAS, emptySet())?.toSet() ?: emptySet()

    fun ehFavorita(context: Context, videoId: String): Boolean =
        videoId in favoritas(context)

    /** Alterna o favorito e devolve o novo estado. */
    fun alternarFavorita(context: Context, videoId: String): Boolean {
        val atual = favoritas(context).toMutableSet()
        val virouFavorita = if (atual.contains(videoId)) {
            atual.remove(videoId)
            false
        } else {
            atual.add(videoId)
            true
        }
        prefs(context).edit().putStringSet(CHAVE_FAVORITAS, atual).apply()
        return virouFavorita
    }

    // ---- última aula aberta (continuar assistindo)

    fun guardarUltima(context: Context, videoId: String, titulo: String, area: String) {
        prefs(context).edit()
            .putString(CHAVE_ULTIMA_ID, videoId)
            .putString(CHAVE_ULTIMA_TITULO, titulo)
            .putString(CHAVE_ULTIMA_AREA, area)
            .apply()
    }

    /** (id, título, área) da última aula aberta, se houver. */
    fun ultima(context: Context): Triple<String, String, String>? {
        val p = prefs(context)
        val id = p.getString(CHAVE_ULTIMA_ID, null) ?: return null
        val titulo = p.getString(CHAVE_ULTIMA_TITULO, null).orEmpty()
        val area = p.getString(CHAVE_ULTIMA_AREA, null).orEmpty()
        return Triple(id, titulo, area)
    }
}