package com.franferreira.enem2027.data.repo

import android.content.Context
import com.franferreira.enem2027.data.local.AgendaEntity
import com.franferreira.enem2027.data.local.EnemDatabase
import com.franferreira.enem2027.data.local.GamificationEntity
import com.franferreira.enem2027.data.local.PendingAnswerEntity
import com.franferreira.enem2027.data.local.ProgressEntity
import com.franferreira.enem2027.data.local.QuestionEntity
import com.franferreira.enem2027.data.model.Alternative
import com.franferreira.enem2027.data.model.ProgressBatch
import com.franferreira.enem2027.data.model.ProgressItem
import com.franferreira.enem2027.data.model.Question
import com.franferreira.enem2027.data.model.YearInfo
import com.franferreira.enem2027.data.remote.ApiClient
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Repositório único do app.
 *
 * O app é offline-first: TUDO é lido do banco local. A rede só entra para
 * (a) baixar o acervo e (b) enviar o progresso acumulado. Se a rede falhar,
 * o app continua funcionando — só avisa que não conseguiu sincronizar.
 */
class Repositorio(private val context: Context) {

    private val db = EnemDatabase.get(context)
    private val api = ApiClient.api
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    // ---------- acervo ----------

    suspend fun anosDisponiveis(): List<YearInfo> = runCatching { api.anos() }.getOrDefault(emptyList())

    /** Baixa o pacote de um ano e guarda localmente. */
    suspend fun baixarAno(ano: Int): Result<Int> = runCatching {
        val pacote = api.ano(ano, limit = 300)
        val entidades = pacote.questoes.map { it.paraEntidade(pacote.versao) }
        db.questions().substituirAno(ano, entidades)
        entidades.size
    }

    suspend fun anosLocais(): List<Int> = db.questions().anos()

    suspend fun totalLocal(ano: Int): Int = db.questions().totalDoAno(ano)

    suspend fun areasLocais(): List<String> = db.questions().areas()

    suspend fun temasLocais(): List<String> = db.questions().temas()

    /** Sessão de hoje: o que vence no SRS, filtrado por área/tema escolhidos. */
    suspend fun sessao(area: String?, tema: String?, limite: Int = 20): List<Question> {
        val agora = System.currentTimeMillis()
        return db.questions()
            .paraRevisao(area, tema, agora, limite)
            .map { it.paraModelo() }
    }

    suspend fun pendentes(): Int = db.progress().pendentes(System.currentTimeMillis())

    suspend fun questaoPorId(id: String): Question? =
        db.questions().porId(id)?.paraModelo()

    // ---------- respostas e SRS ----------

    /**
     * Registra uma resposta: grava local na hora (para o app responder sem
     * delay) e enfileira para o servidor.
     */
    suspend fun registrarResposta(
        questionId: String,
        correta: Boolean,
        segundos: Float?,
    ) {
        // A quality segue a convenção do Anki: errou = 1, acertou fácil = 5.
        val quality = if (correta) 4 else 1
        val agora = System.currentTimeMillis()

        val atual = db.progress().porId(questionId)
        val novo = srsRevisar(
            atual ?: ProgressEntity(questionId = questionId),
            quality,
            agora,
        )
        db.progress().upsert(novo)
        db.pendingAnswers().inserir(
            PendingAnswerEntity(
                questionId = questionId,
                quality = quality,
                correct = correta,
                secondsTaken = segundos,
                reviewedAt = agora,
            ),
        )
        atualizarGamificacaoLocal(correta)
    }

    /** Portão do SRS, espelhando a lógica do backend (SM-2 com facilidade). */
    private fun srsRevisar(atual: ProgressEntity, quality: Int, agora: Long): ProgressEntity {
        var ease = atual.ease
        var interval = atual.intervalDays
        var repetitions = atual.repetitions
        var lapses = atual.lapses
        var state = atual.state
        var dueAt = atual.dueAt

        if (quality < 3) {
            ease = maxOf(1.3f, ease - 0.2f)
            if (state == "review" || state == "learning") lapses += 1
            repetitions = 0
            state = if (state == "new") "learning" else "relearning"
            interval = 10f / (24 * 60)
            dueAt = agora + 10 * 60 * 1000L
        } else {
            if (quality == 5) ease = minOf(3.0f, ease + 0.1f)
            else if (quality == 3) ease = maxOf(1.3f, ease - 0.15f)

            when (state) {
                "new" -> {
                    state = "learning"
                    repetitions = 1
                    interval = 1f / (24 * 60)
                    dueAt = agora + 60 * 1000L
                }
                "learning", "relearning" -> {
                    repetitions += 1
                    if (repetitions >= 2) {
                        state = "review"
                        interval = maxOf(1f, if (interval > 1f) interval * ease else 1f)
                        dueAt = agora + (interval * 24 * 60 * 60 * 1000).toLong()
                    } else {
                        interval = 10f / (24 * 60)
                        dueAt = agora + 10 * 60 * 1000L
                    }
                }
                else -> {
                    repetitions += 1
                    val fator = ease / 2.5f
                    interval = when (quality) {
                        5 -> maxOf(interval * 1.3f * fator, 4f)
                        3 -> maxOf(interval * 1.2f * fator, 1f)
                        else -> maxOf(interval + 1f, 1f)
                    }.coerceAtMost(365f)
                    state = "review"
                    dueAt = agora + (interval * 24 * 60 * 60 * 1000).toLong()
                }
            }
        }

        return atual.copy(
            ease = ease,
            intervalDays = interval,
            repetitions = repetitions,
            lapses = lapses,
            state = state,
            dueAt = dueAt,
            lastReviewedAt = agora,
            lastQuality = quality,
            correctCount = atual.correctCount + if (corretaPelaQuality(quality)) 1 else 0,
            wrongCount = atual.wrongCount + if (!corretaPelaQuality(quality)) 1 else 0,
        )
    }

    private fun corretaPelaQuality(quality: Int) = quality >= 3

    // ---------- gamificação ----------

    private suspend fun atualizarGamificacaoLocal(correta: Boolean) {
        val atual = db.gamification().atual()
            ?: GamificationEntity(id = 0, xp = 0, level = 1, streak = 0, longestStreak = 0)
        val hoje = LocalDate.now()
        val hojeIso = hoje.toString()
        val streak = when {
            atual.lastStudyDate == hojeIso -> atual.streak
            atual.lastStudyDate == hoje.minusDays(1).toString() -> atual.streak + 1
            else -> 1
        }
        val xp = atual.xp + if (correta) 10 else 2
        db.gamification().salvar(
            atual.copy(
                xp = xp,
                level = nivelPorXp(xp),
                streak = streak,
                longestStreak = maxOf(atual.longestStreak, streak),
                lastStudyDate = hojeIso,
            ),
        )
    }

    private fun nivelPorXp(xp: Int): Int {
        var nivel = 1
        var restante = xp
        while (restante >= 200 * nivel) {
            restante -= 200 * nivel
            nivel += 1
        }
        return nivel
    }

    suspend fun gamificacao() = db.gamification().atual()

    // ---------- agenda ----------

    suspend fun agenda(): List<AgendaEntity> = db.agenda().todos()

    suspend fun salvarAgenda(itens: List<AgendaEntity>) {
        db.agenda().limpar()
        db.agenda().salvar(itens)
    }

    // ---------- sincronização ----------

    /**
     * Envia as respostas pendentes. Retorna quantas foram aceitas.
     * Não lança exceção: falha de sync não pode quebrar o estudo.
     */
    suspend fun sincronizar(): Result<Int> = runCatching {
        val pendentes = db.pendingAnswers().naoSincronizadas()
        if (pendentes.isEmpty()) return@runCatching 0

        val itens = pendentes.map { resposta ->
            val questao = db.questions().porId(resposta.questionId)
            ProgressItem(
                questionId = resposta.questionId,
                topic = questao?.tema,
                discipline = questao?.area,
                quality = resposta.quality,
                correct = resposta.correct,
                secondsTaken = resposta.secondsTaken,
                reviewedAt = Instant.ofEpochMilli(resposta.reviewedAt).toString(),
            )
        }
        val resposta = api.sincronizar(ProgressBatch(itens))
        db.pendingAnswers().marcarSincronizadas(pendentes.map { it.localId })
        db.pendingAnswers().limparSincronizadas()

        // Espelha a gamificação do servidor (streak é mais confiável lá,
        // porque o fuso do celular pode errar).
        val gam = api.gamificacao()
        val local = db.gamification().atual()
        db.gamification().salvar(
            (local ?: GamificationEntity(id = 0)).copy(
                xp = gam.xp,
                level = gam.level,
                streak = gam.streak,
                longestStreak = gam.longestStreak,
            ),
        )
        resposta.applied
    }

    // ---------- conversões ----------

    private fun Question.paraEntidade(versao: String?): QuestionEntity = QuestionEntity(
        id = id,
        ano = ano,
        indice = indice,
        area = area,
        idioma = idioma,
        origem = origem,
        enunciado = enunciado,
        contexto = contexto,
        arquivosJson = json.encodeToString(arquivos),
        alternativasJson = json.encodeToString(alternativas),
        gabarito = gabarito,
        tema = tema,
        competencia = competencia,
        dificuldade = dificuldade,
        subassunto = subassunto,
        versaoAcervo = versao,
    )

    private fun QuestionEntity.paraModelo(): Question = Question(
        id = id,
        ano = ano,
        indice = indice,
        area = area,
        idioma = idioma,
        origem = origem,
        enunciado = enunciado,
        contexto = contexto,
        arquivos = json.decodeFromString<List<String>>(arquivosJson),
        alternativas = json.decodeFromString<List<Alternative>>(alternativasJson),
        gabarito = gabarito,
        tema = tema,
        competencia = competencia,
        dificuldade = dificuldade,
        subassunto = subassunto,
    )
}
