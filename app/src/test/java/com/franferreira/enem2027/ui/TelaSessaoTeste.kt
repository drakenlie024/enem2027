package com.franferreira.enem2027.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.franferreira.enem2027.data.model.TemaTreino
import com.franferreira.enem2027.ui.screens.TelaSessao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Teste de tela da sessão guiada.
 *
 * Existe porque o "Começar" já abriu uma lista em vez do itinerário, e depois
 * uma tela vazia — nenhum dos dois aparecia no teste unitário. Aqui a tela é
 * montada de verdade e o texto que a pessoa lê é conferido.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], qualifiers = "w411dp-h891dp-xhdpi")
class TelaSessaoTeste {

    @get:Rule
    val compose = createComposeRule()

    private fun tema(nome: String, area: String = "ciencias-natureza", vistas: Int = 0) =
        TemaTreino(
            tema = nome,
            area = area,
            total = 40,
            vistas = vistas,
            acertos = 0,
            disponiveis = 12,
            diasDesde = null,
            intervaloDias = 2,
            forca = 0f,
            vencidoHa = 1,
            novo = vistas == 0,
        )

    @Test
    fun `mostra o tema do dia com a aula e a pratica`() {
        // "Ecologia" tem aula mapeada na tabela de aulas.
        compose.setContent {
            androidx.compose.material3.MaterialTheme {
                TelaSessao(
                    temas = listOf(tema("Ecologia"), tema("Genética")),
                    indice = 0,
                    aoTreinar = {},
                    aoAbrirAula = {},
                    aoEncerrar = {},
                )
            }
        }

        compose.onNodeWithText("TEMA 1 DE 2").assertIsDisplayed()
        compose.onNodeWithText("Ecologia").assertIsDisplayed()
        compose.onNodeWithText("2 · PRATICAR").assertIsDisplayed()
        compose.onNodeWithText("PRÓXIMO TEMA").assertIsDisplayed()
    }

    @Test
    fun `tema sem aula comeca pela pratica`() {
        // "Estatística" não tem aula: o passo 1 é praticar, sem buraco na conta.
        compose.setContent {
            androidx.compose.material3.MaterialTheme {
                TelaSessao(
                    temas = listOf(tema("Estatística", area = "matematica")),
                    indice = 0,
                    aoTreinar = {},
                    aoAbrirAula = {},
                    aoEncerrar = {},
                )
            }
        }

        compose.onNodeWithText("1 · PRATICAR").assertIsDisplayed()
    }

    @Test
    fun `tocar em comecar as questoes abre o treino do tema`() {
        var treinado: String? = null
        compose.setContent {
            androidx.compose.material3.MaterialTheme {
                TelaSessao(
                    temas = listOf(tema("Estatística", area = "matematica")),
                    indice = 0,
                    aoTreinar = { treinado = it.tema },
                    aoAbrirAula = {},
                    aoEncerrar = {},
                )
            }
        }

        compose.onNodeWithText("Começar as questões").performClick()

        assertEquals("Estatística", treinado)
    }

    @Test
    fun `sessao vazia diz que nao ha nada, em vez de abrir tela em branco`() {
        var encerrou = false
        compose.setContent {
            androidx.compose.material3.MaterialTheme {
                TelaSessao(
                    temas = emptyList(),
                    indice = 0,
                    aoTreinar = {},
                    aoAbrirAula = {},
                    aoEncerrar = { encerrou = true },
                )
            }
        }

        compose.onNodeWithText("Nada para estudar agora").assertIsDisplayed()
        compose.onNodeWithText("Voltar para a Hoje").performClick()
        assertTrue(encerrou)
    }

    @Test
    fun `o proximo tema pode ser pulado sem responder tudo`() {
        var pulou = false
        compose.setContent {
            androidx.compose.material3.MaterialTheme {
                TelaSessao(
                    temas = listOf(tema("Ecologia"), tema("Genética")),
                    indice = 0,
                    aoTreinar = {},
                    aoAbrirAula = {},
                    aoEncerrar = {},
                    aoPular = { pulou = true },
                )
            }
        }

        compose.onNodeWithText("Pular para este tema").performClick()

        assertTrue(pulou)
    }

    @Test
    fun `sessao concluida avisa em vez de voltar sozinha`() {        compose.setContent {
            androidx.compose.material3.MaterialTheme {
                TelaSessao(
                    temas = listOf(tema("Ecologia")),
                    indice = 1,
                    aoTreinar = {},
                    aoAbrirAula = {},
                    aoEncerrar = {},
                )
            }
        }

        compose.onNodeWithText("Sessão concluída").assertIsDisplayed()
    }
}
