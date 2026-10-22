package com.franferreira.enem2027.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.franferreira.enem2027.data.model.AgendaItem
import com.franferreira.enem2027.data.model.DiagnosticoSimulado
import com.franferreira.enem2027.data.model.ProgressoResponse
import com.franferreira.enem2027.data.model.Question
import com.franferreira.enem2027.data.model.QuestaoPendente
import com.franferreira.enem2027.data.model.ResolverPendenteRequest
import com.franferreira.enem2027.data.model.RespostaSimulado
import com.franferreira.enem2027.data.model.SimuladoMontado
import com.franferreira.enem2027.data.model.SimuladoRequest
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
    val xp: Int = 0,
    val nivel: Int = 1,
    val streak: Int = 0,
    val mensagem: String? = null,
)

class ViewModelPrincipal(application: Application) : AndroidViewModel(application) {

    private val repo = Repositorio(application)
    private val api = ApiClient.api

    private val _estado = MutableStateFlow(EstadoHoje())
    val estado: StateFlow<EstadoHoje> = _estado.asStateFlow()

    init {
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
            // Confere se o token ainda vale.
            val valido = runCatching { api.gamificacao() }.isSuccess
            if (!valido) {
                ApiClient.token = null
                prefs.edit().remove("token").apply()
                _estado.value = _estado.value.copy(carregando = false, autenticado = false)
                return@launch
            }
            _estado.value = _estado.value.copy(carregando = false, autenticado = true)
            recarregar()
            carregarAgenda()
            carregarFilaAprovacao()
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
                xp = gam?.xp ?: 0,
                nivel = gam?.level ?: 1,
                streak = gam?.streak ?: 0,
            )
            carregarSessao()
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
            runCatching { ApiClient.api.salvarAgenda(itens) }
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
                    _estado.value = _estado.value.copy(
                        mensagem = "Não consegui salvar a agenda. Ela fica salva só neste aparelho por enquanto.",
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
        viewModelScope.launch {
            val questao = repo.questaoPorId(questionId)
            _estado.value = _estado.value.copy(questaoAberta = questao)
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

    fun responder(questionId: String, correta: Boolean, segundos: Float?) {        viewModelScope.launch {
            repo.registrarResposta(questionId, correta, segundos)
            val gam = repo.gamificacao()
            _estado.value = _estado.value.copy(
                pendentes = repo.pendentes(),
                xp = gam?.xp ?: 0,
                nivel = gam?.level ?: 1,
                streak = gam?.streak ?: 0,
            )
            carregarSessao()
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
    }

    fun limparMensagem() {
        _estado.value = _estado.value.copy(mensagem = null)
    }
}
