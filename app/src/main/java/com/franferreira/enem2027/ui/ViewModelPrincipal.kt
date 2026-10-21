package com.franferreira.enem2027.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.franferreira.enem2027.data.model.Question
import com.franferreira.enem2027.data.remote.ApiClient
import com.franferreira.enem2027.data.repo.Repositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EstadoHoje(
    val carregando: Boolean = true,
    val sincronizando: Boolean = false,
    val pendentes: Int = 0,
    val totalLocal: Int = 0,
    val anos: List<Int> = emptyList(),
    val areas: List<String> = emptyList(),
    val temas: List<String> = emptyList(),
    val areaSelecionada: String? = null,
    val temaSelecionado: String? = null,
    val sessao: List<Question> = emptyList(),
    val xp: Int = 0,
    val nivel: Int = 1,
    val streak: Int = 0,
    val mensagem: String? = null,
)

class ViewModelPrincipal(application: Application) : AndroidViewModel(application) {

    private val repo = Repositorio(application)

    private val _estado = MutableStateFlow(EstadoHoje())
    val estado: StateFlow<EstadoHoje> = _estado.asStateFlow()

    init {
        recarregar()
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
            _estado.value = _estado.value.copy(
                sessao = sessao,
                pendentes = repo.pendentes(),
            )
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

    fun baixarAcervo(ano: Int) {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(sincronizando = true, mensagem = null)
            val resultado = repo.baixarAno(ano)
            _estado.value = _estado.value.copy(
                sincronizando = false,
                mensagem = resultado.fold(
                    onSuccess = { "$ano baixado: $it questões" },
                    onFailure = { "Falha ao baixar $ano: ${it.message}" },
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

    fun responder(questionId: String, correta: Boolean, segundos: Float?) {
        viewModelScope.launch {
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

    fun limparMensagem() {
        _estado.value = _estado.value.copy(mensagem = null)
    }
}
