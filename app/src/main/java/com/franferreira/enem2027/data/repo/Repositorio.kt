package com.franferreira.enem2027.data.repo

import android.content.Context
import com.franferreira.enem2027.data.local.AgendaEntity
import com.franferreira.enem2027.data.local.AcertoAreaRow
import com.franferreira.enem2027.data.local.EnemDatabase
import com.franferreira.enem2027.data.local.ErroRow
import com.franferreira.enem2027.data.local.EssayEntity
import com.franferreira.enem2027.data.local.GamificationEntity
import com.franferreira.enem2027.data.local.PendingAnswerEntity
import com.franferreira.enem2027.data.local.ProgressEntity
import com.franferreira.enem2027.data.local.TrilhaEtapaEntity
import com.franferreira.enem2027.data.local.JsonCompartilhado
import com.franferreira.enem2027.data.local.paraEntidade
import com.franferreira.enem2027.data.local.paraModelo
import com.franferreira.enem2027.data.model.CurriculoTrilhas
import com.franferreira.enem2027.data.model.EtapaConcluida
import com.franferreira.enem2027.data.model.ProgressBatch
import com.franferreira.enem2027.data.model.ProgressItem
import com.franferreira.enem2027.data.model.Question
import com.franferreira.enem2027.data.model.Trilha
import com.franferreira.enem2027.data.model.YearInfo
import com.franferreira.enem2027.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    companion object {
        /**
         * Máximo de itens por requisição de sync.
         *
         * O servidor recusa lotes maiores com 422. Sem este limite, um fim de
         * semana de estudo (mais de 200 respostas) travava o sync.
         */
        const val MAX_ITENS_POR_LOTE = 200
    }

    // ---------- acervo ----------

    suspend fun anosDisponiveis(): List<YearInfo> = runCatching { api.anos() }.getOrDefault(emptyList())

    /** Baixa o pacote de um ano e guarda localmente.
     *
     * O backend entrega no máximo 200 questões por requisição, então a
     * busca é paginada até o fim do ano. Pedir mais que isso dá 422.
     */
    suspend fun baixarAno(ano: Int): Result<Int> = runCatching {
        val todas = mutableListOf<Question>()
        var offset = 0
        var versao: String? = null
        var total = 0
        while (true) {
            val pagina = api.ano(ano, limit = 200, offset = offset)
            if (versao == null) versao = pagina.versao
            total = pagina.total
            val recebidas = pagina.questoes
            if (recebidas.isEmpty()) break
            todas += recebidas
            offset += recebidas.size
            if (recebidas.size < 200) break
            if (todas.size >= pagina.total && pagina.total > 0) break
        }
        // Substituir o ano é destrutivo: `substituirAno` apaga o que está no
        // aparelho e grava o que veio. Se o servidor devolver vazio (ano que
        // ele ainda não carregou, payload cortado), a resposta "vazia" não
        // pode apagar as ~180 questões do ano e deixar o SRS delas órfão.
        check(todas.isNotEmpty()) { "O servidor não devolveu nenhuma questão de $ano." }
        if (total > 0 && todas.size != total) {
            error("Vieram ${todas.size} de $total questões de $ano; nada foi substituído.")
        }
        val entidades = todas.map { it.paraEntidade(versao) }
        db.questions().substituirAno(ano, entidades)
        entidades.size
    }

    /** Anos que já estão no aparelho. */
    suspend fun anosLocais(): List<Int> = db.questions().anos()

    /**
     * Baixa em segundo plano as figuras referenciadas pelo acervo.
     *
     * O acervo embutido aponta ~980 URLs remotas; sem baixá-las o app não
     * mostra a figura offline. Roda em paralelo com o estudo: a tela usa a
     * URL enquanto o arquivo local não existe, e passa a usar o arquivo assim
     * que ele chega.
     */
    suspend fun baixarImagensDoAcervo() = withContext(Dispatchers.IO) {
        val urls = mutableListOf<String>()
        db.questions().todos().forEach { q ->
            runCatching {
                urls += JsonCompartilhado.decodeFromString<List<String>>(q.arquivosJson)
            }
            // alternativas podem ter figura (alternativa.arquivo)
            runCatching {
                val alts = JsonCompartilhado.decodeFromString<List<com.franferreira.enem2027.data.model.Alternative>>(q.alternativasJson)
                alts.mapNotNull { it.arquivo }.forEach { urls += it }
            }
        }
        ImagensAcervo.baixarTodas(context, urls)
    }

    /**
     * Semeia o acervo que vem DENTRO do APK.
     *
     * O app ja abre com as 17 provas (2009-2025) e a explicacao offline de
     * cada questao: nao ha download nem espera, e o app funciona offline desde
     * o primeiro clique.
     *
     * O servidor continua servindo o acervo para REBAIXAR um ano quando o
     * servidor corrige alguma coisa (a area de 15 questoes de 2024/2025, por
     * exemplo). Mas o caminho normal ja nao precisa de rede.
     *
     * Devolve quantas questoes foram semeadas.
     */
    suspend fun semearAcervoEmbutido(): Int = withContext(Dispatchers.IO) {
        val jaTem = db.questions().anos().toSet()
        var total = 0
        for (ano in AcervoEmbutido.anos(context)) {
            if (ano in jaTem) continue
            val pacote = AcervoEmbutido.carregar(context, ano) ?: continue
            if (pacote.questoes.isEmpty()) continue
            db.questions().substituirAno(
                ano,
                pacote.questoes.map { it.paraEntidade(pacote.versao) },
            )
            total += pacote.questoes.size
        }
        total
    }

    /**
     * Anos que o servidor tem mais novos que o aparelho.
     *
     * Acontece quando o acervo do servidor muda depois do download — por
     * exemplo, quando as explicações offline foram geradas. Sem esta
     * checagem, as questões já baixadas ficariam sem explicação até o
     * usuário rebaixar o ano na mão.
     */
    suspend fun anosDesatualizados(): List<Int> {
        val remotos = runCatching { api.anos() }.getOrDefault(emptyList())
        if (remotos.isEmpty()) return emptyList()
        val locais = db.questions().versoesLocais().associateBy { it.ano }
        return remotos.mapNotNull { info ->
            val local = locais[info.ano] ?: return@mapNotNull null
            val versaoLocal = local.versaoAcervo
            // Versão diferente (e o servidor sempre tem) = desatualizado.
            if (versaoLocal != null && versaoLocal != info.versao) info.ano else null
        }
    }

    /** Rebaixa os anos desatualizados, para trazer as explicações novas. */
    suspend fun atualizarAcervo(): Result<Int> = runCatching {
        val desatualizados = anosDesatualizados()
        var total = 0
        for (ano in desatualizados) {
            val r = baixarAno(ano)
            total += r.getOrDefault(0)
        }
        total
    }

    /**
     * Baixa as questões de IA que o usuário aprovou.
     *
     * Vêm separadas do acervo oficial porque a origem é diferente — o app
     * mostra "IA" na questão. Nunca se misturam.
     */
    suspend fun baixarQuestoesDeIA(): Result<Int> = runCatching {
        // Paginado: a API entrega no máximo 200 por requisição, e pedir só
        // a primeira página deixava as demais (quando houvesse mais de 200)
        // para sempre.
        var total = 0
        var offset = 0
        while (true) {
            val pacote = api.acervoDeIA(limit = 200, offset = offset)
            if (pacote.questoes.isEmpty()) break
            val entidades = pacote.questoes.map { it.paraEntidade(pacote.versao) }
            db.questions().inserir(entidades)
            total += entidades.size
            offset += pacote.questoes.size
            if (pacote.questoes.size < 200) break
        }
        total
    }

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

    suspend fun pendentes(): Int = db.questions().pendentes(System.currentTimeMillis())

    /**
     * Quantas questões foram respondidas hoje (para o anel da meta).
     *
     * A contagem é do dia LOCAL do aparelho: o usuário estuda à noite e o
     * "hoje" dele é o dia dele, não o do servidor.
     */
    suspend fun respondidasHoje(): Int {
        val inicioDoDia = java.time.LocalDate.now()
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return db.questions().respondidasHoje(inicioDoDia)
    }

    /**
     * A fila de revisão separada por estado de memória.
     *
     * - [FilaRevisao.vencendo]: o SRS mandou voltar agora.
     * - [FilaRevisao.novas]: nunca vistas.
     * - [FilaRevisao.erradas]: caíram (lapse) e ainda estão em relearning.
     */
    suspend fun filaRevisao(area: String?, tema: String?, limite: Int = 50): FilaRevisao {
        val agora = System.currentTimeMillis()
        val vencendo = mutableListOf<Question>()
        val novas = mutableListOf<Question>()
        val erradas = mutableListOf<Question>()

        db.questions().paraRevisao(area, tema, agora, limite * 2).forEach { entity ->
            val questao = entity.paraModelo()
            when (entity.progressState) {
                "seen" -> Unit
                "relearning" -> erradas += questao
                null, "new" -> novas += questao
                else -> vencendo += questao
            }
        }
        return FilaRevisao(
            vencendo = vencendo.take(limite),
            novas = novas.take(limite),
            erradas = erradas.take(limite),
        )
    }

    suspend fun questaoPorId(id: String): Question? =
        db.questions().porId(id)?.paraModelo()

    // ---------- caderno de erros e previsão de nota ----------

    /**
     * As questões que o usuário já errou, das mais recentes para as mais
     * antigas.
     *
     * É o histórico do que já foi errado — diferente da fila do SRS, que é
     * sobre o que vence agora. É aqui que o aprendizado se concentra.
     */
    suspend fun cadernoDeErros(limite: Int = 200): List<ErroRow> =
        db.progress().cadernoDeErros(limite)

    /** Acertos e erros por área, para a previsão de nota. */
    suspend fun acertosPorArea(): List<AcertoAreaRow> =
        db.progress().acertosPorArea()

    // ---------- trilhas ----------

    /**
     * Currículo das trilhas: vem do servidor (é estático) e é cacheado em
     * memória. Se a rede falhar, o app usa o currículo que já tem.
     */
    private var curriculoCache: CurriculoTrilhas? = null

    suspend fun curriculoTrilhas(): CurriculoTrilhas {
        curriculoCache?.let { return it }
        val baixado = runCatching { api.curriculoTrilhas() }.getOrNull()
        if (baixado != null) {
            curriculoCache = baixado
            return baixado
        }
        // Sem rede: um currículo vazio ainda deixa o app abrir e mostrar as
        // trilhas que ele conseguir montar localmente.
        return curriculoCache ?: CurriculoTrilhas()
    }

    /** Monta as trilhas com as questões do aparelho e o SRS local. */
    suspend fun trilhas(): List<Trilha> {
        val curriculo = curriculoTrilhas()
        importarProgressoDoServidor(curriculo)
        val montador = MontadorTrilhas(db)

        val stats = db.questions().statsPorTema().associate { it.tema to
            StatsTema(
                tema = it.tema,
                erros = it.erros,
                acertos = it.acertos,
                total = it.total,
                area = it.area,
            )
        }

        val etapasLocais = db.trilhaEtapas().todos()
        val concluidas = etapasLocais.groupBy { it.trilhaId }
            .mapValues { (_, lista) -> lista.map { it.etapa } }
        val resultados = etapasLocais.groupBy { it.trilhaId }
            .mapValues { (_, lista) ->
                lista.associate {
                    it.etapa to ResultadoEtapa(
                        acertos = it.acertos,
                        total = it.total,
                        segundos = it.segundos,
                        xp = it.xp,
                        tema = it.tema,
                    )
                }
            }

        // A montagem faz mais de 100 queries (uma por etapa e por tema de
        // apoio) e converte centenas de questões. Na thread principal, a
        // tela "Hoje" travava ao abrir depois de um sync.
        return withContext(Dispatchers.Default) {
            montador.montar(
                curriculo = curriculo,
                statsPorTema = stats,
                etapasConcluidas = concluidas,
                resultadoPorEtapa = resultados,
            )
        }
    }

    /**
     * Importa o progresso de etapas que o servidor tem.
     *
     * Sem isso, num aparelho novo (ou depois de um app apagado) as trilhas
     * apareceriam zeradas, mesmo com o progresso no servidor. Só entra o que
     * ainda não está aqui — o que o aparelho tem sempre ganha.
     */
    private suspend fun importarProgressoDoServidor(curriculo: CurriculoTrilhas) {
        if (curriculo.progresso.isEmpty()) return
        val locais = db.trilhaEtapas().todos().map { it.trilhaId to it.etapa }.toSet()
        val novas = curriculo.progresso.flatMap { (trilhaId, etapas) ->
            etapas.filterNot { (trilhaId to it) in locais }.map { trilhaId to it }
        }
        if (novas.isEmpty()) return
        for ((trilhaId, etapa) in novas) {
            db.trilhaEtapas().salvar(
                TrilhaEtapaEntity(
                    trilhaId = trilhaId,
                    etapa = etapa,
                    area = "",
                    tema = "",
                    acertos = 0,
                    total = 0,
                    segundos = 0,
                    xp = 0,
                    // Veio do servidor: não precisa ser reenviado.
                    synced = true,
                ),
            )
        }
    }

    /**
     * Registra o resultado de uma etapa concluída.
     *
     * Grava local na hora (a trilha já mostra a etapa como concluída, sem
     * esperar a rede) e enfileira para o servidor.
     *
     * Guarda a MELHOR tentativa, como o servidor faz. Com REPLACE, refazer
     * a etapa e piorar sobrescrevia o placar bom — e a divergência aparecia
     * justamente ao trocar de aparelho.
     */
    suspend fun registrarEtapa(
        trilhaId: String,
        etapa: Int,
        area: String,
        tema: String,
        acertos: Int,
        total: Int,
        segundos: Int,
        xp: Int,
    ) {
        val anterior = db.trilhaEtapas().porEtapa(trilhaId, etapa)
        val melhor = anterior == null || acertos >= anterior.acertos
        db.trilhaEtapas().salvar(
            TrilhaEtapaEntity(
                trilhaId = trilhaId,
                etapa = etapa,
                area = area,
                tema = tema,
                acertos = if (melhor) acertos else anterior.acertos,
                total = if (melhor) total else anterior.total,
                segundos = if (melhor) segundos else anterior.segundos,
                xp = if (melhor) xp else anterior.xp,
                synced = false,
            ),
        )
    }

    /** Envia as etapas concluídas que ainda não foram para o servidor. */
    suspend fun sincronizarEtapas(): Int {
        val pendentes = db.trilhaEtapas().naoSincronizadas()
        if (pendentes.isEmpty()) return 0
        var enviadas = 0
        for (etapa in pendentes) {
            val ok = runCatching {
                api.registrarEtapa(
                    EtapaConcluida(
                        trilhaId = etapa.trilhaId,
                        etapa = etapa.etapa,
                        area = etapa.area,
                        tema = etapa.tema,
                        acertos = etapa.acertos,
                        total = etapa.total,
                        segundos = etapa.segundos,
                        xp = etapa.xp,
                    ),
                )
            }.isSuccess
            if (ok) {
                db.trilhaEtapas().marcarSincronizado(etapa.trilhaId, etapa.etapa)
                enviadas += 1
            }
        }
        return enviadas
    }

    // ---------- respostas e SRS ----------

    /**
     * Registra uma resposta: grava local na hora (para o app responder sem
     * delay) e enfileira para o servidor.
     */
    suspend fun registrarResposta(
        questionId: String,
        correta: Boolean,
        segundos: Float?,
        confianca: String? = null,
    ) {
        // A quality segue a convenção do Anki: errou = 1, acertou fácil = 5.
        // Antes era sempre 4 (ou 1), então o único ramo que sobe o `ease` nunca
        // rodava: a facilidade só caía, o intervalo parava de crescer, e a
        // aluna revisava a mesma questão de novo cedo demais.
        val quality = if (!correta) {
            1
        } else if (confianca == "palpite" || confianca == "baixa") {
            3
        } else {
            5
        }
        val agora = System.currentTimeMillis()

        val atual = db.progress().porId(questionId)
        val novo = srsRevisar(
            atual ?: ProgressEntity(questionId = questionId),
            quality,
            agora,
        ).copy(lastConfidence = confianca)
        db.progress().upsert(novo)
        db.pendingAnswers().inserir(
            PendingAnswerEntity(
                questionId = questionId,
                quality = quality,
                correct = correta,
                secondsTaken = segundos,
                reviewedAt = agora,
                confianca = confianca,
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
        // limpar + inserir numa transação só: um crash no meio deixaria a
        // agenda vazia, e é dela que o alarme do lembrete lê.
        db.agenda().substituir(itens)
    }

    // ---------- redação ----------

    /**
     * Salva o rascunho da redação no aparelho e devolve o id.
     *
     * A redação é o maior texto que o usuário digita no app. Guardar só na
     * memória (`remember`) fazia ele se perder ao trocar de aba ou no
     * process death — e a correção pela IA leva minutos, tempo mais que
     * suficiente para o celular matar o app.
     */
    suspend fun salvarRascunho(tema: String, texto: String, id: String? = null): String {
        val idFinal = id ?: "red-${System.currentTimeMillis()}"
        val existente = db.essays().porId(idFinal)
        db.essays().salvar(
            EssayEntity(
                id = idFinal,
                theme = tema,
                prompt = null,
                text = texto,
                score = existente?.score,
                feedbackJson = existente?.feedbackJson,
                createdAt = existente?.createdAt ?: System.currentTimeMillis(),
                synced = existente?.synced ?: false,
            ),
        )
        return idFinal
    }

    /** Grava a nota e o feedback da IA no rascunho local. */
    suspend fun registrarCorrecaoRedacao(id: String, nota: Int?, feedbackJson: String?) {
        val existente = db.essays().porId(id) ?: return
        db.essays().salvar(
            existente.copy(score = nota, feedbackJson = feedbackJson),
        )
    }

    suspend fun redacoes(): List<EssayEntity> = db.essays().todos()

    suspend fun redacao(id: String): EssayEntity? = db.essays().porId(id)


    // ---------- sincronização ----------

    /**
     * Envia as respostas pendentes. Retorna quantas foram aceitas.
     * Não lança exceção: falha de sync não pode quebrar o estudo.
     */
    suspend fun sincronizar(): Result<Int> = runCatching {
        val pendentes = db.pendingAnswers().naoSincronizadas()
        if (pendentes.isEmpty()) return@runCatching 0

        // O servidor aceita no máximo 200 itens por requisição. Sem fatiar, um
        // fim de semana de estudo (mais de 200 respostas) estourava 422 e o
        // sync morria para sempre — e a mensagem dizia "sem conexão".
        var totalAplicado = 0
        var falhou: Throwable? = null
        var tudoAceito = true

        pendentes.chunked(MAX_ITENS_POR_LOTE).forEach { lote ->
            val itens = lote.map { resposta -> _itemDe(resposta) }
            try {
                val resposta = api.sincronizar(
                    ProgressBatch(
                        items = itens,
                        // O "hoje" do aparelho, para o servidor julgar o streak
                        // contra o dia de quem estudou.
                        localToday = LocalDate.now().toString(),
                    )
                )
                totalAplicado += resposta.applied
                // Marca só o que o servidor aceitou DE FATO. Antes o lote
                // inteiro saía da fila mesmo quando o servidor recusava parte
                // dele: as recusadas nunca eram reenviadas e sumiam caladas.
                val aceitas = resposta.applied + resposta.duplicates
                if (aceitas >= lote.size) {
                    db.pendingAnswers().marcarSincronizadas(lote.map { it.localId })
                } else {
                    // Aceitou parte: o lote continua pendente por inteiro. O
                    // reenvio é idempotente (o servidor deduplica por questão
                    // + horário), então repetir não infla XP.
                    tudoAceito = false
                    falhou = IllegalStateException(
                        "O servidor aceitou $aceitas de ${lote.size} respostas; " +
                            "as outras continuam na fila.",
                    )
                }
            } catch (erro: Throwable) {
                tudoAceito = false
                falhou = erro
                // Um item "envenenado" (question_id que saiu do acervo,
                // seconds_taken inválido) fazia o lote de 200 falhar para
                // sempre, e tudo o que vinha depois ficava atrás dele na fila.
                // Em erro de validação (4xx) o lote é refeito item por item,
                // para isolar o que o servidor recusa.
                if (erro is retrofit2.HttpException && erro.code() in 400..499) {
                    totalAplicado += _enviarUmPorUm(lote)
                }
                return@forEach
            }
        }
        db.pendingAnswers().limparSincronizadas()

        // Espelha a gamificação do servidor — mas só quando TODOS os lotes
        // foram aceitos. Espelhar com o sync pela metade rebaixava o XP que o
        // usuário já tinha ganho no aparelho.
        if (tudoAceito) {
            runCatching {
                val gam = api.gamificacao()
                val local = db.gamification().atual()
                db.gamification().salvar(
                    (local ?: GamificationEntity(id = 0)).copy(
                        xp = maxOf(local?.xp ?: 0, gam.xp),
                        level = maxOf(local?.level ?: 1, gam.level),
                        // Streak também não rebaixa: se o servidor calcular 0
                        // (fuso, dia sem resposta aplicada), a sequência da
                        // aluna voltava a zero no aparelho.
                        streak = maxOf(local?.streak ?: 0, gam.streak),
                        longestStreak = maxOf(local?.longestStreak ?: 0, gam.longestStreak),
                    ),
                )
            }
        }

        // Houve lote que não passou: o que passou já está salvo, o resto fica
        // pendente para a próxima tentativa.
        if (falhou != null) {
            throw IllegalStateException(
                "Só ${totalAplicado} de ${pendentes.size} respostas foram sincronizadas.",
                falhou,
            )
        }
        totalAplicado
    }

    // As conversões entre entity e modelo estão em `data/local/Conversores.kt`
    // — o montador de trilhas também precisa delas.

    /** Uma resposta pendente no formato que o servidor espera. */
    private suspend fun _itemDe(resposta: PendingAnswerEntity): ProgressItem {
        val questao = db.questions().porId(resposta.questionId)
        return ProgressItem(
            questionId = resposta.questionId,
            topic = questao?.tema,
            discipline = questao?.area,
            quality = resposta.quality,
            correct = resposta.correct,
            secondsTaken = resposta.secondsTaken,
            reviewedAt = Instant.ofEpochMilli(resposta.reviewedAt).toString(),
            // O dia local do aparelho: o streak é sobre "hoje" de quem
            // estudou, e não sobre o dia UTC do servidor.
            localDate = Instant.ofEpochMilli(resposta.reviewedAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toString(),
            // A confiança que a pessoa declarou ("sei" / "palpite").
            confidence = resposta.confianca,
        )
    }

    /**
     * Reenvia o lote item por item, depois de um 4xx no lote inteiro.
     *
     * O que o servidor aceitar sai da fila; o resto continua pendente. Serve
     * para um único item inválido não travar os outros 199 para sempre.
     */
    private suspend fun _enviarUmPorUm(lote: List<PendingAnswerEntity>): Int {
        var aceitas = 0
        for (pendente in lote) {
            val resposta = runCatching {
                api.sincronizar(
                    ProgressBatch(
                        items = listOf(_itemDe(pendente)),
                        localToday = LocalDate.now().toString(),
                    ),
                )
            }.getOrNull() ?: continue
            if (resposta.applied + resposta.duplicates > 0) {
                db.pendingAnswers().marcarSincronizadas(listOf(pendente.localId))
                aceitas += resposta.applied
            }
        }
        return aceitas
    }
}
