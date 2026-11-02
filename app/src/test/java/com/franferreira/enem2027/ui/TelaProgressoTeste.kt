package com.franferreira.enem2027.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.franferreira.enem2027.data.model.ProgressoResponse
import com.franferreira.enem2027.ui.screens.TelaProgresso
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Teste de tela do Progresso.
 *
 * O que interessa aqui é o relato de problema: é a saída para quando algo
 * quebra no aparelho e não há ninguém do outro lado para ver a tela.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], qualifiers = "w411dp-h891dp-xhdpi")
class TelaProgressoTeste {

    @get:Rule
    val compose = createComposeRule()

    private fun montar(aoRelatar: (String) -> Unit = {}) {
        compose.setContent {
            androidx.compose.material3.MaterialTheme {
                TelaProgresso(
                    progresso = ProgressoResponse(),
                    aoAbrirAgenda = {},
                    aoAbrirAprovacao = {},
                    aoAbrirNota = {},
                    aoAbrirCaderno = {},
                    aoAbrirAulas = {},
                    pendentesAprovacao = 0,
                    aoRelatarProblema = aoRelatar,
                )
            }
        }
    }

    @Test
    fun `o convite do relato aparece no fim da tela`() {
        montar()
        compose.onNodeWithText("Algo não funcionou?").assertIsDisplayed()
    }

    @Test
    fun `o relato digitado chega para envio`() {
        var recebido: String? = null
        montar(aoRelatar = { recebido = it })

        compose.onNodeWithText("Algo não funcionou?").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("toquei em Começar e não abriu nada")
        compose.onNodeWithText("Enviar").performClick()

        assertEquals("toquei em Começar e não abriu nada", recebido)
    }

    @Test
    fun `cancelar nao envia nada`() {
        var enviou = false
        montar(aoRelatar = { enviou = true })

        compose.onNodeWithText("Algo não funcionou?").performClick()
        compose.onNodeWithText("Cancelar").performClick()

        assertFalse(enviou)
    }
}
