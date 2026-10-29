package com.franferreira.enem2027.data.model

/** Uma video-aula sugerida para um tema de questão. */
data class AulaSugerida(
    val id: String,
    val titulo: String,
    val subtema: String,
    val area: String,
)
