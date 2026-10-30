package com.franferreira.enem2027.data.repo

import android.content.Context
import com.franferreira.enem2027.data.model.Diagnostico

/**
 * O que a aluna gosta mais e menos de estudar, tema por tema.
 *
 * "Mais" puxa o tema para o começo da fila da sua área no cronograma e para a
 * aula do dia; "menos" empurra para o fim. Nenhum tema some do plano.
 *
 * Fica em SharedPreferences porque é opinião pessoal, não dado de estudo.
 */
object PreferenciasEstudo {

    enum class Nivel(val peso: Double, val rotulo: String) {
        MENOS(0.5, "Menos"),
        NORMAL(1.0, "Normal"),
        MAIS(2.0, "Mais");

        /** Próximo nível no toque: Normal → Mais → Menos → Normal. */
        fun proximo(): Nivel = when (this) {
            NORMAL -> MAIS
            MAIS -> MENOS
            MENOS -> NORMAL
        }
    }

    private const val ARQUIVO = "preferencias"
    private const val CHAVE_MAIS = "temas_mais"
    private const val CHAVE_MENOS = "temas_menos"

    private fun prefs(context: Context) =
        context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    fun nivel(context: Context, area: String, tema: String): Nivel {
        val chave = Diagnostico.chaveTema(area, tema)
        val p = prefs(context)
        return when {
            chave in p.getStringSet(CHAVE_MAIS, emptySet()).orEmpty() -> Nivel.MAIS
            chave in p.getStringSet(CHAVE_MENOS, emptySet()).orEmpty() -> Nivel.MENOS
            else -> Nivel.NORMAL
        }
    }

    fun definir(context: Context, area: String, tema: String, nivel: Nivel) {
        val chave = Diagnostico.chaveTema(area, tema)
        val p = prefs(context)
        val mais = p.getStringSet(CHAVE_MAIS, emptySet()).orEmpty().toMutableSet()
        val menos = p.getStringSet(CHAVE_MENOS, emptySet()).orEmpty().toMutableSet()
        mais.remove(chave)
        menos.remove(chave)
        when (nivel) {
            Nivel.MAIS -> mais.add(chave)
            Nivel.MENOS -> menos.add(chave)
            Nivel.NORMAL -> Unit
        }
        p.edit()
            .putStringSet(CHAVE_MAIS, mais)
            .putStringSet(CHAVE_MENOS, menos)
            .apply()
    }

    /** Peso de cada tema marcado (os temas em Normal não entram no mapa). */
    fun pesos(context: Context): Map<String, Double> {
        val p = prefs(context)
        val mapa = mutableMapOf<String, Double>()
        p.getStringSet(CHAVE_MAIS, emptySet()).orEmpty().forEach { mapa[it] = Nivel.MAIS.peso }
        p.getStringSet(CHAVE_MENOS, emptySet()).orEmpty().forEach { mapa[it] = Nivel.MENOS.peso }
        return mapa
    }
}
