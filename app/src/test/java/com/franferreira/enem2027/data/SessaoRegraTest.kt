package com.franferreira.enem2027.data

import com.franferreira.enem2027.data.model.SessaoRegra
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regra de avanço da sessão guiada.
 *
 * O usuário relatou: abriu as questões do tema, voltou sem responder, abriu de
 * novo e vieram menos questões — porque a sessão já tinha pulado para outro
 * tema. O teste protege esse comportamento.
 */
class SessaoRegraTest {

    private val treino = listOf("q1", "q2", "q3")

    @Test
    fun `nao avanca sem responder nada`() {
        assertFalse(SessaoRegra.terminou(treino, emptySet()))
    }

    @Test
    fun `nao avanca com o tema pela metade`() {
        assertFalse(SessaoRegra.terminou(treino, setOf("q1", "q2")))
    }

    @Test
    fun `avanca quando o tema inteiro foi respondido`() {
        assertTrue(SessaoRegra.terminou(treino, setOf("q1", "q2", "q3")))
    }

    @Test
    fun `treino sem questoes nao avanca sozinho`() {
        assertFalse(SessaoRegra.terminou(emptyList(), emptySet()))
    }

    @Test
    fun `resposta de outra tela nao conta para o tema`() {
        assertFalse(SessaoRegra.terminou(treino, setOf("q1", "outra", "mais-outra")))
    }
}
