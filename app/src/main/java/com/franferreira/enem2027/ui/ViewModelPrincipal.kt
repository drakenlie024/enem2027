package com.franferreira.enem2027.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.franferreira.enem2027.data.model.Question
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
        }    }

    fun filtrarPorArea(area: String?) {
        _estado.value = _estado.value.copy(areaSelecionada = area, temaSelecionado = null)
        carregarSessao()
    }

    fun filtrarPorTema(tema: String?) {
        _estado.value = _estado.value.copy(temaSelecionado = tema)
        carregarSessao()
    }

    /**
     * Busca uma questão pelo id, no banco local.
     *
     * A tela de questão recebe só o ID: o `savedStateHandle` do Navigation
     * exige `Parcelable` ou `Serializable`, e a data class `Question` do
     * Kotlin não é nenhuma das duas — passar o objeto inteiro fechava o app.
     */
    suspend fun buscarQuestao(id: String): Question? = repo.questaoPorId(id)

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
            recarregar()
        }
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
