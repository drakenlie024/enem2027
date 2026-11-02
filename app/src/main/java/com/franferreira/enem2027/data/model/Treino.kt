package com.franferreira.enem2027.data.model

import kotlinx.serialization.Serializable

/**
 * Um tema como unidade de treino espaçado.
 *
 * O app agenda temas, não questões: cada tema tem um intervalo que cresce
 * conforme o acerto, e a sessão sempre traz questões novas daquele tema.
 */
@Serializable
data class TemaTreino(
    val tema: String,
    val area: String,
    val total: Int,
    val vistas: Int,
    val acertos: Int,
    /** Questões que podem ser servidas hoje (novas ou fora do cooldown). */
    val disponiveis: Int,
    /** Dias desde a última prática (null = nunca praticado). */
    val diasDesde: Int?,
    /** Intervalo atual do tema, em dias. */
    val intervaloDias: Int,
    /** Acertos / vistas (0..1). */
    val forca: Float,
    /** Dias além do intervalo (0 = vence hoje; nunca visto fica alto). */
    val vencidoHa: Int,
    /** Nunca praticado. */
    val novo: Boolean,
    /** Sem questão nova: o treino vai revisar o que já foi visto. */
    val emRevisao: Boolean = false,
) {
    val percentual: Int get() = (forca * 100).toInt()
}
