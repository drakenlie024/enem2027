package com.franferreira.enem2027.data.repo

import android.content.Context
import com.franferreira.enem2027.data.model.AulaSugerida
import java.text.Normalizer
import org.json.JSONObject

/**
 * Qual video-aula ensina cada tema de questão.
 *
 * A tabela vem pronta no APK (`assets/aulas_por_tema.json`): foi montada sobre os
 * títulos das 1.025 aulas, porque o subtema da aula e o tema da questão usam
 * vocabulários diferentes (só 4 dos 77 temas batiam pelo nome).
 *
 * Fica em arquivo, e não numa busca por palavra-chave, para funcionar offline e
 * para não recomendar aula errada quando o nome do tema não aparece no título.
 */
object AulasPorTema {

    private var cache: Map<String, List<AulaSugerida>>? = null

    fun normalizar(texto: String?): String {
        if (texto.isNullOrBlank()) return ""
        val semAcento = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return semAcento.lowercase().replace(Regex("\\s+"), " ")
    }

    private fun carregar(context: Context): Map<String, List<AulaSugerida>> {
        cache?.let { return it }
        val mapa = runCatching {
            val texto = context.assets.open(ARQUIVO).bufferedReader().use { it.readText() }
            val raiz = JSONObject(texto)
            buildMap {
                raiz.keys().forEach { tema ->
                    val lista = raiz.optJSONArray(tema) ?: return@forEach
                    val aulas = (0 until lista.length()).mapNotNull { i ->
                        val item = lista.optJSONObject(i) ?: return@mapNotNull null
                        val id = item.optString("id")
                        if (id.isBlank()) return@mapNotNull null
                        AulaSugerida(
                            id = id,
                            titulo = item.optString("titulo"),
                            subtema = item.optString("subtema"),
                            area = item.optString("area"),
                        )
                    }
                    if (aulas.isNotEmpty()) put(tema, aulas)
                }
            }
        }.getOrDefault(emptyMap())
        cache = mapa
        return mapa
    }

    /** As aulas que ensinam este tema, da mais adequada para a menos. */
    fun aulasDoTema(context: Context, tema: String?): List<AulaSugerida> {
        val chave = normalizar(tema)
        if (chave.isBlank()) return emptyList()
        return carregar(context)[chave].orEmpty()
    }

    private const val ARQUIVO = "aulas_por_tema.json"
}
