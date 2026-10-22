package com.franferreira.enem2027.data.repo

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Baixa a video-aula direto do YouTube para dentro do aparelho.
 *
 * O servidor NAO guarda nem entrega video: ele so tem o resumo e as
 * questoes. O video e baixado do YouTube pelo proprio celular, guardado no
 * armazenamento privado do app, e pode ser apagado quando o usuario quiser.
 *
 * Como resolver o video:
 * A API interna do YouTube (`youtubei/v1/player`) com o cliente ANDROID
 * devolve os enderecos de streaming. Medido em 22/10/2026: o cliente ANDROID
 * na versao 20.10.38 responde com o formato combinado (video+audio) em 360p;
 * as versoes antigas e o cliente WEB devolvem "UNPLAYABLE" ou erro 400.
 *
 * O formato combinado de 360p serve para uma aula de quadro: e leve e cabe
 * bem na tela do celular.
 */
object VideoDownloader {

    private const val PLAYER_URL = "https://www.youtube.com/youtubei/v1/player"
    private const val CLIENTE = "ANDROID"
    private const val CLIENTE_VERSAO = "20.10.38"
    private const val USER_AGENT =
        "com.google.android.youtube/20.10.38 (Linux; U; Android 11) gzip"

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    // ---- resposta do player (so o que interessa)
    @Serializable
    private class RespostaPlayer(
        @SerialName("playabilityStatus") val status: Status? = null,
        @SerialName("videoDetails") val detalhes: Detalhes? = null,
        @SerialName("streamingData") val streaming: Streaming? = null,
    )

    @Serializable
    private class Status(@SerialName("status") val status: String? = null)

    @Serializable
    private class Detalhes(
        @SerialName("title") val titulo: String? = null,
        @SerialName("lengthSeconds") val duracao: String? = null,
    )

    @Serializable
    private class Streaming(
        @SerialName("formats") val formatos: List<Formato> = emptyList(),
        @SerialName("adaptiveFormats") val adaptativos: List<Formato> = emptyList(),
    )

    @Serializable
    private class Formato(
        @SerialName("itag") val itag: Int = 0,
        @SerialName("url") val url: String? = null,
        @SerialName("mimeType") val mimeType: String? = null,
        @SerialName("qualityLabel") val qualidade: String? = null,
        @SerialName("contentLength") val tamanho: String? = null,
        @SerialName("audioQuality") val audioQuality: String? = null,
        @SerialName("bitrate") val bitrate: Long = 0,
    )

    data class Resolucao(
        val url: String,
        val titulo: String,
        val duracaoSegundos: Int,
        val tamanhoBytes: Long,
        val qualidade: String,
    )

    // ---- arquivos
    private fun pasta(context: Context): File =
        File(context.filesDir, "videos").apply { mkdirs() }

    private fun arquivo(context: Context, videoId: String): File =
        File(pasta(context), "${videoId.replace(Regex("[^A-Za-z0-9_-]"), "_")}.mp4")

    /** O video ja esta no aparelho? */
    fun existe(context: Context, videoId: String): Boolean {
        val f = arquivo(context, videoId)
        return f.exists() && f.length() > 10_000
    }

    /** Tamanho do video guardado, em bytes. */
    fun tamanho(context: Context, videoId: String): Long =
        runCatching { arquivo(context, videoId).length() }.getOrDefault(0L)

    /** Apaga o video do aparelho. */
    fun apagar(context: Context, videoId: String): Boolean =
        runCatching { arquivo(context, videoId).delete() }.getOrDefault(false)

    /** O arquivo (para abrir no player do sistema). */
    fun arquivoLocal(context: Context, videoId: String): File = arquivo(context, videoId)

    /**
     * Resolve o endereco de download do video.
     *
     * Devolve null se o YouTube nao entregar (video removido, restricao de
     * idade, erro de rede).
     */
    suspend fun resolver(videoId: String): Resolucao? = withContext(Dispatchers.IO) {
        runCatching {
            val corpo = """
                {"videoId":"$videoId",
                 "context":{"client":{"clientName":"$CLIENTE","clientVersion":"$CLIENTE_VERSAO",
                 "androidSdkVersion":30,"hl":"pt","gl":"BR"}},
                 "contentCheckOk":true,"racyCheckOk":true}
            """.trimIndent()
            val req = Request.Builder()
                .url(PLAYER_URL)
                .header("Content-Type", "application/json")
                .header("User-Agent", USER_AGENT)
                .header("X-YouTube-Client-Name", "3")
                .header("X-YouTube-Client-Version", CLIENTE_VERSAO)
                .header("Accept-Language", "pt-BR")
                .post(corpo.toRequestBody("application/json".toMediaType()))
                .build()

            http.newCall(req).execute().use { resposta ->
                val texto = resposta.body?.string().orEmpty()
                if (!resposta.isSuccessful) return@runCatching null
                val dados = json.decodeFromString(RespostaPlayer.serializer(), texto)
                if (dados.status?.status != "OK") return@runCatching null
                val sd = dados.streaming ?: return@runCatching null

                // O formato COMBINADO (video+audio no mesmo arquivo) e o que
                // dá para salvar e assistir; os adaptativos são separados e
                // precisariam de muxing.
                val combined = sd.formatos.filter { it.url != null && "mp4" in (it.mimeType ?: "") }
                val escolhido = combined.maxByOrNull { it.bitrate }
                    ?: return@runCatching null
                Resolucao(
                    url = escolhido.url!!,
                    titulo = dados.detalhes?.titulo ?: videoId,
                    duracaoSegundos = dados.detalhes?.duracao?.toIntOrNull() ?: 0,
                    tamanhoBytes = escolhido.tamanho?.toLongOrNull() ?: 0L,
                    qualidade = escolhido.qualidade ?: "",
                )
            }
        }.getOrNull()
    }

    /**
     * Baixa o video para o armazenamento privado do app.
     *
     * `aoProgresso` recebe (baixados, total). O download vai para um .tmp e
     * so vira .mp4 no fim: um app morto no meio nao deixa um arquivo
     * corrompido que o player nao abre.
     */
    suspend fun baixar(
        context: Context,
        videoId: String,
        aoProgresso: (baixados: Long, total: Long) -> Unit = { _, _ -> },
    ): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            val resolucao = resolver(videoId)
                ?: return@runCatching Result.failure(
                    IllegalStateException("O YouTube nao entregou este video.")
                )
            val destino = arquivo(context, videoId)
            val temporario = File(destino.parentFile, "${destino.name}.tmp")
            val req = Request.Builder()
                .url(resolucao.url)
                .header("User-Agent", USER_AGENT)
                .build()

            http.newCall(req).execute().use { resposta ->
                val corpo = resposta.body ?: return@runCatching Result.failure(
                    IllegalStateException("Resposta vazia do YouTube.")
                )
                val total = corpo.contentLength().takeIf { it > 0 } ?: resolucao.tamanhoBytes
                var baixados = 0L
                corpo.byteStream().use { entrada ->
                    temporario.outputStream().use { saida ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val lidos = entrada.read(buffer)
                            if (lidos <= 0) break
                            saida.write(buffer, 0, lidos)
                            baixados += lidos
                            aoProgresso(baixados, total)
                        }
                    }
                }
                if (!temporario.renameTo(destino)) {
                    destino.writeBytes(temporario.readBytes())
                    temporario.delete()
                }
            }
            Result.success(destino.length())
        }.getOrElse { Result.failure(it) }
    }
}
