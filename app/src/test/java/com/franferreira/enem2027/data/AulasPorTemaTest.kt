package com.franferreira.enem2027.data

import com.franferreira.enem2027.data.repo.AulasPorTema
import org.junit.Assert.assertEquals
import org.junit.Test

class AulasPorTemaTest {

    @Test
    fun `ignora acento e maiuscula`() {
        assertEquals("estatistica", AulasPorTema.normalizar("Estatística"))
        assertEquals("era vargas", AulasPorTema.normalizar("ERA VARGAS"))
    }

    @Test
    fun `junta espacos repetidos`() {
        assertEquals("cinetica quimica", AulasPorTema.normalizar("  Cinetica   quimica  "))
    }

    @Test
    fun `tema vazio vira string vazia`() {
        assertEquals("", AulasPorTema.normalizar(null))
        assertEquals("", AulasPorTema.normalizar("   "))
    }
}
