package com.franferreira.enem2027.data.repo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Atualização do próprio app, fora da loja.
 *
 * Android não troca um app instalado por APK em silêncio. O que dá é: consultar
 * um manifesto na VPS, baixar o APK, conferir o hash e abrir o instalador do
 * sistema, onde a pessoa confirma com um toque.
 *
 * Usa um cliente próprio, sem o token da API: o APK não é do servidor da API.
 */
object Atualizacao {

    const val MANIFESTO = "https://leticia.franferreira.cloud/versao.json"

    data class Nova(
        val versionCode: Long,
        val versionName: String,
        val url: String,
        val sha256: String,
    )

    private val consulta = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val download = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun codigoAtual(context: Context): Long =
        context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode

    /** Versão nova no manifesto, ou null (sem versão nova, sem rede ou manifesto inválido). */
    fun verificar(context: Context): Nova? = runCatching {
        consulta.newCall(Request.Builder().url(MANIFESTO).build()).execute().use { r ->
            if (!r.isSuccessful) return@use null
            val j = JSONObject(r.body!!.string())
            val nova = Nova(
                versionCode = j.getLong("versionCode"),
                versionName = j.getString("versionName"),
                url = j.getString("url"),
                sha256 = j.getString("sha256").lowercase(),
            )
            nova.takeIf { it.versionCode > codigoAtual(context) }
        }
    }.getOrNull()

    fun podeInstalar(context: Context): Boolean =
        context.packageManager.canRequestPackageInstalls()

    /** Tela do sistema para liberar "instalar apps desconhecidos" para este app. */
    fun abrirPermissaoInstalar(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /** Baixa o APK e confere o hash. Lança erro se a rede cair ou o hash não bater. */
    fun baixar(context: Context, nova: Nova): File {
        val pasta = File(context.cacheDir, "atualizacao").apply { mkdirs() }
        val destino = File(pasta, "leticia-${nova.versionName}.apk")
        download.newCall(Request.Builder().url(nova.url).build()).execute().use { r ->
            if (!r.isSuccessful) error("HTTP ${r.code}")
            r.body!!.byteStream().use { entrada ->
                destino.outputStream().use { saida -> entrada.copyTo(saida) }
            }
        }
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(destino.readBytes())
            .joinToString("") { "%02x".format(it) }
        if (hash != nova.sha256) {
            destino.delete()
            error("hash não confere")
        }
        return destino
    }

    /** Abre o instalador do sistema com o APK já baixado. */
    fun instalar(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
