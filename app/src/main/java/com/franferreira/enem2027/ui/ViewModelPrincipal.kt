package com.franferreira.enem2027.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.franferreira.enem2027.data.local.AgendaEntity
import com.franferreira.enem2027.data.model.AgendaItem
import com.franferreira.enem2027.data.model.DiagnosticoSimulado
import com.franferreira.enem2027.data.model.AreaAulas
import com.franferreira.enem2027.data.model.AreasAula
import com.franferreira.enem2027.data.model.AulasDeArea
import com.franferreira.enem2027.data.model.Etapa
import com.franferreira.enem2027.data.model.ItemCaderno
import com.franferreira.enem2027.data.model.PacoteAula
import com.franferreira.enem2027.data.model.PrevisaoNota
import com.franferreira.enem2027.data.model.Previsor
import com.franferreira.enem2027.data.repo.AulasCache
import com.franferreira.enem2027.data.model.ProgressoResponse
import com.franferreira.enem2027.data.model.Question
import com.franferreira.enem2027.data.model.QuestaoPendente
import com.franferreira.enem2027.data.model.ResolverPendenteRequest
import com.franferreira.enem2027.data.model.RespostaSimulado
import com.franferreira.enem2027.data.model.SimuladoMontado
import kotlinx.serialization.encodeToString
import com.franferreira.enem2027.data.model.SimuladoRequest
import com.franferreira.enem2027.data.model.Trilha
import com.franferreira.enem2027.data.remote.ApiClient
import com.franferreira.enem2027.data.repo.FilaRevisao
import com.franferreira.enem2027.data.repo.RepertorioRedacao
import com.franferreira.enem2027.data.repo.Repositorio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EstadoHoje(
    val carregando: Boolean = true,
    val sincronizando: Boolean = false,
    /** O acervo embutido do APK esta sendo gravado no banco local. */
    val semeando: Boolean = false,
    val autenticado: Boolean = false,
    /** Nome que a aluna digitou ao começar; a saudação da tela Hoje usa. */
    val nome: String = "",
    val pendentes: Int = 0,
    /** Respostas salvas no aparelho que ainda não subiram (falta de rede). */
    val esperandoRede: Int = 0,
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
    /** Simulador em andamento. */
    val simulado: SimuladoMontado? = null,    val carregandoSimulado: Boolean = false,
    /** Quando o simulado em andamento começou (para retomar com o tempo certo). */
    val simuladoInicio: Long = 0L,
    /** Diagnóstico do último simulado encerrado. */
    val diagnostico: DiagnosticoSimulado? = null,
    /** Simulado encerrado, guardado para a revisão com gabarito. */
    val simuladoRevisao: SimuladoMontado? = null,
    /** Respostas dadas no simulado revisado (id, escolhida, gabarito, ...). */
    val respostasRevisao: List<Map<String, String>> = emptyList(),
    /** Tempo gasto em cada questão do simulado (id -> segundos). */
    val temposRevisao: Map<String, Int> = emptyMap(),
    val mensagemSimulado: String? = null,
    /** Fila de questões geradas por IA aguardando aprovação. */
    val filaAprovacao: List<QuestaoPendente> = emptyList(),
    // ---- video-aulas ----
    /** Áreas que têm video-aula. */
    val areasAula: List<AreaAulas> = emptyList(),
    /** Aulas da área aberta, agrupadas por tema. */
    val aulasDaArea: AulasDeArea = AulasDeArea(),
    /** A aula aberta (video, resumo e questões). */
    val pacoteAula: PacoteAula? = null,
    val carregandoAulas: Boolean = false,
    /** Rascunho da redação em edição (sobrevive ao process death). */
    val redacaoRascunho: RascunhoRedacao? = null,
    /** Histórico de redações do aparelho. */
    val historicoRedacoes: List<RedacaoSalva> = emptyList(),
    /** Correção em andamento (roda em segundo plano). */
    val corrigindoRedacao: Boolean = false,
    /** Erro da última tentativa de correção. */
    val erroRedacao: String? = null,
    /** Resultado da última correção (para a tela exibir). */
    val resultadoRedacao: com.franferreira.enem2027.data.model.EssayCorrection? = null,
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
    /** Meta semanal: dias de estudo por semana. */
    val metaSemanal: Int = 5,
    /** Dias desta semana (1 = segunda) em que já houve estudo. */
    val diasMarcados: Set<Int> = emptySet(),
    /** Previsão de nota (estimativa por TRI, com a faixa do ano). */
    val previsao: PrevisaoNota = PrevisaoNota(),
    /** Ano de referência para o corte de nota. */
    val anoCorte: Int = 2024,
    /** Nota que o usuário quer alcançar. */
    val notaAlvo: Int = 700,
    /** Caderno de erros: o que já foi errado. */
    val caderno: List<ItemCaderno> = emptyList(),
    /** Temas devidos hoje (treino espaçado por tema). */
    val treinos: List<com.franferreira.enem2027.data.model.TemaTreino> = emptyList(),
    /** Tema aberto para treinar. */
    val treinoAberto: com.franferreira.enem2027.data.model.TemaTreino? = null,
    /** Questões do tema aberto. */
    val treinoQuestoes: List<Question> = emptyList(),
    /** Texto da busca de questão. */
    val buscaTexto: String = "",
    /** Resultado da busca de questão no acervo local. */
    val resultadoBusca: List<Question> = emptyList(),
    /** Bloco de foco (pomodoro) em andamento. */
    val focoAtivo: Boolean = false,
    /** Segundos restantes do bloco de foco. */
    val focoRestante: Int = 0,
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

class ViewModelPrincipal(
    application: Application,
    private val savedStateHandle: androidx.lifecycle.SavedStateHandle,
) : AndroidViewModel(application) {

    private val repo = Repositorio(application)
    private val api = ApiClient.api

    private val _estado = MutableStateFlow(EstadoHoje())
    val estado: StateFlow<EstadoHoje> = _estado.asStateFlow()

    init {
        carregarMeta()
        semearAcervo()
        restaurarSessao()
        restaurarFoco()
        restaurarSimulado()
    }

    /** Reabre o simulado que estava em andamento quando o app foi fechado. */
    private fun restaurarSimulado() {
        val bruto = prefsLocais().getString("simulado_ativo", null) ?: return
        val inicio = prefsLocais().getLong("simulado_inicio", 0L)
        val simulado = runCatching {
            kotlinx.serialization.json.Json { ignoreUnknownKeys = true; explicitNulls = false }
                .decodeFromString(SimuladoMontado.serializer(), bruto)
        }.getOrNull() ?: return
        // Simulado antigo demais (mais de 12h) já não faz sentido retomar.
        if (inicio <= 0L || System.currentTimeMillis() - inicio > 12 * 60 * 60 * 1000L) {
            prefsLocais().edit().remove("simulado_ativo").remove("simulado_inicio").apply()
            return
        }
        _estado.value = _estado.value.copy(simulado = simulado, simuladoInicio = inicio)
    }

    /**
     * Semeia o acervo que vem no APK.
     *
     * Roda em paralelo com o login: nao depende de rede nem de token, e
     * enquanto o acervo entra a tela mostra que esta preparando.
     */
    private fun semearAcervo() {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(semeando = true)
            val quantas = runCatching { repo.semearAcervoEmbutido() }.getOrDefault(0)
            _estado.value = _estado.value.copy(semeando = false, anos = repo.anosLocais())
            // A tela do Hoje pode ter lido o banco ANTES da semeadura terminar.
            // Sem recarregar aqui ela fica dizendo "baixe o acervo" com as
            // 3.117 questões já gravadas no aparelho.
            recarregar()
            restaurarEstadoAberto()
            if (quantas > 0) {
                _estado.value = _estado.value.copy(
                    mensagem = "$quantas questões do ENEM já vieram no aplicativo."
                )
            }
            // Baixa as figuras em segundo plano: não bloqueia a primeira
            // abertura, e a tela usa a URL até o arquivo local chegar.
            viewModelScope.launch {
                runCatching { repo.baixarImagensDoAcervo() }
            }
        }
    }

    /**
     * Guarda o token localmente para não pedir login toda vez.
     */
    private fun restaurarSessao() {
        viewModelScope.launch {
            val prefs = getApplication<Application>()
                .getSharedPreferences("leticia", android.content.Context.MODE_PRIVATE)
            val token = prefs.getString("token", null)
            val modoLocal = prefs.getBoolean("modo_local", false)
            if (token.isNullOrBlank()) {
                // Sem token há dois casos: primeira abertura (mostra o login) e
                // conta local, criada só com o nome. No segundo, o estudo tem
                // de começar na hora: o acervo é do aparelho, não do servidor.
                _estado.value = _estado.value.copy(
                    carregando = false,
                    autenticado = modoLocal,
                    nome = prefs.getString("nome", "").orEmpty(),
                )
                if (modoLocal) {
                    recarregar()
                    carregarAgenda()
                    garantirContaRemota()
                }
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
                _estado.value = _estado.value.copy(
                    carregando = false,
                    autenticado = modoLocal,
                    nome = prefs.getString("nome", "").orEmpty(),
                )
                if (modoLocal) {
                    recarregar()
                    carregarAgenda()
                }
                return@launch
            }
            _estado.value = _estado.value.copy(
                carregando = false,
                autenticado = true,
                nome = prefs.getString("nome", "").orEmpty(),
            )
            recarregar()
            carregarAgenda()
            carregarFilaAprovacao()
            carregarRedacoes()
        }
    }

    private fun prefsLocais() = getApplication<Application>()
        .getSharedPreferences("leticia", android.content.Context.MODE_PRIVATE)

    /**
     * Começa a estudar só com o nome, sem e-mail nem senha.
     *
     * O requisito número um do app é funcionar offline: as 3.117 questões vêm
     * dentro do APK. Exigir conta (e internet) para chegar até elas deixava o
     * app inteiro atrás de uma tela de login que não tem o que fazer offline.
     *
     * A conta no servidor é criada sozinha, com e-mail e senha gerados, na
     * primeira vez que houver rede — é ela que faz o sync e a correção de
     * redação por IA funcionarem depois, sem pedir nada à aluna.
     */
    fun comecarSemConta(nome: String) {
        val limpo = nome.trim()
        if (limpo.isBlank()) return
        prefsLocais().edit()
            .putString("nome", limpo)
            .putBoolean("modo_local", true)
            .apply()
        _estado.value = _estado.value.copy(
            carregando = false,
            autenticado = true,
            nome = limpo,
        )
        recarregar()
        carregarAgenda()
        garantirContaRemota()
    }

    /** Cria (ou reencontra) a conta do servidor para uma conta local. */
    private fun garantirContaRemota() {
        if (!ApiClient.token.isNullOrBlank()) return
        viewModelScope.launch {
            val prefs = prefsLocais()
            val nome = prefs.getString("nome", null)?.takeIf { it.isNotBlank() } ?: return@launch
            val emailSalvo = prefs.getString("email_local", null)
            val senhaSalva = prefs.getString("senha_local", null)
            // example.com é reservado por norma (RFC 2606): o e-mail nunca
            // entrega nada e o backend aceita como endereço válido.
            val email = emailSalvo
                ?: "aluno-${java.util.UUID.randomUUID().toString().take(8)}@example.com"
            val senha = senhaSalva
                ?: java.util.UUID.randomUUID().toString().replace("-", "").take(24)

            val resposta = runCatching {
                if (emailSalvo == null) {
                    api.registrar(
                        com.franferreira.enem2027.data.model.RegisterRequest(email, senha, nome)
                    )
                } else {
                    api.entrar(com.franferreira.enem2027.data.model.LoginRequest(email, senha))
                }
            }.getOrNull() ?: return@launch

            ApiClient.token = resposta.accessToken
            prefs.edit()
                .putString("email_local", email)
                .putString("senha_local", senha)
                .putString("token", resposta.accessToken)
                .apply()
            // Com token, o que estava esperando na fila sobe agora.
            runCatching { repo.sincronizar() }
            recarregar()
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
                .putString("nome", resposta.name.ifBlank { nome })
                .putBoolean("modo_local", false)
                .apply()
            _estado.value = _estado.value.copy(
                autenticado = true,
                nome = resposta.name.ifBlank { nome },
            )
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
            var totalLocal = 0
            for (ano in anos) totalLocal += repo.totalLocal(ano)
            _estado.value = _estado.value.copy(
                carregando = false,
                anos = anos,
                areas = areas,
                temas = temas,
                totalLocal = totalLocal,
                pendentes = repo.pendentes(),
                esperandoRede = repo.esperandoRede(),
                respondidasHoje = repo.respondidasHoje(),
                metaSemanal = com.franferreira.enem2027.data.repo.MetaSemanal.meta(
                    getApplication()
                ),
                diasMarcados = com.franferreira.enem2027.data.repo.MetaSemanal.diasMarcados(
                    getApplication()
                ),
                xp = gam?.xp ?: 0,
                nivel = gam?.level ?: 1,
                streak = gam?.streak ?: 0,
            )
            carregarSessao()
            carregarTrilhas()
            carregarTreinos()
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
                    val agora = System.currentTimeMillis()
                    _estado.value = _estado.value.copy(
                        carregandoSimulado = false,
                        simulado = simulado,
                        simuladoInicio = agora,
                    )
                    // Guarda no aparelho: se o app morrer no meio da prova,
                    // a pessoa volta de onde parou em vez de perder tudo.
                    runCatching {
                        val texto = kotlinx.serialization.json.Json {
                            ignoreUnknownKeys = true
                            explicitNulls = false
                        }.encodeToString(SimuladoMontado.serializer(), simulado)
                        prefsLocais().edit()
                            .putString("simulado_ativo", texto)
                            .putLong("simulado_inicio", agora)
                            .apply()
                    }
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
        prefsLocais().edit().remove("simulado_ativo").remove("simulado_inicio").apply()
        _estado.value = _estado.value.copy(
            simulado = null, diagnostico = null, simuladoInicio = 0L,
            simuladoRevisao = null, respostasRevisao = emptyList(), temposRevisao = emptyMap(),
        )
    }

    fun finalizarSimulado(
        respostas: List<Map<String, String>>,
        duracaoSegundos: Int,
        tempos: Map<String, Int> = emptyMap(),
    ) {
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
                    prefsLocais().edit().remove("simulado_ativo").remove("simulado_inicio").apply()
                    _estado.value = _estado.value.copy(
                        simulado = null,
                        simuladoInicio = 0L,
                        diagnostico = diag,
                        // Guarda o simulado para a revisão com gabarito.
                        simuladoRevisao = simulado,
                        respostasRevisao = respostas,
                        temposRevisao = tempos,
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
            // O aparelho manda: é a agenda do Room que o alarme lê. Antes esta
            // função só lia o servidor — offline a tela abria com tudo
            // desmarcado e o "Salvar" apagava a agenda real.
            val local = runCatching { repo.agenda() }.getOrDefault(emptyList())
            if (local.isNotEmpty()) {
                _estado.value = _estado.value.copy(
                    agenda = local.map { it.weekday to it.period }.toSet(),
                )
                return@launch
            }
            // Aparelho novo (ou recém-instalado): aí sim vale o que o servidor
            // tem, e ele é gravado localmente para o alarme usar.
            runCatching { ApiClient.api.agenda() }
                .onSuccess { resposta ->
                    val itens = resposta["itens"] ?: emptyList<AgendaItem>()
                    val pares = itens.mapNotNull { item ->
                        item.period?.let { periodo -> item.weekday to periodo }
                    }.toSet()
                    if (pares.isEmpty()) return@onSuccess
                    _estado.value = _estado.value.copy(agenda = pares)
                    runCatching {
                        repo.salvarAgenda(
                            pares.mapIndexed { indice, (weekday, periodo) ->
                                AgendaEntity(
                                    id = (weekday * 10 + indice).toLong(),
                                    weekday = weekday,
                                    period = periodo,
                                    minutesTarget = 0,
                                )
                            },
                        )
                    }
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
        savedStateHandle["questao_aberta_id"] = questionId
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

    /** Reabre o que estava aberto antes do processo ser morto. */
    private fun restaurarEstadoAberto() {
        val qid = savedStateHandle.get<String>("questao_aberta_id")
        if (!qid.isNullOrBlank()) {
            abrirQuestao(qid)
        }
        val pid = savedStateHandle.get<String>("pacote_aula_id")
        if (!pid.isNullOrBlank()) {
            abrirPacoteAula(pid)
        }
    }
    fun fecharQuestao() {
        savedStateHandle.remove<String>("questao_aberta_id")
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
                esperandoRede = repo.esperandoRede(),
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

    /**
     * Corrige a redação pela IA.
     *
     * Roda em segundo plano: salva o rascunho primeiro (para o texto não se
     * perder se o app for morto no meio) e devolve imediatamente. O resultado
     * chega em `estado.resultadoRedacao` quando a IA responder, e a redação
     * corrigida é gravada no histórico do aparelho.
     */
    fun corrigirRedacao(tema: String, texto: String) {
        // Rascunho primeiro: o texto não pode depender da rede.
        salvarRascunhoRedacao(tema, texto)
        // Guarda a versão antes de mandar: a correção pode reescrever tudo.
        com.franferreira.enem2027.data.repo.VersoesRascunho.salvar(getApplication(), texto)
        _estado.value = _estado.value.copy(
            corrigindoRedacao = true,
            erroRedacao = null,
            resultadoRedacao = null,
        )
        viewModelScope.launch {
            val correcao = runCatching {
                ApiClient.api.corrigirRedacao(
                    com.franferreira.enem2027.data.model.EssayRequest(theme = tema, text = texto)
                )
            }.getOrNull()
            if (correcao == null) {
                _estado.value = _estado.value.copy(
                    corrigindoRedacao = false,
                    erroRedacao = "Não consegui corrigir agora. A IA pode estar ocupada — tente de novo em instantes.",
                )
                return@launch
            }
            val id = _estado.value.redacaoRascunho?.id
            if (id != null) {
                repo.registrarCorrecaoRedacao(id, correcao.nota, correcao.toString())
            }
            _estado.value = _estado.value.copy(
                corrigindoRedacao = false,
                resultadoRedacao = correcao,
                mensagem = "Redação corrigida: ${correcao.nota ?: "—"}",
            )
            carregarRedacoes()
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

    /** Muda a meta semanal (quantos dias por semana). */
    fun ajustarMetaSemanal(dias: Int) {
        com.franferreira.enem2027.data.repo.MetaSemanal.definirMeta(getApplication(), dias)
        _estado.value = _estado.value.copy(
            metaSemanal = com.franferreira.enem2027.data.repo.MetaSemanal.meta(getApplication()),
        )
    }

    // ---------- treino espaçado por tema ----------

    /** Carrega os temas que estão devidos hoje. */
    fun carregarTreinos() {
        viewModelScope.launch {
            val lista = runCatching { repo.temasParaTreinar() }.getOrDefault(emptyList())
            _estado.value = _estado.value.copy(treinos = lista)
        }
    }

    /** Abre um tema para treinar: serve questões novas dele. */
    fun abrirTreino(treino: com.franferreira.enem2027.data.model.TemaTreino) {
        viewModelScope.launch {
            val questoes = runCatching {
                repo.questoesDoTema(treino.area, treino.tema)
            }.getOrDefault(emptyList())
            _estado.value = _estado.value.copy(
                treinoAberto = treino,
                treinoQuestoes = questoes,
            )
        }
    }

    fun fecharTreino() {
        _estado.value = _estado.value.copy(treinoAberto = null, treinoQuestoes = emptyList())
        carregarTreinos()
    }

    /** Busca livre no acervo do aparelho (enunciado, contexto e tema). */
    fun buscarQuestoes(texto: String) {
        _estado.value = _estado.value.copy(buscaTexto = texto)
        if (texto.trim().length < 2) {
            _estado.value = _estado.value.copy(resultadoBusca = emptyList())
            return
        }
        viewModelScope.launch {
            val achadas = repo.buscarQuestoes(texto)
            _estado.value = _estado.value.copy(resultadoBusca = achadas)
        }
    }

    // ---------- foco (pomodoro) ----------

    /** Começa um bloco de foco de 25 minutos e agenda o aviso do fim. */
    fun iniciarFoco(minutos: Int = 25) {
        val prefs = prefsLocais()
        val inicio = System.currentTimeMillis()
        prefs.edit().putLong("foco_inicio", inicio).putInt("foco_min", minutos).apply()
        com.franferreira.enem2027.notificacao.Agendador.agendarFoco(getApplication(), minutos)
        _estado.value = _estado.value.copy(focoAtivo = true, focoRestante = minutos * 60)
        viewModelScope.launch {
            while (true) {
                val restante = ((inicio + minutos * 60_000L - System.currentTimeMillis()) / 1000).toInt()
                if (restante <= 0) break
                _estado.value = _estado.value.copy(focoRestante = restante)
                kotlinx.coroutines.delay(1000)
            }
            prefs.edit().remove("foco_inicio").remove("foco_min").apply()
            _estado.value = _estado.value.copy(focoAtivo = false, focoRestante = 0)
        }
    }

    fun pararFoco() {
        prefsLocais().edit().remove("foco_inicio").remove("foco_min").apply()
        com.franferreira.enem2027.notificacao.Agendador.cancelarFoco(getApplication())
        _estado.value = _estado.value.copy(focoAtivo = false, focoRestante = 0)
    }

    /** Retoma um bloco de foco que já estava rodando quando o app fechou. */
    private fun restaurarFoco() {
        val prefs = prefsLocais()
        val inicio = prefs.getLong("foco_inicio", 0L)
        val minutos = prefs.getInt("foco_min", 0)
        if (inicio <= 0L || minutos <= 0) return
        val restante = ((inicio + minutos * 60_000L - System.currentTimeMillis()) / 1000).toInt()
        if (restante <= 0) {
            prefs.edit().remove("foco_inicio").remove("foco_min").apply()
            return
        }
        _estado.value = _estado.value.copy(focoAtivo = true, focoRestante = restante)
        viewModelScope.launch {
            while (true) {
                val r = ((inicio + minutos * 60_000L - System.currentTimeMillis()) / 1000).toInt()
                if (r <= 0) break
                _estado.value = _estado.value.copy(focoRestante = r)
                kotlinx.coroutines.delay(1000)
            }
            prefs.edit().remove("foco_inicio").remove("foco_min").apply()
            _estado.value = _estado.value.copy(focoAtivo = false, focoRestante = 0)
        }
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

    // ---------- video-aulas ----------

    /** Carrega as areas que tem video-aula. */
    fun carregarAreasAula() {
        viewModelScope.launch(Dispatchers.IO) {
            _estado.value = _estado.value.copy(carregandoAulas = true)
            val app = getApplication<Application>()
            val resultado = runCatching {
                ApiClient.api.areasComAula().string()
            }
            val corpo = resultado.getOrNull()
            if (corpo != null) {
                AulasCache.guardar(app, "areas", corpo)
            }
            val areas = corpo?.let {
                runCatching {
                    kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                        .decodeFromString(AreasAula.serializer(), it)
                }.getOrNull()
            }
            ?: AulasCache.lerModelo<AreasAula>(app, "areas")
            _estado.value = _estado.value.copy(
                areasAula = areas?.areas ?: emptyList(),
                carregandoAulas = false,
            )
        }
    }

    /** Abre uma area: as aulas dela, agrupadas por tema. */
    fun carregarAulasDe(area: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _estado.value = _estado.value.copy(carregandoAulas = true, aulasDaArea = AulasDeArea())
            val app = getApplication<Application>()
            val corpo = runCatching { ApiClient.api.aulasDe(area).string() }.getOrNull()
            if (corpo != null) {
                AulasCache.guardar(app, "area-$area", corpo)
            }
            val dados = corpo?.let {
                runCatching {
                    kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                        .decodeFromString(AulasDeArea.serializer(), it)
                }.getOrNull()
            }
            ?: AulasCache.lerModelo<AulasDeArea>(app, "area-$area")
            _estado.value = _estado.value.copy(
                aulasDaArea = dados ?: AulasDeArea(),
                carregandoAulas = false,
            )
        }
    }

    /** Abre uma aula: video, resumo e questoes. */
    fun abrirPacoteAula(id: String) {
        savedStateHandle["pacote_aula_id"] = id
        viewModelScope.launch(Dispatchers.IO) {
            _estado.value = _estado.value.copy(carregandoAulas = true, pacoteAula = null)
            val app = getApplication<Application>()
            val corpo = runCatching { ApiClient.api.pacoteAula(id).string() }.getOrNull()
            if (corpo != null) {
                AulasCache.guardar(app, "pacote-$id", corpo)
            }
            val pacote = corpo?.let {
                runCatching {
                    kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                        .decodeFromString(PacoteAula.serializer(), it)
                }.getOrNull()
            }
            ?: AulasCache.lerModelo<PacoteAula>(app, "pacote-$id")
            _estado.value = _estado.value.copy(
                pacoteAula = pacote,
                carregandoAulas = false,
            )
        }
    }

    fun fecharAula() {
        savedStateHandle.remove<String>("pacote_aula_id")
        _estado.value = _estado.value.copy(pacoteAula = null)
    }
}
