package com.franferreira.enem2027.data.repo

import android.content.Context

/**
 * Anotação livre por questão: o caderninho de margem.
 *
 * O rascunho (desenho) é para a conta; a anotação é para a ideia que a pessoa
 * quer lembrar na próxima vez que a questão aparecer. Fica em
 * SharedPreferences, separada do SRS.
 */
object Anotacoes {

    private const val ARQUIVO = "anotacoes"

    private fun prefs(context: Context) =
        context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    fun ler(context: Context, questionId: String): String =
        prefs(context).getString(questionId, null).orEmpty()

    fun salvar(context: Context, questionId: String, texto: String) {
        val editor = prefs(context).edit()
        if (texto.isBlank()) {
            editor.remove(questionId)
        } else {
            editor.putString(questionId, texto)
        }
        editor.apply()
    }
}