package com.franferreira.enem2027.data.repo

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Rascunho por questão: o papel digital.
 *
 * A conta de matemática morre junto com a resposta. Hoje o app só guarda a
 * alternativa marcada — o cálculo vai para o papelão e some. Com o rascunho
 * digital, a conta fica colada na questão, onde o usuário pode voltar.
 *
 * Os traços são gravados em arquivo, não no Room: assim não precisa de
 * migração de banco (o Room do app está em versão 3 e mexer nisso é
 * risco num app que roda em cima do acervo do usuário).
 *
 * As coordenadas são **normalizadas** (0 a 1) para o traço sobreviver a
 * outro tamanho de tela.
 */
object RascunhoStore {

    @Serializable
    data class Ponto(val x: Float, val y: Float)

    @Serializable
    data class Traco(
        val pontos: List<Ponto> = emptyList(),
        /** Índice na paleta (0 = grafite, 1 = azul, 2 = vermelho, 3 = verde). */
        val cor: Int = 0,
        val espessura: Float = 3f,
    )

    @Serializable
    private class Arquivo(val tracos: List<Traco> = emptyList())

    private val json = Json { ignoreUnknownKeys = true }

    private fun pasta(context: Context): File =
        File(context.filesDir, "rascunhos").apply { mkdirs() }

    private fun arquivo(context: Context, questionId: String): File =
        File(pasta(context), "${sanear(questionId)}.json")

    /** Ids de arquivo não podem ter nada além de letras, números, - e _ */
    private fun sanear(id: String): String =
        id.replace(Regex("[^A-Za-z0-9_-]"), "_").take(120)

    fun carregar(context: Context, questionId: String): List<Traco> {
        val f = arquivo(context, questionId)
        if (!f.exists()) return emptyList()
        return runCatching {
            json.decodeFromString(Arquivo.serializer(), f.readText(Charsets.UTF_8)).tracos
        }.getOrDefault(emptyList())
    }

    fun salvar(context: Context, questionId: String, tracos: List<Traco>) {
        val f = arquivo(context, questionId)
        runCatching {
            val conteudo = json.encodeToString(Arquivo.serializer(), Arquivo(tracos))
            // Escrita atômica: um crash no meio não deixa o rascunho pela metade.
            val tmp = File(f.parentFile, f.name + ".tmp")
            tmp.writeText(conteudo, Charsets.UTF_8)
            if (!tmp.renameTo(f)) {
                f.writeText(conteudo, encoding="utf-8")
                tmp.delete()
            }
        }
    }

    fun apagar(context: Context, questionId: String) {
        runCatching { arquivo(context, questionId).delete() }
    }

    /** Há rascunho salvo para esta questão? */
    fun existe(context: Context, questionId: String): Boolean =
        runCatching { arquivo(context, questionId).exists() }.getOrDefault(false)
}
