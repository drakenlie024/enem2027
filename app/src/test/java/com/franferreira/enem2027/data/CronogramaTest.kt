package com.franferreira.enem2027.data

import com.franferreira.enem2027.data.model.Cronograma
import com.franferreira.enem2027.data.model.TemaDaSemana
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CronogramaTest {

    private fun tema(nome: String, area: String, coberto: Boolean = false) =
        TemaDaSemana(tema = nome, area = area, totalQuestoes = 10, coberto = coberto)

    private val hoje = LocalDate.of(2026, 10, 24)

    @Test
    fun `semanas ate a prova conta em semanas`() {
        assertEquals(53, Cronograma.semanasAte(hoje, LocalDate.of(2027, 11, 1)))
    }

    @Test
    fun `prova no passado nao deixa o plano vazio`() {
        assertEquals(4, Cronograma.semanasAte(hoje, LocalDate.of(2026, 1, 1)))
    }

    @Test
    fun `todo tema entra no plano`() {
        val temas = (1..20).map { tema("t$it", "matematica") }

        val semanas = Cronograma.montar(temas, semanas = 10, inicio = hoje, hoje = hoje)

        assertEquals(10, semanas.size)
        assertEquals(20, semanas.sumOf { it.temas.size })
    }

    @Test
    fun `tema coberto nao volta como novo`() {
        val temas = listOf(
            tema("novo", "matematica"),
            tema("visto", "historia", coberto = true),
        )

        val semanas = Cronograma.montar(temas, semanas = 4, inicio = hoje, hoje = hoje)

        val novos = semanas.flatMap { it.temas }.map { it.tema }
        assertEquals(listOf("novo"), novos)
    }

    @Test
    fun `semana alterna as areas`() {
        val temas = (1..3).flatMap { n ->
            listOf(tema("m$n", "matematica"), tema("h$n", "ciencias-humanas"))
        }

        val semanas = Cronograma.montar(temas, semanas = 3, inicio = hoje, hoje = hoje)
        val primeira = semanas.first().temas

        assertEquals(2, primeira.size)
        assertEquals(2, primeira.map { it.area }.distinct().size)
    }

    @Test
    fun `semana com tema nao coberto e marcada como atrasada depois que passa`() {
        val temas = (1..8).map { tema("t$it", "matematica") }

        val semanas = Cronograma.montar(
            temas,
            semanas = 4,
            inicio = hoje.minusWeeks(3),
            hoje = hoje,
        )

        assertTrue(semanas.take(3).all { it.atrasada })
        assertTrue(semanas.last().atual)
    }

    @Test
    fun `cobertura conta por area`() {
        val temas = listOf(
            tema("a", "matematica", coberto = true),
            tema("b", "matematica"),
            tema("c", "historia"),
        )

        val cobertura = Cronograma.cobertura(temas)

        assertEquals(1 to 2, cobertura["matematica"])
        assertEquals(0 to 1, cobertura["historia"])
    }
}
