package com.franferreira.enem2027.data

import com.franferreira.enem2027.ui.components.FlorNivel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A conta da flor.
 *
 * O desenho depende dela: se a fase ou o campo errarem, a flor mostra um
 * estágio que não corresponde ao nível — e o campo ganha uma flor que ela
 * ainda não completou.
 */
class FlorNivelTest {

    @Test
    fun `nivel 10 custa nove mil xp`() {
        // É o número combinado: no ritmo de 20 por dia, completa em ~45 dias.
        assertEquals(9000, FlorNivel.xpParaChegar(10))
    }

    @Test
    fun `cada fase corresponde a um trecho do ciclo`() {
        assertEquals(1, FlorNivel.faseDoNivel(1))
        assertEquals(2, FlorNivel.faseDoNivel(2))
        assertEquals(5, FlorNivel.faseDoNivel(5))
        assertEquals(6, FlorNivel.faseDoNivel(6))
        assertEquals(6, FlorNivel.faseDoNivel(7))
        assertEquals(7, FlorNivel.faseDoNivel(8))
        assertEquals(8, FlorNivel.faseDoNivel(10))
    }

    @Test
    fun `a flor nao regride depois do ciclo`() {
        // Nível 11 abre uma plantinha nova; a anterior vai para o campo.
        assertEquals(1, FlorNivel.faseDoNivel(11))
        assertEquals(1, FlorNivel.floresNoCampo(11))
        assertEquals(8, FlorNivel.faseDoNivel(20))
        assertEquals(1, FlorNivel.floresNoCampo(20))
    }

    @Test
    fun `campo conta uma flor por ciclo completo`() {
        assertEquals(0, FlorNivel.floresNoCampo(1))
        assertEquals(0, FlorNivel.floresNoCampo(10))
        assertEquals(1, FlorNivel.floresNoCampo(11))
        assertEquals(2, FlorNivel.floresNoCampo(21))
    }

    @Test
    fun `progresso no nivel fica entre zero e um`() {
        val base = FlorNivel.xpParaChegar(5)
        val alvo = FlorNivel.xpDoNivel(5)
        assertEquals(0f, FlorNivel.progressoNoNivel(base, 5), 0.001f)
        assertTrue(FlorNivel.progressoNoNivel(base + alvo - 50, 5) > 0.9f)
        // XP muito acima do nível não estoura a barra.
        assertEquals(1f, FlorNivel.progressoNoNivel(base + 99999, 5), 0.001f)
    }
}
