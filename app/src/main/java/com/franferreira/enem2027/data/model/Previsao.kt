package com.franferreira.enem2027.data.model

import com.franferreira.enem2027.data.local.AcertoAreaRow
import com.franferreira.enem2027.data.local.ErroRow
import com.franferreira.enem2027.data.repo.DadosTri

/**
 * Como o usuário está em cada área, e o que isso significa em nota.
 */
data class DesempenhoArea(
    val area: String,
    val nome: String,
    val acertos: Int,
    val respondidas: Int,
    val totalQuestoes: Int,
    /** Nota estimada pelo TRI, se houver dados suficientes. */
    val notaEstimada: Int?,
    /** Acertos que a mediana precisou fazer para chegar na nota-alvo. */
    val acertosParaAlvo: Int?,
) {
    /** Acerto sobre o total de questões da área. */
    val cobertura: Float
        get() = if (totalQuestoes > 0) respondidas.toFloat() / totalQuestoes else 0f

    val taxa: Int
        get() = if (respondidas > 0) (100 * acertos / respondidas) else 0

    /** Faltam quantas questões certas para bater a nota-alvo. */
    val acertosFaltamParaAlvo: Int?
        get() = acertosParaAlvo?.let { maxOf(0, it - acertos) }
}

/**
 * A previsão de nota do usuário.
 *
 * IMPORTANTE: é uma ESTIMATIVA. O TRI é calculado com a TRI bruta de cada
 * participante, que o app não tem. O que o app faz é usar as medianas de
 * acerto→nota dos microdados do Inep — serve para dar uma ordem de grandeza
 * e dizer "faltam X acertos para 700", não para prometer um número.
 */
data class PrevisaoNota(
    val areas: List<DesempenhoArea> = emptyList(),
    val anoAlvo: Int = 2024,
    val notaAlvo: Int = 700,
) {
    /** Média simples das áreas com estimativa (peso igual, como no ENEM). */
    val notaMedia: Int?
        get() {
            val notas = areas.mapNotNull { it.notaEstimada }
            return if (notas.isEmpty()) null else notas.average().toInt()
        }

    val respondidas: Int get() = areas.sumOf { it.respondidas }
    val acertos: Int get() = areas.sumOf { it.acertos }

    /** As áreas mais fracas, na ordem de prioridade. */
    val areasMaisFracas: List<DesempenhoArea>
        get() = areas.filter { it.respondidas > 0 }.sortedBy { it.taxa }
}

/**
 * Monta a previsão a partir do que o usuário já respondeu.
 */
object Previsor {

    /** Ordem canônica das áreas do ENEM. */
    private val ORDEM = listOf(
        "linguagens",
        "ciencias-humanas",
        "ciencias-natureza",
        "matematica",
    )

    fun prever(
        acertosPorArea: List<AcertoAreaRow>,
        anoAlvo: Int = 2024,
        notaAlvo: Int = 700,
    ): PrevisaoNota {
        val porArea = acertosPorArea.associateBy { it.area }
        val areas = ORDEM.map { area ->
            val linha = porArea[area]
            val respondidas = linha?.respondidas ?: 0
            val acertos = linha?.acertos ?: 0
            DesempenhoArea(
                area = area,
                nome = DadosTri.NOMES_AREA[area] ?: area,
                acertos = acertos,
                respondidas = respondidas,
                totalQuestoes = DadosTri.TOTAL_QUESTOES_AREA[area] ?: 45,
                notaEstimada = if (respondidas >= 5) {
                    DadosTri.notaEstimada(area, acertos)
                } else {
                    null
                },
                acertosParaAlvo = DadosTri.acertosParaNota(area, notaAlvo),
            )
        }
        return PrevisaoNota(areas = areas, anoAlvo = anoAlvo, notaAlvo = notaAlvo)
    }

    /**
     * Onde a nota estimada cai na faixa daquele ano.
     *
     * A faixa (mínimo e máximo apurados) é a referência de mercado: se a
     * nota estimada está acima do máximo, é competitive; se está abaixo do
     * mínimo, não compensa naquele ano.
     */
    fun posicaoNaFaixa(area: String, nota: Int?, ano: Int): PosicaoFaixa {
        val faixa = DadosTri.faixaDoAno(ano, area)
        if (faixa == null || nota == null) return PosicaoFaixa.DESCONHECIDA
        val (minimo, maximo) = faixa
        return when {
            nota >= maximo -> PosicaoFaixa.ACIMA
            nota >= minimo -> PosicaoFaixa.DENTRO
            else -> PosicaoFaixa.ABAIXO
        }
    }
}

/** Onde a nota estimada está em relação à faixa daquele ano. */
enum class PosicaoFaixa(val rotulo: String) {
    ACIMA("acima do máximo"),
    DENTRO("dentro da faixa"),
    ABAIXO("abaixo do mínimo"),
    DESCONHECIDA("sem dados"),
}

/**
 * Uma linha do caderno de erros, pronta para a tela.
 */
data class ItemCaderno(
    val id: String,
    val ano: Int,
    val area: String?,
    val tema: String?,
    val enunciado: String?,
    val gabarito: String?,
    val vezesErrada: Int,
    val vezesAcertada: Int,
    val ultimaVez: Long?,
) {
    /** Errou mais de uma vez: o item merece atenção especial. */
    val reincidente: Boolean get() = vezesErrada >= 2

    companion object {
        fun de(row: ErroRow): ItemCaderno = ItemCaderno(
            id = row.questionId,
            ano = row.ano,
            area = row.area,
            tema = row.tema,
            enunciado = row.enunciado,
            gabarito = row.gabarito,
            vezesErrada = row.wrongCount,
            vezesAcertada = row.correctCount,
            ultimaVez = row.lastReviewedAt,
        )
    }
}
