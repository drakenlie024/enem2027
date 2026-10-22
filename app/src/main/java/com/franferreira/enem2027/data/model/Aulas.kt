package com.franferreira.enem2027.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Video-aulas: o video, o resumo em topicos e as questoes sobre a aula.
 *
 * Tudo pre-computado no servidor, entao o app mostra sem depender de rede.
 * A origem e "aula" — separada do acervo oficial do Inep. O gabarito de uma
 * questao de aula tem o TIMESTAMP do momento da video onde a resposta
 * aparece, e por isso a pessoa sempre consegue conferir.
 */

@Serializable
data class AreasAula(
    val areas: List<AreaAulas> = emptyList(),
)

@Serializable
data class AreaAulas(
    val area: String = "",
    val aulas: Int = 0,
    val questoes: Int = 0,
    val temas: List<String> = emptyList(),
)

@Serializable
data class AulasDeArea(
    val area: String = "",
    val temas: List<TemaComAulas> = emptyList(),
)

@Serializable
data class TemaComAulas(
    val tema: String = "",
    val aulas: List<AulaResumo> = emptyList(),
)

@Serializable
data class AulaResumo(
    val id: String = "",
    val titulo: String = "",
    val area: String = "",
    val tema: String = "",
    val canal: String = "",
    val duracao: Int = 0,
    val url: String = "",
    val resumo: List<String> = emptyList(),
    @SerialName("total_questoes") val totalQuestoes: Int = 0,
)

@Serializable
data class PacoteAula(
    val id: String = "",
    val titulo: String = "",
    val area: String = "",
    val tema: String = "",
    val canal: String = "",
    val duracao: Int = 0,
    val url: String = "",
    val origem: String = "aula",
    val palavras: Int = 0,
    val resumo: List<String> = emptyList(),
    val questoes: List<QuestaoAula> = emptyList(),
)

@Serializable
data class QuestaoAula(
    val enunciado: String = "",
    val alternativas: List<AlternativaAula> = emptyList(),
    val gabarito: String = "",
    val justificativa: String = "",
    /** "MM:SS" — o momento da video onde a resposta aparece. */
    val timestamp: String = "",
)

@Serializable
data class AlternativaAula(
    val letra: String = "",
    val texto: String = "",
)
