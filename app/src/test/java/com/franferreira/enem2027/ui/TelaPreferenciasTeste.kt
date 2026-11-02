package com.franferreira.enem2027.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.franferreira.enem2027.data.model.TemaDaSemana
import com.franferreira.enem2027.data.repo.PreferenciasEstudo
import com.franferreira.enem2027.ui.screens.TelaPreferencias
import com.franferreira.enem2027.ui.theme.EnemTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Teste de tela das preferências.
 *
 * O ponto que já falhou: a matéria tem que aparecer mesmo sem tema carregado,
 * porque a tela é o único jeito de a pessoa dizer o que gosta.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], qualifiers = "w411dp-h891dp-xhdpi")
class TelaPreferenciasTeste {

    @get:Rule
    val compose = createComposeRule()

    private fun montar(
        temas: List<TemaDaSemana> = emptyList(),
        aoMudarMateria: (String, PreferenciasEstudo.Nivel) -> Unit = { _, _ -> },
    ) {
        compose.setContent {
            EnemTheme {
                TelaPreferencias(
                    temas = temas,
                    preferencias = emptyMap(),
                    preferenciasMaterias = emptyMap(),
                    aoMudar = { _, _ -> },
                    aoMudarMateria = aoMudarMateria,
                    aoVoltar = {},
                )
            }
        }
    }

    @Test
    fun `as materias aparecem mesmo sem tema carregado`() {
        montar()
        compose.onNodeWithText("MATÉRIAS").assertIsDisplayed()
        compose.onNodeWithText("Biologia").assertIsDisplayed()
        compose.onNodeWithText("História").assertIsDisplayed()
    }

    @Test
    fun `tocar na materia sobe para Mais`() {
        var pedida: Pair<String, PreferenciasEstudo.Nivel>? = null
        montar(aoMudarMateria = { materia, nivel -> pedida = materia to nivel })

        // O primeiro "Normal" é o da primeira matéria da lista (Biologia).
        compose.onAllNodesWithText("Normal")[0].performClick()

        assertEquals("biologia", pedida?.first)
        assertEquals(PreferenciasEstudo.Nivel.MAIS, pedida?.second)
    }
}
