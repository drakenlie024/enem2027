package com.franferreira.enem2027.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Currículo das trilhas, como o servidor entrega.
 *
 * É estático: quais trilhas existem, com quais temas, em que ordem. O app
 * baixa uma vez e guarda.
 */
@Serializable
data class CurriculoTrilhas(
    val versao: Int = 1,
    @SerialName("questoes_por_etapa") val questoesPorEtapa: Int = 12,
    val trilhas: List<TrilhaCurriculo> = emptyList(),
    /** {trilha_id: [indice de etapa, ...]} — o que já foi concluído. */
    val progresso: Map<String, List<Int>> = emptyMap(),
)

/** A definição de uma trilha (sem as questões). */
@Serializable
data class TrilhaCurriculo(
    val id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val area: String = "",
    @SerialName("area_nome") val areaNome: String = "",
    val cor: String = "",
    val tipo: String = "area",
    @SerialName("questoes_por_etapa") val questoesPorEtapa: Int = 12,
    val etapas: List<EtapaCurriculo> = emptyList(),
)

/** A definição de uma etapa: um tema e os temas de apoio. */
@Serializable
data class EtapaCurriculo(
    val indice: Int = 0,
    val nome: String = "",
    val apoio: List<String> = emptyList(),
)

/**
 * Uma trilha montada pelo app: o currículo + as questões do aparelho + o
 * progresso local.
 *
 * O app monta isso localmente (e não no servidor) porque é offline-first: as
 * questões já estão no Room, e a trilha adaptativa depende do SRS local.
 */
data class Trilha(
    val id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val area: String = "",
    val areaNome: String = "",
    val cor: String = "",
    val tipo: String = "area",
    val etapas: List<Etapa> = emptyList(),
) {
    val adaptativa: Boolean get() = tipo == "adaptativa"

    val totalEtapas: Int get() = etapas.size

    val concluidas: Int get() = etapas.count { it.concluida }

    val progresso: Double
        get() = if (etapas.isEmpty()) 0.0 else 100.0 * concluidas / etapas.size

    /** Índice da primeira etapa não concluída (null se acabou). */
    val proximaEtapa: Int? get() = etapas.indexOfFirst { !it.concluida }.takeIf { it >= 0 }

    /**
     * Índice da primeira etapa que dá para JOGAR agora.
     *
     * Não é o mesmo que [proximaEtapa]: com um ano só baixado, algumas
     * etapas ficam sem questões (o tema tem 3 questões no acervo inteiro e
     * a etapa anterior consumiu as outras). Uma etapa sem questões não pode
     * travar a trilha — o usuário ficaria preso sem como seguir.
     */
    val primeiraJogavel: Int?
        get() = etapas.indexOfFirst { !it.concluida && it.jogavel }.takeIf { it >= 0 }

    /** A etapa que o usuário deveria atacar agora. */
    val etapaAtual: Etapa? get() = primeiraJogavel?.let { etapas.getOrNull(it) }
}

/**
 * Uma etapa montada: o tema, as 12 questões (ou menos, se o acervo local
 * não tiver) e o resultado.
 */
data class Etapa(
    val indice: Int = 0,
    val nome: String = "",
    val apoio: List<String> = emptyList(),
    val questoes: List<Question> = emptyList(),
    val concluida: Boolean = false,
    val acertos: Int = 0,
    val total: Int = 0,
    val segundos: Int = 0,
    val xp: Int = 0,
) {
    /** "7/8" — o resumo numérico da etapa concluída. */
    val placar: String get() = "$acertos/$total"

    /** "1min 30s" — o tempo gasto, abreviado. */
    val tempo: String
        get() = when {
            segundos <= 0 -> "—"
            segundos < 60 -> "${segundos}s"
            else -> "${segundos / 60}min"
        }

    /** A etapa está pronta para ser feita? (tem questões suficientes) */
    val jogavel: Boolean get() = questoes.isNotEmpty()

    /**
     * Aprovou? (pedido do usuário: 80% de acerto)
     *
     * Abaixo disso a etapa conta como feita, mas marcada como NÃO aprovada:
     * o usuário vê que precisa revisar aquele tema antes de seguir.
     */
    val passou: Boolean
        get() = total > 0 && (acertos * 100f / total) >= PASSO_MINIMO_PCT

    /** Percentual de acerto (0 a 100). */
    val percentual: Int
        get() = if (total == 0) 0 else (acertos * 100 / total)
}

/** Percentual de acerto mínimo para a etapa ser considerada aprovada. */
const val PASSO_MINIMO_PCT = 80

/** O que o app manda ao servidor quando termina uma etapa. */
@Serializable
data class EtapaConcluida(
    @SerialName("trilha_id") val trilhaId: String,
    val etapa: Int,
    val area: String = "",
    val tema: String = "",
    val acertos: Int,
    val total: Int,
    val segundos: Int = 0,
    val xp: Int = 0,
)
