package com.franferreira.enem2027.data.repo

import android.content.Context
import com.franferreira.enem2027.data.local.JsonCompartilhado
import com.franferreira.enem2027.data.model.YearPackage

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

    private val json = JsonCompartilhado

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

    /**
     * Carrega o pacote de um ano do APK.
     *
     * Devolve null se o arquivo nao existir ou nao der para ler.
     */
    fun carregar(context: Context, ano: Int): YearPackage? = try {
        val bruto = context.assets.open("$PASTA/enem-$ano.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        json.decodeFromString(YearPackage.serializer(), bruto)
    } catch (e: Exception) {
        null
    }
}
