package com.franferreira.enem2027.data.repo

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * Imagens das questões: baixa as figuras do acervo (que estão só em URL remota)
 * para o aparelho, para o app funcionar offline de verdade.
 *
 * O acervo embutido referencia ~980 figuras em `https://enem.dev/...`. Sem
 * baixá-las, offline a figura simplesmente não aparece e a questão não fecha.
 *
 * Estratégia: a URL é a chave. O arquivo local fica em
 * `filesDir/imagens/<hash>.<ext>`. Quem desenha (`TelaQuestao`) usa o arquivo
 * se existir, senão cai na URL (e a Coil tenta online).
 */
object ImagensAcervo {

    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun pasta(context: Context): File =
        File(context.filesDir, "imagens").apply { mkdirs() }

    /** Nome estável para uma URL: hash + extensão. */
    private fun nomeArquivo(url: String): String {
        val ext = url.substringAfterLast('.', "").takeIf { it.length in 2..5 } ?: "png"
        val hash = MessageDigest.getInstance("MD5")
            .digest(url.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return "$hash.$ext"
    }

    /** Arquivo local da URL, se já foi baixado. */
    fun arquivoLocal(context: Context, url: String): File? {
        val f = File(pasta(context), nomeArquivo(url))
        return f.takeIf { it.exists() && it.length() > 100 }
    }

    /** Baixa uma URL para o arquivo local. Devolve true se deu certo. */
    suspend fun baixarUma(context: Context, url: String): Boolean =
        withContext(Dispatchers.IO) {
            val destino = File(pasta(context), nomeArquivo(url))
            if (destino.exists() && destino.length() > 100) return@withContext true
            runCatching {
                val req = Request.Builder().url(url).build()
                http.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext false
                    val corpo = resp.body ?: return@withContext false
                    val tmp = File(destino.parentFile, "${destino.name}.tmp")
                    corpo.byteStream().use { entrada ->
                        tmp.outputStream().use { saida ->
                            entrada.copyTo(saida)
                        }
                    }
                    if (tmp.length() < 100) {
                        tmp.delete()
                        return@withContext false
                    }
                    if (!tmp.renameTo(destino)) {
                        destino.writeBytes(tmp.readBytes())
                        tmp.delete()
                    }
                    true
                }
            }.getOrDefault(false)
        }

    /**
     * Baixa todas as URLs da lista, em segundo plano. Idempotente: as que já
     * existem são puladas. Não lança exceção.
     */
    suspend fun baixarTodas(context: Context, urls: Collection<String>) =
        withContext(Dispatchers.IO) {
            for (url in urls.distinct()) {
                if (url.isBlank()) continue
                if (url.startsWith("http")) baixarUma(context, url)
            }
        }
}