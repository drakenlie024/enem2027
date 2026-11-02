package com.franferreira.enem2027.data.repo

import android.content.Context
import com.franferreira.enem2027.data.model.TemaTreino
import java.time.LocalDate
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * A sessão de hoje, guardada no aparelho.
 *
 * Antes ela vivia só na memória: fechar o app no meio do segundo tema mandava
 * a pessoa de volta ao primeiro. Aqui ficam o dia, os temas e onde ela parou.
 *
 * Some sozinha quando o dia vira: sessão de ontem não faz sentido hoje.
 */
object SessaoSalva {

    private const val ARQUIVO = "sessao"
    private const val CHAVE_DIA = "dia"
    private const val CHAVE_TEMAS = "temas"
    private const val CHAVE_INDICE = "indice"

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val lista = ListSerializer(TemaTreino.serializer())

    data class Salva(val temas: List<TemaTreino>, val indice: Int)

    private fun prefs(context: Context) =
        context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    fun salvar(context: Context, temas: List<TemaTreino>, indice: Int) {
        val hoje = LocalDate.now().toString()
        prefs(context).edit()
            .putString(CHAVE_DIA, hoje)
            .putString(CHAVE_TEMAS, runCatching { json.encodeToString(lista, temas) }.getOrNull())
            .putInt(CHAVE_INDICE, indice)
            .apply()
    }

    fun carregar(context: Context): Salva? {
        val p = prefs(context)
        if (p.getString(CHAVE_DIA, null) != LocalDate.now().toString()) return null
        val bruto = p.getString(CHAVE_TEMAS, null) ?: return null
        val temas = runCatching { json.decodeFromString(lista, bruto) }.getOrNull() ?: return null
        if (temas.isEmpty()) return null
        return Salva(temas, p.getInt(CHAVE_INDICE, 0))
    }

    fun limpar(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
