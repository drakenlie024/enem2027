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
    private const val CHAVE_MAIS_MATERIA = "materias_mais"
    private const val CHAVE_MENOS_MATERIA = "materias_menos"
    private const val CHAVE_PERGUNTOU = "perguntou"

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

    /**
     * Se a pergunta sobre preferências já foi tratada (respondida ou dispensada).
     * Quem termina o onboarding novo já nasce com a marca; contas antigas não.
     */
    fun jaPerguntou(context: Context): Boolean = prefs(context).getBoolean(CHAVE_PERGUNTOU, false)

    fun marcarPerguntado(context: Context) {
        prefs(context).edit().putBoolean(CHAVE_PERGUNTOU, true).apply()
    }

    /** Peso de cada tema marcado (os temas em Normal não entram no mapa). */
    fun pesos(context: Context): Map<String, Double> {
        val p = prefs(context)
        val mapa = mutableMapOf<String, Double>()
        p.getStringSet(CHAVE_MAIS, emptySet()).orEmpty().forEach { mapa[it] = Nivel.MAIS.peso }
        p.getStringSet(CHAVE_MENOS, emptySet()).orEmpty().forEach { mapa[it] = Nivel.MENOS.peso }
        return mapa
    }

    // ---- matéria

    /** Nível da matéria ("biologia", "historia"...). */
    fun nivelMateria(context: Context, materia: String): Nivel {
        val p = prefs(context)
        return when {
            materia in p.getStringSet(CHAVE_MAIS_MATERIA, emptySet()).orEmpty() -> Nivel.MAIS
            materia in p.getStringSet(CHAVE_MENOS_MATERIA, emptySet()).orEmpty() -> Nivel.MENOS
            else -> Nivel.NORMAL
        }
    }

    fun definirMateria(context: Context, materia: String, nivel: Nivel) {
        val p = prefs(context)
        val mais = p.getStringSet(CHAVE_MAIS_MATERIA, emptySet()).orEmpty().toMutableSet()
        val menos = p.getStringSet(CHAVE_MENOS_MATERIA, emptySet()).orEmpty().toMutableSet()
        mais.remove(materia)
        menos.remove(materia)
        when (nivel) {
            Nivel.MAIS -> mais.add(materia)
            Nivel.MENOS -> menos.add(materia)
            Nivel.NORMAL -> Unit
        }
        p.edit()
            .putStringSet(CHAVE_MAIS_MATERIA, mais)
            .putStringSet(CHAVE_MENOS_MATERIA, menos)
            .apply()
    }

    /** Nível de cada matéria marcada; matéria fora do mapa é Normal. */
    fun niveisMaterias(context: Context): Map<String, Nivel> =
        Materias.LISTA.map { it.first }
            .associateWith { nivelMateria(context, it) }
            .filterValues { it != Nivel.NORMAL }

    /**
     * Peso efetivo de cada tema, para o cronograma.
     *
     * O que foi marcado no tema manda; sem marcação no tema, vale a matéria.
     * Tema sem marcação e sem matéria fica no peso normal (fora do mapa).
     */
    fun pesosPara(context: Context, temas: List<Pair<String, String>>): Map<String, Double> {
        val porTema = pesos(context)
        val porMateria = Materias.LISTA.map { it.first }
            .mapNotNull { m ->
                val n = nivelMateria(context, m)
                if (n == Nivel.NORMAL) null else m to n.peso
            }
            .toMap()
        val saida = mutableMapOf<String, Double>()
        for ((area, tema) in temas) {
            val chave = Diagnostico.chaveTema(area, tema)
            val peso = porTema[chave]
                ?: Materias.doTema(context, tema)?.let { porMateria[it] }
                ?: continue
            saida[chave] = peso
        }
        return saida
    }
}
