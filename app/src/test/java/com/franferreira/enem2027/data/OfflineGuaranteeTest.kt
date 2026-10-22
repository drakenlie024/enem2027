package com.franferreira.enem2027.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.franferreira.enem2027.data.local.EnemDatabase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Teste de garantia do offline.
 *
 * O requisito nº 1 do projeto é que o app funcione sem internet. Este teste
 * verifica o que sustenta isso: o acervo baixado e o progresso das respostas
 * vivem no banco LOCAL, e continuam lá e legíveis sem nenhuma rede.
 */
@RunWith(AndroidJUnit4::class)
class OfflineGuaranteeTest {

    private fun bancoLimpo(): EnemDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        File(context.getDatabasePath("enem2027.db").absolutePath).delete()
        return EnemDatabase.get(context)
    }

    private fun questaoDeTeste(
        id: String,
        ano: Int,
        area: String,
        tema: String,
        gabarito: String,
    ) = com.franferreira.enem2027.data.local.QuestionEntity(
        id = id,
        ano = ano,
        indice = 1,
        area = area,
        idioma = null,
        origem = "enem",
        enunciado = "Enunciado de teste",
        contexto = "",
        arquivosJson = "[]",
        alternativasJson = "[]",
        gabarito = gabarito,
        tema = tema,
        competencia = "C2 - Aplicar conhecimentos",
        dificuldade = "media",
        subassunto = null,
        versaoAcervo = "teste",
    )

    @Test
    fun `acervo baixado fica no banco local e sobrevive sem rede`() = runTest {
        val db = bancoLimpo()
        val dao = db.questions()

        // Simula o que o app faz quando baixa um ano: grava local.
        // (Nao ha rede neste teste - e nao deve haver.)
        dao.inserir(
            listOf(
                questaoDeTeste("enem-2023-q001", 2023, "matematica", "Probabilidade", "A"),
                questaoDeTeste("enem-2023-q002", 2023, "matematica", "Probabilidade", "C"),
            )
        )

        // Reabre o banco: simula o app sendo fechado e aberto de novo.
        val reaberto = EnemDatabase.get(ApplicationProvider.getApplicationContext())
        val total = reaberto.questions().totalDoAno(2023)
        assertEquals("o acervo tem que sobreviver ao reinicio do app", 2, total)

        val anos = reaberto.questions().anos()
        assertTrue("2023 tem que estar entre os anos", 2023 in anos)
    }

    @Test
    fun `resposta registrada offline fica na fila e nao se perde`() = runTest {
        val db = bancoLimpo()
        val dao = db.progress()

        dao.upsert(
            com.franferreira.enem2027.data.local.ProgressEntity(
                questionId = "enem-2023-q001",
                state = "learning",
                dueAt = System.currentTimeMillis() + 60_000,
                lastReviewedAt = System.currentTimeMillis(),
                lastQuality = 4,
                correctCount = 1,
            )
        )

        val reaberto = EnemDatabase.get(ApplicationProvider.getApplicationContext())
        val card = reaberto.progress().porId("enem-2023-q001")
        assertTrue("a resposta tem que estar no banco local", card != null)
        assertEquals(4, card?.lastQuality)
        assertTrue("tem que estar marcada como nao sincronizada", card?.synced == false)
    }
}
