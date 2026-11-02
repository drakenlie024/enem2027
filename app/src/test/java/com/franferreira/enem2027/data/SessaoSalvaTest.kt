package com.franferreira.enem2027.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.franferreira.enem2027.data.model.TemaTreino
import com.franferreira.enem2027.data.repo.SessaoSalva
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * A sessão guardada no aparelho.
 *
 * Antes ela vivia só na memória: fechar o app no meio mandava a pessoa de
 * volta ao primeiro tema. O teste garante que ela volta onde parou — e que a
 * sessão de ontem não é retomada hoje.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class SessaoSalvaTest {

    private val contexto: Context = ApplicationProvider.getApplicationContext()

    private fun tema(nome: String) = TemaTreino(
        tema = nome,
        area = "ciencias-natureza",
        total = 10,
        vistas = 2,
        acertos = 1,
        disponiveis = 8,
        diasDesde = 3,
        intervaloDias = 4,
        forca = 0.5f,
        vencidoHa = -1,
        novo = false,
        emRevisao = false,
    )

    @Before
    fun limpar() {
        SessaoSalva.limpar(contexto)
    }

    @Test
    fun `sem sessao salva nao devolve nada`() {
        assertNull(SessaoSalva.carregar(contexto))
    }

    @Test
    fun `volta no mesmo tema em que parou`() {
        SessaoSalva.salvar(contexto, listOf(tema("Ecologia"), tema("Genética")), indice = 1)

        val salva = SessaoSalva.carregar(contexto)!!

        assertEquals(listOf("Ecologia", "Genética"), salva.temas.map { it.tema })
        assertEquals(1, salva.indice)
    }

    @Test
    fun `sessao de outro dia nao e retomada`() {
        SessaoSalva.salvar(contexto, listOf(tema("Ecologia")), indice = 0)
        // Simula a sessão gravada ontem.
        contexto.getSharedPreferences("sessao", Context.MODE_PRIVATE)
            .edit()
            .putString("dia", LocalDate.now().minusDays(1).toString())
            .apply()

        assertNull(SessaoSalva.carregar(contexto))
    }

    @Test
    fun `encerrar apaga a sessao`() {
        SessaoSalva.salvar(contexto, listOf(tema("Ecologia")), indice = 0)
        SessaoSalva.limpar(contexto)

        assertNull(SessaoSalva.carregar(contexto))
    }
}
