package com.franferreira.enem2027.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.franferreira.enem2027.data.local.EnemDatabase
import com.franferreira.enem2027.data.repo.Repositorio
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * O simulado passa a contar como estudo.
 *
 * Antes, terminar uma prova não mexia em nada: a questão errada não entrava no
 * caderno nem no treino, e o esforço da prova se perdia. Estes testes garantem
 * que a resposta do simulado grava no progresso como qualquer outra.
 */
@RunWith(AndroidJUnit4::class)
class SimuladoNoTreinoTest {

    private fun bancoLimpo(): EnemDatabase {
        // EnemDatabase é singleton: apagar o arquivo não basta, a conexão antiga
        // continua de pé e o teste anterior deixa dado para trás.
        val db = EnemDatabase.get(ApplicationProvider.getApplicationContext())
        db.clearAllTables()
        return db
    }

    private fun questao(id: String, tema: String, gabarito: String = "A") =
        com.franferreira.enem2027.data.local.QuestionEntity(
            id = id,
            ano = 2023,
            indice = 1,
            area = "matematica",
            idioma = null,
            origem = "enem",
            enunciado = "Enunciado de teste",
            contexto = "",
            arquivosJson = "[]",
            alternativasJson = "[]",
            gabarito = gabarito,
            tema = tema,
            competencia = null,
            dificuldade = "media",
            subassunto = null,
            versaoAcervo = "teste",
        )

    @Test
    fun `resposta errada do simulado entra no caderno de erros`() = runTest {
        val db = bancoLimpo()
        db.questions().inserir(listOf(questao("q1", "Probabilidade")))
        val repo = Repositorio(ApplicationProvider.getApplicationContext())

        repo.registrarResposta("q1", correta = false, segundos = 30f)

        val caderno = repo.cadernoDeErros()
        assertEquals(1, caderno.size)
        assertEquals("q1", caderno.first().questionId)
    }

    @Test
    fun `responder a mesma questao duas vezes nao duplica o progresso`() = runTest {
        val db = bancoLimpo()
        db.questions().inserir(listOf(questao("q1", "Probabilidade")))
        val repo = Repositorio(ApplicationProvider.getApplicationContext())

        repo.registrarResposta("q1", correta = false, segundos = 20f)
        repo.registrarResposta("q1", correta = false, segundos = 25f)

        val progresso = db.progress().porId("q1")
        assertTrue(progresso != null)
        assertEquals(2, progresso!!.wrongCount)
    }

    @Test
    fun `acerto do simulado nao vira erro no caderno`() = runTest {
        val db = bancoLimpo()
        db.questions().inserir(listOf(questao("q1", "Probabilidade")))
        val repo = Repositorio(ApplicationProvider.getApplicationContext())

        repo.registrarResposta("q1", correta = true, segundos = 15f)

        assertTrue(repo.cadernoDeErros().isEmpty())
    }
}
