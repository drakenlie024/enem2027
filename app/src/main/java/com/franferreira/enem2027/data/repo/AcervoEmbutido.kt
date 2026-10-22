package com.franferreira.enem2027.data.repo

import android.content.Context
import com.franferreira.enem2027.data.local.JsonCompartilhado
import com.franferreira.enem2027.data.local.paraEntidade
import com.franferreira.enem2027.data.model.Question
import com.franferreira.enem2027.data.model.YearPackage
import kotlinx.serialization.json.Json

/**
 * O acervo que vem DENTRO do APK.
 *
 * O app é um curso: as 17 provas do ENEM (2009-2025), com a explicação
 * offline de cada questão, já no instalador. Não há download, não há espera,
 * e o app funciona offline desde o primeiro clique.
 *
 * O servidor continua existindo para sincronizar progresso, correção de
 * redação e (se o acervo for corrigido) para rebaixar um ano.
 *
 * Os arquivos ficam em `assets/enem/enem-<ano>.json` e têm o mesmo formato
 * que a API devolve — é o mesmo acervo, a mesma explicação.
 */
object AcervoEmbutido {

    private const val PASTA = "enem"

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    /** Os anos que vêm no APK, do mais novo para o mais antigo. */
    fun anos(context: Context): List<Int> {
        val nomes = try {
            context.assets.list(PASTA)?.toList().orEmpty()
        } catch (e: Exception) {
            emptyList()
        }
        return nomes
            .mapNotNull { nome ->
                Regex("enem-(\\d{4})\\.json").find(nome)?.groupValues?.get(1)?.toIntOrNull()
            }
            .sortedDescending()
    }

    /** Carrega o pacote de um ano do APK. */
    fun carregar(context: Context, ano: Int): YearPackage? = try {
        val bruto = context.assets.open("$PASTA/enem-$ano.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        json.decodeFromString(YearPackage.serializer(), bruto)
    } catch (e: Exception) {
        null
    }

    /**
     * Semeia um ano no banco local.
     *
     * Grava só o que ainda nao esta (o `substituirAno` do DAO e idempotente
     * por ano), entao chamar de novo nao quebra o progresso.
     */
    fun semear(
        context: Context,
        ano: Int,
        gravar: (ano: Int, versao: String?, questoes: List<Question>) -> Unit,
    ): Int {
        val pacote = carregar(context, ano) ?: return 0
        if (pacote.questoes.isEmpty()) return 0
        gravar(ano, pacote.versao, pacote.questoes)
        return pacote.questoes.size
    }

    /** Converte para entidade (o mesmo caminho que o download usa). */
    fun Question.paraEntidadeDoAcervo(versao: String?) = paraEntidade(versao)
}
