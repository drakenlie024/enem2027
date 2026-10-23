package com.franferreira.enem2027.data

import com.franferreira.enem2027.data.model.Diagnostico
import com.franferreira.enem2027.data.model.Onboarding
import com.franferreira.enem2027.data.model.Question
import com.franferreira.enem2027.data.model.calcularDiagnostico
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticoTest {

    private fun questao(id: String, area: String, tema: String, gabarito: String = "A") =
        Question(id = id, area = area, tema = tema, gabarito = gabarito)

    @Test
    fun `soma acertos por area e no total`() {
        val questoes = listOf(
            questao("1", "matematica", "Funções"),
            questao("2", "matematica", "Funções"),
            questao("3", "linguagens", "Gêneros"),
        )
        val respostas = mapOf("1" to "A", "2" to "B", "3" to "A")

        val resultado = calcularDiagnostico(questoes, respostas)

        assertEquals(3, resultado.total)
        assertEquals(2, resultado.acertos)
        val matematica = resultado.areas.first { it.area == "matematica" }
        assertEquals(1, matematica.acertos)
        assertEquals(2, matematica.total)
    }

    @Test
    fun `gabarito em minuscula conta como acerto`() {
        val questoes = listOf(questao("1", "matematica", "Funções", gabarito = "C"))

        val resultado = calcularDiagnostico(questoes, mapOf("1" to "c"))

        assertEquals(1, resultado.acertos)
    }

    @Test
    fun `tema com menos de duas questoes nao vira forte nem fraco`() {
        val questoes = listOf(questao("1", "matematica", "Geometria"))

        val tema = calcularDiagnostico(questoes, mapOf("1" to "B")).temas.single()

        assertTrue(tema.poucaAmostra)
        assertFalse(tema.fraco)
        assertFalse(tema.forte)
    }

    @Test
    fun `tema com duas questoes erradas e fraco`() {
        val questoes = listOf(
            questao("1", "matematica", "Logaritmo"),
            questao("2", "matematica", "Logaritmo"),
        )

        val resultado = calcularDiagnostico(questoes, mapOf("1" to "B", "2" to "C"))

        assertEquals(listOf("Logaritmo"), resultado.fracos.map { it.tema })
        assertTrue(resultado.fortes.isEmpty())
    }

    @Test
    fun `tema acertado quase todo e forte e ordena do melhor para o pior`() {
        val questoes = listOf(
            questao("1", "linguagens", "Gêneros"),
            questao("2", "linguagens", "Gêneros"),
            questao("3", "linguagens", "Gêneros"),
            questao("4", "linguagens", "Gêneros"),
            questao("5", "matematica", "Probabilidade"),
            questao("6", "matematica", "Probabilidade"),
        )
        val respostas = mapOf(
            "1" to "A", "2" to "A", "3" to "A", "4" to "B",
            "5" to "B", "6" to "C",
        )

        val resultado = calcularDiagnostico(questoes, respostas)

        assertEquals(listOf("Gêneros"), resultado.fortes.map { it.tema })
        assertEquals(listOf("Probabilidade"), resultado.fracos.map { it.tema })
    }

    @Test
    fun `questao sem resposta conta como erro`() {
        val questoes = listOf(questao("1", "matematica", "Funções"))

        val resultado = calcularDiagnostico(questoes, emptyMap())

        assertEquals(0, resultado.acertos)
    }

    @Test
    fun `nome das areas do diagnostico`() {
        assertEquals("Ciências da Natureza", Diagnostico.nomeDaArea("ciencias-natureza"))
        assertEquals("Linguagens", Diagnostico.nomeDaArea("linguagens"))
    }

    @Test
    fun `sorteio espalha por temas antes de repetir um tema`() {
        // 3 temas com 5 questões cada; pedindo 3, cada tema deve aparecer uma vez.
        val candidatas = (1..5).flatMap { i ->
            listOf("A", "B", "C").map { tema -> "$tema$i" to tema }
        }

        val escolhidas = Diagnostico.espalharPorTema(candidatas, 3) { it.second }

        assertEquals(3, escolhidas.size)
        assertEquals(setOf("A", "B", "C"), escolhidas.map { it.second }.toSet())
    }

    @Test
    fun `sorteio devolve tudo quando ha menos candidatas que a quantidade`() {
        val candidatas = listOf("x1" to "T", "x2" to "T")

        val escolhidas = Diagnostico.espalharPorTema(candidatas, 10) { it.second }

        assertEquals(2, escolhidas.size)
    }

    @Test
    fun `faixa da nota fica em dezenas, em torno da estimativa`() {
        assertEquals(600 to 680, Diagnostico.faixaDaNota(640))
        assertEquals(660 to 740, Diagnostico.faixaDaNota(700))
        assertEquals(600 to 690, Diagnostico.faixaDaNota(645))
    }

    @Test
    fun `onboarding passa de metas para agenda para notificacao e depois sai`() {
        assertEquals("agenda", Onboarding.proxima("metas"))
        assertEquals("notificacao", Onboarding.proxima("agenda"))
        assertEquals(null, Onboarding.proxima("notificacao"))
    }

    @Test
    fun `etapa desconhecida nao leva a lugar nenhum`() {
        assertEquals(null, Onboarding.proxima("inexistente"))
    }

    @Test
    fun `sorteio nao repete questao`() {
        val candidatas = (1..20).map { "q$it" to "T${it % 4}" }

        val escolhidas = Diagnostico.espalharPorTema(candidatas, 10) { it.second }

        assertEquals(10, escolhidas.map { it.first }.toSet().size)
    }
}
