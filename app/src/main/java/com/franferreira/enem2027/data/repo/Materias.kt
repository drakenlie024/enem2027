package com.franferreira.enem2027.data.repo

import android.content.Context
import org.json.JSONObject

/**
 * A matéria de cada tema de questão.
 *
 * O acervo só guarda área do ENEM (Ciências da Natureza) e tema (Citologia). A
 * matéria — biologia, química, história — vem desta tabela, montada a partir da
 * matéria das aulas de cada tema, com os temas sem aula preenchidos à mão.
 *
 * As duas caixas genéricas do acervo ("Ciências Humanas", "Ciências da
 * Natureza") não têm matéria: misturam várias, então ficam sem preferência.
 */
object Materias {

    /** As matérias, na ordem em que aparecem na tela. */
    val LISTA: List<Pair<String, String>> = listOf(
        "biologia" to "Biologia",
        "fisica" to "Física",
        "quimica" to "Química",
        "matematica" to "Matemática",
        "historia" to "História",
        "geografia" to "Geografia",
        "sociologia" to "Sociologia",
        "filosofia" to "Filosofia",
        "portugues" to "Português",
        "ingles" to "Inglês",
        "espanhol" to "Espanhol",
        "redacao" to "Redação",
    )

    private var cache: Map<String, String>? = null

    private fun carregar(context: Context): Map<String, String> {
        cache?.let { return it }
        val mapa = runCatching {
            val texto = context.assets.open(ARQUIVO).bufferedReader().use { it.readText() }
            val raiz = JSONObject(texto)
            buildMap {
                raiz.keys().forEach { tema ->
                    val materia = raiz.optString(tema)
                    if (materia.isNotBlank()) put(tema, materia)
                }
            }
        }.getOrDefault(emptyMap())
        cache = mapa
        return mapa
    }

    /** Matéria do tema ("biologia"), ou null quando o tema é uma caixa genérica. */
    fun doTema(context: Context, tema: String?): String? =
        carregar(context)[AulasPorTema.normalizar(tema)]

    fun rotulo(materia: String): String =
        LISTA.firstOrNull { it.first == materia }?.second
            ?: materia.replaceFirstChar { it.uppercase() }

    private const val ARQUIVO = "materias_por_tema.json"
}
