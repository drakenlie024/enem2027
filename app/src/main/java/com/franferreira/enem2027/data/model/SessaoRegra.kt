package com.franferreira.enem2027.data.model

/**
 * Quando a sessão pode passar para o próximo tema.
 *
 * Regra: só depois de responder o tema inteiro. Antes bastava sair do treino
 * — e como cada tema tem um número diferente de questões, parecia que a lista
 * encolhia a cada volta.
 */
object SessaoRegra {

    fun terminou(idsDoTreino: List<String>, respondidas: Set<String>): Boolean =
        idsDoTreino.isNotEmpty() && idsDoTreino.all { it in respondidas }
}
