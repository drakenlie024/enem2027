package com.franferreira.enem2027.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.franferreira.enem2027.data.local.AgendaEntity
import com.franferreira.enem2027.data.model.AgendaItem
import com.franferreira.enem2027.data.model.DiagnosticoSimulado
import com.franferreira.enem2027.data.model.Etapa
import com.franferreira.enem2027.data.model.ItemCaderno
import com.franferreira.enem2027.data.model.PrevisaoNota
import com.franferreira.enem2027.data.model.Previsor
import com.franferreira.enem2027.data.model.ProgressoResponse
import com.franferreira.enem2027.data.model.Question
import com.franferreira.enem2027.data.model.QuestaoPendente
import com.franferreira.enem2027.data.model.ResolverPendenteRequest
import com.franferreira.enem2027.data.model.RespostaSimulado
import com.franferreira.enem2027.data.model.SimuladoMontado
import com.franferreira.enem2027.data.model.SimuladoRequest
import com.franferreira.enem2027.data.model.Trilha
import com.franferreira.enem2027.data.remote.ApiClient
import com.franferreira.enem2027.data.repo.FilaRevisao
import com.franferreira.enem2027.data.repo.RepertorioRedacao
import com.franferreira.enem2027.data.repo.Repositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EstadoHoje(
    val carregando: Boolean = true,
    val sincronizando: Boolean = false,
    val autenticado: Boolean = false,
    val pendentes: Int = 0,
    val totalLocal: Int = 0,
    val anos: List<Int> = emptyList(),
    val areas: List<String> = emptyList(),
    val temas: List<String> = emptyList(),
    val areaSelecionada: String? = null,
    val temaSelecionado: String? = null,
    val sessao: List<Question> = emptyList(),
    val fila: FilaRevisao = FilaRevisao(),
    /** Questão em foco na tela de revisão (null = nenhuma). */
    val questaoAberta: Question? = null,
    /**
     * Id da questão que está sendo carregada para abrir.
     *
     * Sem isso, abrir uma questão pelo caderno mostrava "Questão não
     * encontrada" por um instante: a rota compõe antes do banco responder.
     */
    val questaoPendente: String? = null,
    /** Evolução do usuário: por área, competência, dia e conquistas. */
    val progresso: ProgressoResponse = ProgressoResponse(),
    /** Períodos que o usuário marcou na agenda. */
    val agenda: Set<Pair<Int, String>> = emptySet(),
    /** Simulado em andamento. */
    val simulado: SimuladoMontado? = null,
    val carregandoSimulado: Boolean = false,
    /** Diagnóstico do último simulado encerrado. */
    val diagnostico: DiagnosticoSimulado? = null,
    val mensagemSimulado: String? = null,
    /** Fila de questões geradas por IA aguardando aprovação. */
    val filaAprovacao: List<QuestaoPendente> = emptyList(),
    /** Rascunho da redação em edição (sobrevive ao process death). */
    val redacaoRascunho: RascunhoRedacao? = null,
    /** Histórico de redações do aparelho. */
    val historicoRedacoes: List<RedacaoSalva> = emptyList(),
    /** Questões respondidas hoje (é o que o anel da meta mostra). */
    val respondidasHoje: Int = 0,
    val xp: Int = 0,
    val nivel: Int = 1,
    val streak: Int = 0,
    val mensagem: String? = null,
    /** Trilhas montadas localmente (currículo do servidor + acervo do aparelho). */
    val trilhas: List<Trilha> = emptyList(),
    /** Trilha que o usuário está vendo em detalhe. */
    val trilhaAberta: Trilha? = null,
    /** Etapa em andamento (12 questões). */
    val etapaEmAndamento: Etapa? = null,
    /** Resultado da última etapa concluída, para o resumo numérico. */
    val resumoEtapa: ResumoEtapa? = null,
    /** Meta diária de questões (sugerida, ajustável). */
    val metaDiaria: Int = 12,
    /** Previsão de nota (estimativa por TRI, com a faixa do ano). */
    val previsao: PrevisaoNota = PrevisaoNota(),
    /** Ano de referência para o corte de nota. */
    val anoCorte: Int = 2024,
    /** Nota que o usuário quer alcançar. */
    val notaAlvo: Int = 700,
    /** Caderno de erros: o que já foi errado. */
    val caderno: List<ItemCaderno> = emptyList(),
) {
    /**
     * A trilha que o botão principal da tela Hoje deve atacar.
     *
     * A adaptativa ("Para você") tem prioridade — é a de maior valor, porque
     * é montada com o que o usuário mais erra. Depois vem a primeira trilha
     * de área que ainda tem etapa por fazer.
     */
    val trilhaParaJogar: Trilha?
        get() {
            val adaptativa = trilhas.firstOrNull { it.adaptativa }
            if (adaptativa?.etapaAtual?.jogavel == true) return adaptativa
            return trilhas.firstOrNull { !it.adaptativa && it.etapaAtual?.jogavel == true }
        }
}

/** O rascunho da redação em edição. Vive no Room, não só na memória. */
data class RascunhoRedacao(
    val id: String,
    val tema: String,
    val texto: String,
)

/** Uma redação já corrigida, no histórico do aparelho. */
data class RedacaoSalva(
    val id: String,
    val tema: String,
    val texto: String,
    val nota: Int?,
)

/** O resumo de uma etapa concluída: os números que o usuário vê. */
data class ResumoEtapa(
    val trilhaId: String,
    val etapa: Int,
    val tema: String,
    val acertos: Int,
    val total: Int,
    val segundos: Int,
    val xp: Int,
) {
    val placar: String get() = "$acertos/$total"
    val tempo: String
        get() = when {
            segundos <= 0 -> "—"
            segundos < 60 -> "${segundos}s"
            else -> "${segundos / 60}min ${segundos % 60}s"
        }
}

class ViewModelPrincipal(application: Application) : AndroidViewModel(application) {

    private val repo = Repositorio(application)
    private val api = ApiClient.api

    private val _estado = MutableStateFlow(EstadoHoje())
    val estado: StateFlow<EstadoHoje> = _estado.asStateFlow()

    init {
        carregarMeta()
        restaurarSessao()
    }

    /**
     * Guarda o token localmente para não pedir login toda vez.
     */
    private fun restaurarSessao() {
        viewModelScope.launch {
            val prefs = getApplication<Application>()
                .getSharedPreferences("leticia", android.content.Context.MODE_PRIVATE)
            val token = prefs.getString("token", null)
            if (token.isNullOrBlank()) {
                _estado.value = _estado.value.copy(carregando = false, autenticado = false)
                return@launch
            }
            ApiClient.token = token
            // Confere se o token ainda vale. SÓ desloga em 401: erro de rede
            // não é token inválido, e deslogar ali matava o app offline na
            // primeira abertura sem internet.
            val erro = runCatching { api.gamificacao() }.exceptionOrNull()
            if (erro is retrofit2.HttpException && erro.code() == 401) {
                ApiClient.token = null
                prefs.edit().remove("token").apply()
                _estado.value = _estado.value.copy(carregando = false, autenticado = false)
                return@launch
            }
            _estado.value = _estado.value.copy(carregando = false, autenticado = true)
            recarregar()
            carregarAgenda()
            carregarFilaAprovacao()
            carregarRedacoes()
        }
    }

    /** Faz login ou cadastro. Devolve null em caso de sucesso, ou a mensagem de erro. */
    suspend fun autenticar(
        email: String,
        senha: String,
        nome: String,
        criar: Boolean,
    ): String? {
        return runCatching {
            val resposta = if (criar) {
                ApiClient.api.registrar(
                    com.franferreira.enem2027.data.model.RegisterRequest(email, senha, nome)
                )
            } else {
                ApiClient.api.entrar(
                    com.franferreira.enem2027.data.model.LoginRequest(email, senha)
                )
            }
            ApiClient.token = resposta.accessToken
            getApplication<Application>()
                .getSharedPreferences("leticia", android.content.Context.MODE_PRIVATE)
                .edit()
                .putString("token", resposta.accessToken)
                .apply()
            _estado.value = _estado.value.copy(autenticado = true)
            recarregar()
            null
        }.getOrElse { erro ->
            when (erro) {
                is retrofit2.HttpException -> when (erro.code()) {
                    401 -> "E-mail ou senha incorretos"
                    409 -> "Já existe conta com esse e-mail"
                    429 -> "Muitas tentativas. Aguarde um pouco."
                    else -> "Falha no servidor (${erro.code()})"
                }
                else -> "Não consegui falar com o servidor. Tente de novo."
            }
        }
    }

    fun recarregar() {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(carregando = true)
            val anos = repo.anosLocais()
            val areas = repo.areasLocais()
            val temas = repo.temasLocais()
            val gam = repo.gamificacao()
            _estado.value = _estado.value.copy(
                carregando = false,
                anos = anos,
                areas = areas,
                temas = temas,
                totalLocal = anos.sumOf { repo.totalLocal(it) },
                pendentes = repo.pendentes(),
                respondidasHoje = repo.respondidasHoje(),
                xp = gam?.xp ?: 0,
                nivel = gam?.level ?: 1,
                streak = gam?.streak ?: 0,
            )
            carregarSessao()
            carregarTrilhas()
        }
    }

    fun carregarSessao() {
        viewModelScope.launch {
            val s = _estado.value
            val sessao = repo.sessao(s.areaSelecionada, s.temaSelecionado, limite = 20)
            val fila = repo.filaRevisao(s.areaSelecionada, s.temaSelecionado, limite = 50)
            _estado.value = _estado.value.copy(
                sessao = sessao,
                fila = fila,
                pendentes = repo.pendentes(),
            )
            carregarProgresso()
        }
    }

    /** Carrega a evolução. Falha de rede não pode virar tela em branco. */
    private fun carregarProgresso() {
        viewModelScope.launch {
            runCatching { ApiClient.api.progresso(dias = 30) }
                .onSuccess { dados ->
                    _estado.value = _estado.value.copy(progresso = dados)
                }
        }
    }

    // ---------- simulados ----------

    fun gerarSimulado(tipo: String, area: String?, ano: Int?, quantidade: Int) {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(carregandoSimulado = true, mensagemSimulado = null)
            runCatching {
                ApiClient.api.gerarSimulado(
                    SimuladoRequest(
                        tipo = tipo,
                        area = area,
                        ano = ano,
                        quantidade = quantidade,
                    )
                )
            }
                .onSuccess { simulado ->
                    _estado.value = _estado.value.copy(
                        carregandoSimulado = false,
                        simulado = simulado,
                    )
                }
                .onFailure {
                    _estado.value = _estado.value.copy(
                        carregandoSimulado = false,
                        mensagemSimulado = "Não consegui montar o simulado. Tente de novo.",
                    )
                }
        }
    }

    fun fecharSimulado() {
        _estado.value = _estado.value.copy(simulado = null, diagnostico = null)
    }

    fun finalizarSimulado(respostas: List<Map<String, String>>, duracaoSegundos: Int) {
        val simulado = _estado.value.simulado ?: return
        viewModelScope.launch {
            runCatching {
                ApiClient.api.diagnosticoSimulado(
                    RespostaSimulado(
                        simuladoId = simulado.id,
                        tipo = simulado.tipo,
                        label = simulado.label,
                        duracaoSegundos = duracaoSegundos,
                        respostas = respostas,
                        // O total vai separado: `respostas` só traz o que foi
                        // respondido, e sem isto o servidor usaria 8/8 = 100%
                        // quando o simulado tinha 20 questões.
                        totalQuestions = simulado.questoes.size,
                    )
                )
            }
                .onSuccess { diag ->
                    _estado.value = _estado.value.copy(
                        simulado = null,
                        diagnostico = diag,
                    )
                }
                .onFailure {
                    _estado.value = _estado.value.copy(
                        simulado = null,
                        mensagemSimulado = "Não consegui fechar o simulado no servidor.",
                    )
                }
        }
    }

    // ---------- fila de aprovação ----------

    private fun carregarFilaAprovacao() {
        viewModelScope.launch {
            runCatching { ApiClient.api.filaDeAprovacao() }
                .onSuccess { resposta ->
                    _estado.value = _estado.value.copy(
                        filaAprovacao = (resposta["fila"] ?: emptyList()).map { dto ->
                            QuestaoPendente(
                                id = dto.id,
                                tema = dto.payload.tema,
                                enunciado = dto.payload.enunciado,
                                alternativas = dto.payload.alternativas,
                                gabarito = dto.payload.gabarito,
                                explicacao = dto.payload.explicacao,
                                confianca = dto.confidence,
                            )
                        },
                    )
                }
        }
    }

    fun resolverAprovacao(id: String, aprovar: Boolean) {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.resolverAprovacao(id, ResolverPendenteRequest(approved = aprovar))
            }
                .onSuccess {
                    _estado.value = _estado.value.copy(
                        mensagem = if (aprovar) "Questão aprovada" else "Questão rejeitada",
                    )
                    carregarFilaAprovacao()
                }
                .onFailure {
                    _estado.value = _estado.value.copy(
                        mensagem = "Não consegui resolver a aprovação. Tente de novo.",
                    )
                }
        }
    }

    // ---------- agenda ----------

    private fun carregarAgenda() {
        viewModelScope.launch {
            runCatching { ApiClient.api.agenda() }
                .onSuccess { resposta ->
                    val itens = resposta["itens"] ?: emptyList<AgendaItem>()
                    _estado.value = _estado.value.copy(
                        agenda = itens.mapNotNull { item ->
                            item.period?.let { periodo -> item.weekday to periodo }
                        }.toSet(),
                    )
                }
        }
    }

    fun alternarAgenda(weekday: Int, periodo: String) {
        val atual = _estado.value.agenda
        val novo = if ((weekday to periodo) in atual) {
            atual - (weekday to periodo)
        } else {
            atual + (weekday to periodo)
        }
        _estado.value = _estado.value.copy(agenda = novo)
    }

    fun salvarAgenda() {
        viewModelScope.launch {
            val itens = _estado.value.agenda.map { (weekday, periodo) ->
                AgendaItem(weekday = weekday, period = periodo)
            }

            // Grava LOCAL primeiro. O agendador do alarme lê a tabela Room
            // (não o servidor), e ela nunca era escrita — então o alarme
            // saía vazio e a agenda "não fazia nada". Gravar local primeiro
            // também faz a agenda funcionar offline.
            runCatching {
                repo.salvarAgenda(
                    itens.mapIndexed { indice, item ->
                        AgendaEntity(
                            // Id determinístico: com id = 0, todas as linhas
                            // colavam na MESMA linha da tabela (o REPLACE
                            // do Room sobrescrevia), e a semana montada
                            // sumia — inclusive a que o alarme lia.
                            id = (item.weekday * 10 + indice).toLong(),
                            weekday = item.weekday,
                            period = item.period,
                            minutesTarget = item.minutesTarget,
                        )
                    }
                )
            }

            runCatching { ApiClient.api.salvarAgenda(mapOf("itens" to itens)) }
                .onSuccess {
                    _estado.value = _estado.value.copy(
                        mensagem = "Agenda salva: ${itens.size} períodos",
                    )
                    // Reagenda o lembrete com os novos períodos.
                    com.franferreira.enem2027.notificacao.Agendador.agendar(
                        getApplication<Application>()
                    )
                }
                .onFailure {
                    // Salvou localmente; só o servidor não respondeu.
                    _estado.value = _estado.value.copy(
                        mensagem = "Agenda salva neste aparelho (${itens.size} períodos). " +
                            "O servidor não respondeu — ela sincroniza depois.",
                    )
                    com.franferreira.enem2027.notificacao.Agendador.agendar(
                        getApplication<Application>()
                    )
                }
        }
    }

    fun filtrarPorArea(area: String?) {
        _estado.value = _estado.value.copy(areaSelecionada = area, temaSelecionado = null)
        carregarSessao()
    }

    fun filtrarPorTema(tema: String?) {
        _estado.value = _estado.value.copy(temaSelecionado = tema)
        carregarSessao()
    }

    /**
     * Abre uma questão na tela de revisão.
     *
     * A tela recebe só o ID: o `savedStateHandle` do Navigation exige
     * `Parcelable` ou `Serializable`, e a data class `Question` do Kotlin não
     * é nenhuma das duas — passar o objeto inteiro fechava o app.
     */
    fun abrirQuestao(questionId: String) {
        // Marca o Pending antes de sair para a corrotina: a rota sabe que
        // está carregando em vez de jurar que a questão não existe.
        _estado.value = _estado.value.copy(questaoPendente = questionId)
        viewModelScope.launch {
            val questao = repo.questaoPorId(questionId)
            _estado.value = _estado.value.copy(
                questaoAberta = questao,
                questaoPendente = null,
            )
        }
    }

    fun baixarAcervo(ano: Int) {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(sincronizando = true, mensagem = null)
            val resultado = repo.baixarAno(ano)
            _estado.value = _estado.value.copy(
                sincronizando = false,
                mensagem = resultado.fold(
                    onSuccess = { "$ano baixado: $it questões" },
                    onFailure = { erro ->
                        val detalhe = when (erro) {
                            is retrofit2.HttpException -> when (erro.code()) {
                                422 -> "O servidor recusou o pedido (422)."
                                401 -> "Sua sessão expirou. Entre de novo."
                                404 -> "Não achei o acervo de $ano."
                                else -> "Erro ${erro.code()} do servidor."
                            }
                            else -> erro.message ?: "Falha ao baixar $ano."
                        }
                        "Não consegui baixar $ano: $detalhe"
                    },
                ),
            )
            recarregar()
        }
    }

    /** Baixa as questões de IA aprovadas (vêm separadas do acervo oficial). */
    fun baixarQuestoesDeIA() {
        viewModelScope.launch {
            repo.baixarQuestoesDeIA()
                .onSuccess { total ->
                    if (total > 0) {
                        _estado.value = _estado.value.copy(
                            mensagem = "$total questões de IA baixadas",
                        )
                        recarregar()
                    }
                }
        }
    }

    fun sincronizar() {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(sincronizando = true, mensagem = null)
            val resultado = repo.sincronizar()
            _estado.value = _estado.value.copy(
                sincronizando = false,
                mensagem = resultado.fold(
                    onSuccess = { "$it respostas sincronizadas" },
                    onFailure = { "Sem conexão — suas respostas ficam salvas no aparelho" },
                ),
            )
            // Aproveita o mesmo momento para pegar as questões de IA novas.
            baixarQuestoesDeIA()
            recarregar()
        }
    }

    fun fecharQuestao() {
        _estado.value = _estado.value.copy(questaoAberta = null)
    }

    fun responder(
        questionId: String,
        correta: Boolean,
        segundos: Float?,
        confianca: String? = null,
    ) {
        viewModelScope.launch {
            repo.registrarResposta(questionId, correta, segundos, confianca)
            val gam = repo.gamificacao()
            _estado.value = _estado.value.copy(
                pendentes = repo.pendentes(),
                xp = gam?.xp ?: 0,
                nivel = gam?.level ?: 1,
                streak = gam?.streak ?: 0,
            )
            carregarSessao()
            // A previsão de nota muda a cada resposta — recalcula para a
            // tela "Nota" mostrar o número de agora, não o de antes.
            carregarPrevisao()
        }
    }

    suspend fun pedirExplicacao(questao: Question): String? {
        return runCatching {
            ApiClient.api.explicar(
                com.franferreira.enem2027.data.model.ExplainRequest(
                    question = questao,
                    userAnswer = null,
                )
            ).explicacao
        }.getOrNull()
    }

    // ---------- redação ----------

    val temasRedacao: List<RepertorioRedacao.Tema>
        get() = RepertorioRedacao.temas

    suspend fun corrigirRedacao(
        tema: String,
        texto: String,
    ): Result<com.franferreira.enem2027.data.model.EssayCorrection> = runCatching {
        ApiClient.api.corrigirRedacao(
            com.franferreira.enem2027.data.model.EssayRequest(theme = tema, text = texto)
        )
    }.onSuccess { correcao ->
        // Prende a correção ao rascunho local: assim o texto e a nota ficam
        // juntos no aparelho, e o `essay_id` do servidor (que só existe no
        // servidor) não é o que identifica a redação aqui.
        val id = _estado.value.redacaoRascunho?.id
        if (id != null) {
            registrarCorrecaoRedacao(id, correcao)
        }
    }

    /**
     * Guarda o rascunho da redação no aparelho.
     *
     * A correção pela IA leva minutos — tempo mais que suficiente para o
     * celular matar o app e o texto ir embora. Com o rascunho salvo, o
     * trabalho não se perde.
     */
    fun salvarRascunhoRedacao(tema: String, texto: String) {
        viewModelScope.launch {
            val id = repo.salvarRascunho(tema, texto, _estado.value.redacaoRascunho?.id)
            _estado.value = _estado.value.copy(
                redacaoRascunho = RascunhoRedacao(id = id, tema = tema, texto = texto),
            )
        }
    }

    /** Carrega o rascunho e o histórico de redações do aparelho. */
    fun carregarRedacoes() {
        viewModelScope.launch {
            val lista = repo.redacoes()
            val rascunho = lista.firstOrNull { it.score == null }?.let {
                RascunhoRedacao(id = it.id, tema = it.theme, texto = it.text)
            }
            _estado.value = _estado.value.copy(
                redacaoRascunho = rascunho,
                historicoRedacoes = lista.map {
                    RedacaoSalva(id = it.id, tema = it.theme, texto = it.text, nota = it.score)
                },
            )
        }
    }

    /** Grava o resultado da correção no rascunho local. */
    fun registrarCorrecaoRedacao(id: String, correcao: com.franferreira.enem2027.data.model.EssayCorrection) {
        viewModelScope.launch {
            repo.registrarCorrecaoRedacao(
                id = id,
                nota = correcao.nota,
                feedbackJson = correcao.toString(),
            )
            carregarRedacoes()
        }
    }

    fun limparMensagem() {
        _estado.value = _estado.value.copy(mensagem = null)
    }

    /**
     * Rebaixa os anos cujo acervo no servidor mudou.
     *
     * Sem isso, as questões já baixadas ficariam sem a explicação offline
     * (que só existia depois da primeira leva de explicações) até o usuário
     * rebaixar o ano na mão.
     */
    fun atualizarAcervo() {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(sincronizando = true, mensagem = null)
            val resultado = repo.atualizarAcervo()
            _estado.value = _estado.value.copy(
                sincronizando = false,
                mensagem = resultado.fold(
                    onSuccess = { total ->
                        if (total > 0) {
                            "Acervo atualizado: $total questões com explicação nova"
                        } else {
                            "Acervo já está atualizado"
                        }
                    },
                    onFailure = { "Não consegui atualizar o acervo. Tente quando houver internet." },
                ),
            )
            recarregar()
        }
    }

    /**
     * Ajusta a meta diária de questões.
     *
     * A meta é sugerida, mas o usuário manda: 12 por dia é o padrão, mas
     * quem tem prova em menos de um ano pode querer 30, e quem estuda em
     * dia de semana talvez queira 6. Fica salvo no aparelho.
     */
    fun ajustarMeta(delta: Int) {
        val nova = (_estado.value.metaDiaria + delta).coerceIn(4, 60)
        if (nova == _estado.value.metaDiaria) return
        _estado.value = _estado.value.copy(metaDiaria = nova)
        getApplication<Application>()
            .getSharedPreferences("leticia", android.content.Context.MODE_PRIVATE)
            .edit()
            .putInt("meta_diaria", nova)
            .apply()
    }

    /** Carrega a meta salva (ou a sugerida). */
    private fun carregarMeta() {
        val prefs = getApplication<Application>()
            .getSharedPreferences("leticia", android.content.Context.MODE_PRIVATE)
        val meta = prefs.getInt("meta_diaria", 12)
        _estado.value = _estado.value.copy(metaDiaria = meta.coerceIn(4, 60))
    }

    // ---------- trilhas ----------

    /** Carrega as trilhas (montadas localmente, com o acervo do aparelho). */
    fun carregarTrilhas() {
        viewModelScope.launch {
            val trilhas = repo.trilhas()
            // A trilha ABERTA é re-derivada da lista nova: sem isso, quem
            // conclui uma etapa de dentro da tela de trilha volta e vê o
            // mesmo nó pulsando como "atual", e pode refazê-la à toa.
            val aberta = _estado.value.trilhaAberta?.let { atual ->
                trilhas.firstOrNull { it.id == atual.id } ?: atual
            }
            _estado.value = _estado.value.copy(trilhas = trilhas, trilhaAberta = aberta)
        }
    }

    fun abrirTrilha(trilha: Trilha) {
        _estado.value = _estado.value.copy(trilhaAberta = trilha)
    }

    fun fecharTrilha() {
        _estado.value = _estado.value.copy(trilhaAberta = null)
    }

    /** Começa uma etapa: as 12 questões entram em modo simulado. */
    fun iniciarEtapa(trilha: Trilha, etapa: Etapa) {
        _estado.value = _estado.value.copy(
            trilhaAberta = trilha,
            etapaEmAndamento = etapa,
            resumoEtapa = null,
        )
    }

    fun fecharEtapa() {
        _estado.value = _estado.value.copy(etapaEmAndamento = null)
    }

    /**
     * Conclui a etapa: grava o resultado, soma o XP e mostra o resumo.
     *
     * O resumo é o que o usuário vê: 7/8, o tempo e o XP ganho.
     */
    fun concluirEtapa(acertos: Int, segundos: Int) {
        val etapa = _estado.value.etapaEmAndamento ?: return
        val trilha = _estado.value.trilhaAberta ?: return
        val total = etapa.questoes.size
        val xp = acertos * 10

        viewModelScope.launch {
            repo.registrarEtapa(
                trilhaId = trilha.id,
                etapa = etapa.indice,
                area = trilha.area,
                tema = etapa.nome,
                acertos = acertos,
                total = total,
                segundos = segundos,
                xp = xp,
            )
            // Envia as etapas pendentes junto com a sincronização normal.
            runCatching { repo.sincronizarEtapas() }

            _estado.value = _estado.value.copy(
                etapaEmAndamento = null,
                resumoEtapa = ResumoEtapa(
                    trilhaId = trilha.id,
                    etapa = etapa.indice,
                    tema = etapa.nome,
                    acertos = acertos,
                    total = total,
                    segundos = segundos,
                    xp = xp,
                ),
            )
            carregarTrilhas()
        }
    }

    fun fecharResumo() {
        _estado.value = _estado.value.copy(resumoEtapa = null)
    }

    // ---------- previsão de nota e caderno de erros ----------

    /**
     * Recalcula a previsão de nota a partir do que o usuário já respondeu.
     *
     * É local de propósito: o SRS vive no aparelho, e a previsão precisa
     * refletir as respostas de hoje — não as de quando o servidor sincronizou.
     */
    fun carregarPrevisao() {
        viewModelScope.launch {
            val acertos = repo.acertosPorArea()
            val previsao = Previsor.prever(
                acertosPorArea = acertos,
                anoAlvo = _estado.value.anoCorte,
                notaAlvo = _estado.value.notaAlvo,
            )
            _estado.value = _estado.value.copy(previsao = previsao)
        }
    }

    /** Troca o ano de referência do corte de nota. */
    fun trocarAnoCorte(ano: Int) {
        _estado.value = _estado.value.copy(anoCorte = ano)
        carregarPrevisao()
    }

    /** Troca a nota-alvo. */
    fun trocarNotaAlvo(nota: Int) {
        _estado.value = _estado.value.copy(notaAlvo = nota)
        carregarPrevisao()
    }

    /** Carrega o caderno de erros. */
    fun carregarCaderno() {
        viewModelScope.launch {
            val itens = repo.cadernoDeErros().map { ItemCaderno.de(it) }
            _estado.value = _estado.value.copy(caderno = itens)
        }
    }
}
