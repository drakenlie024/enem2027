package com.franferreira.enem2027.data.model

/**
 * Diagnóstico de entrada: 40 questões, 10 de cada área do ENEM.
 *
 * Tema com menos de [MIN_AMOSTRA] questões não ganha rótulo de forte ou fraco:
 * com uma ou duas questões, acertar ou errar diz pouco sobre o assunto.
 */
object Diagnostico {
    val AREAS = listOf("linguagens", "ciencias-humanas", "ciencias-natureza", "matematica")
    const val POR_AREA = 10
    const val MIN_AMOSTRA = 2
    const val TAXA_FORTE = 0.75
    const val TAXA_FRACA = 0.5

    fun nomeDaArea(area: String): String = when (area) {
        "linguagens" -> "Linguagens"
        "ciencias-humanas" -> "Ciências Humanas"
        "ciencias-natureza" -> "Ciências da Natureza"
        "matematica" -> "Matemática"
        else -> area
    }

    /** Margem da previsão inicial: com 40 questões, a nota é uma faixa, não um número. */
    const val MARGEM_NOTA = 40

    /** Faixa de nota (mínimo, máximo) em múltiplos de 10, em torno da estimativa. */
    fun faixaDaNota(nota: Int): Pair<Int, Int> {
        val minimo = ((nota - MARGEM_NOTA) / 10) * 10
        val maximo = ((nota + MARGEM_NOTA + 9) / 10) * 10
        return minimo to maximo
    }

    /** Chave única de um tema dentro de uma área (usada para guardar os fracos). */
    fun chaveTema(area: String, tema: String): String = "$area|$tema"

    /**
     * Uma questão por tema por rodada, em ordem aleatória, até a quantidade.
     * Assim o diagnóstico cobre o máximo de temas antes de repetir algum.
     */
    fun <T> espalharPorTema(candidatas: List<T>, quantidade: Int, temaDe: (T) -> String): List<T> {
        val porTema = candidatas
            .groupBy(temaDe)
            .values
            .map { it.shuffled().toMutableList() }
            .shuffled()
            .toMutableList()
        val saida = mutableListOf<T>()
        while (saida.size < quantidade && porTema.isNotEmpty()) {
            val iterador = porTema.iterator()
            while (iterador.hasNext() && saida.size < quantidade) {
                val lista = iterador.next()
                saida += lista.removeAt(0)
                if (lista.isEmpty()) iterador.remove()
            }
        }
        return saida
    }
}

/**
 * As etapas do primeiro acesso depois do nome. O diagnóstico vem depois delas
 * e não entra aqui: ele é obrigatório e tem tela própria.
 */
object Onboarding {
    val ETAPAS = listOf("metas", "agenda", "notificacao")

    /** Próxima etapa depois de [atual]; null quando a próxima é o diagnóstico. */
    fun proxima(atual: String): String? {
        val indice = ETAPAS.indexOf(atual)
        if (indice < 0) return null
        return ETAPAS.getOrNull(indice + 1)
    }
}

data class AreaDiagnostico(val area: String, val acertos: Int, val total: Int) {
    val taxa: Double get() = if (total == 0) 0.0 else acertos.toDouble() / total
}

data class TemaDiagnostico(val area: String, val tema: String, val acertos: Int, val total: Int) {
    val taxa: Double get() = if (total == 0) 0.0 else acertos.toDouble() / total
    val poucaAmostra: Boolean get() = total < Diagnostico.MIN_AMOSTRA
    val forte: Boolean get() = !poucaAmostra && taxa >= Diagnostico.TAXA_FORTE
    val fraco: Boolean get() = !poucaAmostra && taxa < Diagnostico.TAXA_FRACA
}

data class ResultadoDiagnostico(
    val acertos: Int,
    val total: Int,
    val areas: List<AreaDiagnostico>,
    val temas: List<TemaDiagnostico>,
) {
    val taxa: Double get() = if (total == 0) 0.0 else acertos.toDouble() / total
    val fortes: List<TemaDiagnostico> get() = temas.filter { it.forte }.sortedByDescending { it.taxa }
    val fracos: List<TemaDiagnostico> get() = temas.filter { it.fraco }.sortedBy { it.taxa }
    val semAmostra: Int get() = temas.count { it.poucaAmostra }
}

/** Monta o resultado a partir das questões e das respostas (id da questão -> letra). */
fun calcularDiagnostico(questoes: List<Question>, respostas: Map<String, String>): ResultadoDiagnostico {
    fun acertou(q: Question): Boolean =
        respostas[q.id]?.equals(q.gabarito.orEmpty(), ignoreCase = true) == true

    val areas = Diagnostico.AREAS.map { area ->
        val daArea = questoes.filter { it.area == area }
        AreaDiagnostico(area, daArea.count { acertou(it) }, daArea.size)
    }
    val temas = questoes
        .groupBy { it.area.orEmpty() to it.tema.orEmpty().ifBlank { "Sem tema" } }
        .map { (chave, daTema) ->
            TemaDiagnostico(chave.first, chave.second, daTema.count { acertou(it) }, daTema.size)
        }
    return ResultadoDiagnostico(
        acertos = questoes.count { acertou(it) },
        total = questoes.size,
        areas = areas,
        temas = temas,
    )
}
