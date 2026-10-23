package com.franferreira.enem2027.data

import com.franferreira.enem2027.data.repo.MetaSemanal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class MetaSemanalTest {

    @Test
    fun `prova longe sugere menos dias por semana`() {
        assertEquals(3, MetaSemanal.sugeridaPara(400))
        assertEquals(4, MetaSemanal.sugeridaPara(200))
        assertEquals(5, MetaSemanal.sugeridaPara(100))
    }

    @Test
    fun `prova ja passada sugere o maximo`() {
        assertEquals(5, MetaSemanal.sugeridaPara(-30))
    }

    @Test
    fun `dias ate a prova contam ate 1 de novembro`() {
        val hoje = LocalDate.of(2026, 10, 23)

        // 23/10/2026 até 23/10/2027 são 365 dias; mais 9 até 1º/11/2027.
        assertEquals(374L, MetaSemanal.diasAteAProva(2027, hoje))
    }
}
