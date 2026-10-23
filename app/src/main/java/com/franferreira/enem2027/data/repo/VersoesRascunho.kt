package com.franferreira.enem2027.data.repo

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Versões do rascunho da redação.
 *
 * Cada vez que a pessoa manda corrigir, o texto vai para cá antes de sair do
 * aparelho. Se a correção mudar tudo e ela quiser voltar, a versão anterior
 * está guardada — as cinco últimas.
 */
object VersoesRascunho {

    private const val ARQUIVO = "redacao_versoes"
    private const val CHAVE = "versoes"
    private const val MAXIMO = 5

    data class Versao(val quando: Long, val texto: String)

    private fun prefs(context: Context) =
        context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    fun listar(context: Context): List<Versao> {
        val bruto = prefs(context).getString(CHAVE, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(bruto)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.getJSONObject(i)
                Versao(o.optLong("quando"), o.optString("texto"))
            }
        }.getOrDefault(emptyList())
    }

    fun salvar(context: Context, texto: String) {
        if (texto.isBlank()) return
        val atuais = listar(context)
        // Não guarda a mesma versão duas vezes seguidas.
        if (atuais.firstOrNull()?.texto == texto) return
        val novas = (listOf(Versao(System.currentTimeMillis(), texto)) + atuais).take(MAXIMO)
        val arr = JSONArray()
        novas.forEach { v ->
            arr.put(JSONObject().put("quando", v.quando).put("texto", v.texto))
        }
        prefs(context).edit().putString(CHAVE, arr.toString()).apply()
    }
}