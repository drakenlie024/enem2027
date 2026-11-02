package com.franferreira.enem2027.data.repo

import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Relato de problema do aparelho.
 *
 * Existe por um motivo prático: quando algo não aparece na tela, do outro lado
 * não há como saber a versão, quantas questões existem nem qual exceção
 * aconteceu. Sem isso, cada investigação vira tentativa e erro.
 *
 * Não manda nada sozinho: monta o texto e abre o compartilhamento do Android,
 * para a pessoa escolher para onde vai.
 */
object RelatorioProblema {

    private const val ARQUIVO = "diagnostico"
    private const val CHAVE_ERRO = "ultimo_erro"

    private fun prefs(context: Context) =
        context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    /** Guarda a última exceção não tratada, para entrar no relato. */
    fun guardarErro(context: Context, erro: Throwable?) {
        if (erro == null) return
        val texto = buildString {
            append(erro.javaClass.simpleName)
            append(": ")
            append(erro.message ?: "sem mensagem")
            erro.stackTrace.take(6).forEach { append("\n  ").append(it.toString()) }
        }
        prefs(context).edit().putString(CHAVE_ERRO, texto.take(2000)).apply()
    }

    fun ultimoErro(context: Context): String? = prefs(context).getString(CHAVE_ERRO, null)

    /** Liga o gancho de exceção não tratada: a próxima abertura já traz o erro. */
    fun instalarGanchoDeErro(context: Context) {
        val anterior = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, erro ->
            runCatching { guardarErro(context, erro) }
            anterior?.uncaughtException(thread, erro)
        }
    }

    /** Monta o texto do relato: versão, aparelho, dados e o que ela escreveu. */
    fun texto(
        context: Context,
        versao: String,
        questoes: Int,
        temas: Int,
        trilhas: Int,
        erroDoAcervo: String?,
        relato: String,
    ): String = buildString {
        appendLine("RELATO DO APP (Letícia ENEM)")
        appendLine("versão: $versao")
        appendLine("aparelho: ${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE}")
        appendLine("questões no aparelho: $questoes")
        appendLine("temas: $temas · trilhas: $trilhas")
        if (!erroDoAcervo.isNullOrBlank()) appendLine("aviso na tela: $erroDoAcervo")
        ultimoErro(context)?.let { appendLine("último erro: $it") }
        if (relato.isNotBlank()) {
            appendLine()
            appendLine("O que aconteceu:")
            appendLine(relato.trim())
        }
    }

    /** Abre o compartilhamento do Android com o relato pronto. */
    fun compartilhar(context: Context, texto: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Relato do app Letícia")
            putExtra(Intent.EXTRA_TEXT, texto)
        }
        context.startActivity(
            Intent.createChooser(intent, "Enviar relato").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
