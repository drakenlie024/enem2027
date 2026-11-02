package com.franferreira.enem2027.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.franferreira.enem2027.data.model.Alternative
import com.franferreira.enem2027.data.model.Question
import com.franferreira.enem2027.ui.screens.TelaQuestao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Teste de tela do loop principal: responder e seguir para a próxima.
 *
 * É o gesto que ela repete dezenas de vezes por sessão. Antes, responder não
 * levava a lugar nenhum — era voltar para a lista e achar a questão seguinte.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], qualifiers = "w411dp-h891dp-xhdpi")
class TelaQuestaoTeste {

    @get:Rule
    val compose = createComposeRule()

    private val questao = Question(
        id = "q1",
        ano = 2019,
        area = "ciencias-natureza",
        enunciado = "Qual é a resposta certa desta questão de teste?",
        alternativas = listOf(
            Alternative(letra = "A", texto = "Primeira alternativa"),
            Alternative(letra = "B", texto = "Segunda alternativa"),
            Alternative(letra = "C", texto = "Terceira alternativa"),
        ),
        gabarito = "B",
        tema = "Ecologia",
        explicacao = "Porque sim.",
    )

    private fun montar(aoProxima: (() -> Unit)? = null, posicao: Int = 3, total: Int = 12) {
        compose.setContent {
            androidx.compose.material3.MaterialTheme {
                TelaQuestao(
                    questao = questao,
                    aoResponder = { _, _, _ -> },
                    aoVoltar = {},
                    aoPedirExplicacao = { null },
                    aoProxima = aoProxima,
                    posicao = posicao,
                    total = total,
                )
            }
        }
    }

    @Test
    fun `a questao abre com as alternativas`() {
        montar()
        compose.onNodeWithText("Primeira alternativa").assertIsDisplayed()
        compose.onNodeWithText("Segunda alternativa").assertIsDisplayed()
    }

    @Test
    fun `proxima questao so aparece depois de responder`() {
        montar(aoProxima = {})

        // Antes de responder, o botão não existe.
        compose.onNodeWithText("Próxima questão").assertDoesNotExist()

        compose.onNodeWithText("Segunda alternativa").performClick()

        compose.onNodeWithText("Próxima questão").assertIsDisplayed()
        compose.onNodeWithText("3 de 12").assertIsDisplayed()
    }

    @Test
    fun `tocar em proxima questao avisa a tela`() {
        var avancou = false
        montar(aoProxima = { avancou = true })

        compose.onNodeWithText("Segunda alternativa").performClick()
        compose.onNodeWithText("Próxima questão").performClick()

        assertTrue(avancou)
    }

    @Test
    fun `sem proxima questao o botao nao aparece`() {
        // Abrir pelo caderno ou pela busca: não há "próxima".
        montar(aoProxima = null)

        compose.onNodeWithText("Segunda alternativa").performClick()

        compose.onNodeWithText("Próxima questão").assertDoesNotExist()
    }
}
