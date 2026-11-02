package com.franferreira.enem2027.data.model

/**
 * Uma video-aula sugerida para um tema de questão.
 *
 * [urlExterna] existe para os temas que não têm aula no acervo: em vez de
 * inventar um vídeo, o app aponta para a playlist do canal que cobre aquela
 * matéria. Nesse caso [id] vem vazio e a abertura é no navegador.
 */
data class AulaSugerida(
    val id: String,
    val titulo: String,
    val subtema: String,
    val area: String,
    val urlExterna: String? = null,
)
