package com.franferreira.enem2027.data.repo

import com.franferreira.enem2027.data.model.Question

/**
 * A fila de revisão de hoje, separada por estado de memória.
 *
 * É o retrato do que o SRS decidiu: o que mandou voltar agora, o que você
 * nunca viu e o que você errou (e que por isso volta mais cedo).
 */
data class FilaRevisao(
    val vencendo: List<Question> = emptyList(),
    val novas: List<Question> = emptyList(),
    val erradas: List<Question> = emptyList(),
) {
    val total: Int get() = vencendo.size + novas.size + erradas.size
}
