package com.franferreira.enem2027.data.repo

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Cache local dos pacotes de video-aula.
 *
 * O app precisa funcionar offline (requisito do projeto), e a video-aula é
 * conteúdo como o acervo: baixada uma vez, fica no aparelho.
 *
 * O cache é em ARQUIVO, não no Room: cada pacote é um JSON pequeno e
 * independente, e mexer no schema do Room agora custaria uma migração a
 * mais num app que roda em cima do acervo do usuário.
 */
object AulasCache {

    /** O Json usado para ler o cache. */
    val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun pasta(context: Context): File =
        File(context.filesDir, "aulas").apply { mkdirs() }

    private fun arquivo(context: Context, chave: String): File =
        File(pasta(context), "${sanear(chave)}.json")

    private fun sanear(id: String): String =
        id.replace(Regex("[^A-Za-z0-9_-]"), "_").take(120)

    /** Grava um texto JSON (o corpo cru da resposta da API). */
    fun guardar(context: Context, chave: String, corpo: String) {
        runCatching {
            val f = arquivo(context, chave)
            val tmp = File(f.parentFile, f.name + ".tmp")
            tmp.writeText(corpo, Charsets.UTF_8)
            if (!tmp.renameTo(f)) {
                f.writeText(corpo, Charsets.UTF_8)
                tmp.delete()
            }
        }
    }

    /** Lê o cache cru, ou null se não houver. */
    fun ler(context: Context, chave: String): String? {
        val f = arquivo(context, chave)
        if (!f.exists()) return null
        return runCatching { f.readText(Charsets.UTF_8) }.getOrNull()
    }

    fun existe(context: Context, chave: String): Boolean =
        runCatching { arquivo(context, chave).exists() }.getOrDefault(false)

    /** Quantos pacotes estão baixados. */
    fun quantos(context: Context): Int =
        runCatching { pasta(context).listFiles()?.count { it.name.endsWith(".json") } ?: 0 }
            .getOrDefault(0)

    /** O JSON cru do cache, para o app desserializar com o mesmo modelo. */
    inline fun <reified T> lerModelo(context: Context, chave: String): T? {
        val bruto = ler(context, chave) ?: return null
        return runCatching { json.decodeFromString<T>(bruto) }.getOrNull()
    }
}
