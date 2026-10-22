package com.franferreira.enem2027.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Questão como o app usa. Espelha o JSON do acervo. */
@Serializable
data class Question(
    val id: String,
    @SerialName("ano") val ano: Int = 0,
    @SerialName("indice") val indice: Int = 0,
    @SerialName("area") val area: String? = null,
    @SerialName("idioma") val idioma: String? = null,
    @SerialName("enunciado") val enunciado: String? = null,
    @SerialName("contexto") val contexto: String? = null,
    @SerialName("arquivos") val arquivos: List<String> = emptyList(),
    @SerialName("alternativas") val alternativas: List<Alternative> = emptyList(),
    @SerialName("gabarito") val gabarito: String? = null,
    @SerialName("tema") val tema: String? = null,
    @SerialName("competencia") val competencia: String? = null,
    @SerialName("dificuldade") val dificuldade: String? = null,
    @SerialName("subassunto") val subassunto: String? = null,
    @SerialName("origem") val origem: String = "enem",
    /**
     * Explicação offline, gerada no servidor quando o acervo foi montado.
     *
     * Vem junto com o pacote do ano e fica no aparelho: o app explica por que
     * a alternativa correta é a correta sem precisar chamar a IA.
     */
    @SerialName("explicacao") val explicacao: String? = null,
) {
    val temaExibicao: String
        get() = tema?.takeIf { it.isNotBlank() } ?: subassunto?.takeIf { it.isNotBlank() }
            ?: area ?: "Revisão"

    val oficial: Boolean get() = origem == "enem"

    /** Tem explicação offline? */
    val temExplicacao: Boolean get() = !explicacao.isNullOrBlank()
}

@Serializable
data class Alternative(
    /**
     * `texto` pode vir `null` na fonte (questão de figura, por exemplo).
     * Com `coerceInputValues = true` no Json, o null vira "" em vez de
     * estourar o parse do pacote inteiro.
     */
    @SerialName("letra") val letra: String = "",
    @SerialName("texto") val texto: String = "",
    @SerialName("arquivo") val arquivo: String? = null,
    @SerialName("correta") val correta: Boolean = false,
)

@Serializable
data class YearInfo(
    @SerialName("ano") val ano: Int,
    @SerialName("versao") val versao: String? = null,
    @SerialName("origem") val origem: String? = null,
    @SerialName("total") val total: Int = 0,
)

/** Pacote de um ano, como o backend serve. */
@Serializable
data class YearPackage(
    @SerialName("ano") val ano: Int,
    @SerialName("versao") val versao: String? = null,
    @SerialName("origem") val origem: String? = null,
    @SerialName("total") val total: Int = 0,
    @SerialName("questoes") val questoes: List<Question> = emptyList(),
)

// --- auth ---

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(val email: String, val password: String, val name: String)

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
    @SerialName("user_id") val userId: String,
    val name: String = "",
)

// --- sync ---

@Serializable
data class ProgressItem(
    @SerialName("question_id") val questionId: String,
    val topic: String? = null,
    val discipline: String? = null,
    val quality: Int? = null,
    val correct: Boolean? = null,
    @SerialName("seconds_taken") val secondsTaken: Float? = null,
    @SerialName("reviewed_at") val reviewedAt: String? = null,
    /**
     * O dia local do aparelho (YYYY-MM-DD), no fuso de quem estudou.
     *
     * O streak é sobre "hoje" e "ontem" — e "hoje" é do usuário, não do
     * servidor. Sem isso, uma resposta às 21h em Brasília (UTC-3) vira
     * "amanhã" no servidor, e o streak conta o dia errado.
     */
    @SerialName("local_date") val localDate: String? = null,
)

@Serializable
data class ProgressBatch(
    val items: List<ProgressItem>,
    /**
     * O "hoje" do aparelho (YYYY-MM-DD), no fuso de quem estuda.
     *
     * O streak é julgado contra este dia. Sem ele, o servidor usaria o dia
     * UTC — e uma resposta às 21h em Brasília (UTC-3) cairia no dia seguinte,
     * quebrando a sequência.
     */
    @SerialName("local_today") val localToday: String? = null,
)

@Serializable
data class SyncResponse(
    val applied: Int = 0,
    val duplicates: Int = 0,
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    @SerialName("due_today") val dueToday: Int = 0,
)

@Serializable
data class GamificationResponse(
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    @SerialName("longest_streak") val longestStreak: Int = 0,
)

// --- progresso ---

@Serializable
data class AreaStats(
    val area: String = "",
    val total: Int = 0,
    val respondidas: Int = 0,
    val acertos: Int = 0,
    val taxa: Double = 0.0,
    val pendentes: Int = 0,
)

@Serializable
data class CompetenciaStats(
    val competencia: String = "",
    val respondidas: Int = 0,
    val acertos: Int = 0,
    val taxa: Double = 0.0,
)

@Serializable
data class ProgressoResponse(
    val xp: Int = 0,
    val nivel: Int = 1,
    val streak: Int = 0,
    @SerialName("longest_streak") val longestStreak: Int = 0,
    @SerialName("total_respondidas") val totalRespondidas: Int = 0,
    @SerialName("total_acertos") val totalAcertos: Int = 0,
    @SerialName("taxa_geral") val taxaGeral: Double = 0.0,
    val pendentes: Int = 0,
    @SerialName("por_area") val porArea: List<AreaStats> = emptyList(),
    @SerialName("por_competencia") val porCompetencia: List<CompetenciaStats> = emptyList(),
    @SerialName("por_dia") val porDia: List<Int> = emptyList(),
    val conquistas: List<String> = emptyList(),
)

/** Item da agenda: quando o usuário pode estudar. */
@Serializable
data class AgendaItem(
    val id: String? = null,
    val weekday: Int,
    val period: String,
    @SerialName("minutes_target") val minutesTarget: Int = 30,
)

// --- simulados ---

@Serializable
data class SimuladoRequest(
    val tipo: String = "area",
    val ano: Int? = null,
    val area: String? = null,
    val quantidade: Int = 20,
)

@Serializable
data class SimuladoQuestao(
    val id: String = "",
    val indice: Int = 0,
    val area: String? = null,
    val tema: String? = null,
    val enunciado: String? = null,
    val contexto: String? = null,
    val arquivos: List<String> = emptyList(),
    val alternativas: List<Alternative> = emptyList(),
    val gabarito: String? = null,
    /** Explicação offline, vinda junto com o simulado montado. */
    @SerialName("explicacao") val explicacao: String? = null,
)

@Serializable
data class SimuladoMontado(
    val id: String = "",
    val tipo: String = "",
    val label: String = "",
    val ano: Int? = null,
    val area: String? = null,
    @SerialName("duracao_minutos") val duracaoMinutos: Int = 0,
    val questoes: List<SimuladoQuestao> = emptyList(),
    @SerialName("diagnostico_previo") val diagnosticoPrevio: String? = null,
)

@Serializable
data class RespostaSimulado(
    @SerialName("simulado_id") val simuladoId: String = "",
    val tipo: String = "",
    val label: String = "",
    @SerialName("duracao_segundos") val duracaoSegundos: Int = 0,
    val respostas: List<Map<String, String>> = emptyList(),
    /**
     * Quantas questões o simulado tinha.
     *
     * Sem isto, o servidor calcula o denominador pelo que chegou (só as
     * respondidas) — encerrar com 8 de 20 dava "8/8, 100%".
     */
    @SerialName("total_questions") val totalQuestions: Int = 0,
)

@Serializable
data class TemaFraco(
    val tema: String = "",
    val erros: Int = 0,
    val total: Int = 0,
    val taxa: Double = 0.0,
)

@Serializable
data class DiagnosticoSimulado(
    val total: Int = 0,
    val acertos: Int = 0,
    val taxa: Double = 0.0,
    @SerialName("por_area") val porArea: Map<String, AreaStatsSimulado> = emptyMap(),
    @SerialName("temas_fracos") val temasFracos: List<TemaFraco> = emptyList(),
    val plano: List<String> = emptyList(),
)

@Serializable
data class AreaStatsSimulado(
    val total: Int = 0,
    val acertos: Int = 0,
    val taxa: Double = 0.0,
)

// --- fila de aprovação (questões geradas por IA) ---

/**
 * Resposta crua do backend: o payload vem aninhado.
 */
@Serializable
data class QuestaoPendenteDto(
    val id: String = "",
    val confidence: Double = 0.0,
    val payload: QuestaoPendentePayloadDto = QuestaoPendentePayloadDto(),
)

@Serializable
data class QuestaoPendentePayloadDto(
    val tema: String = "",
    val enunciado: String = "",
    val contexto: String = "",
    val alternativas: List<Alternative> = emptyList(),
    val gabarito: String = "",
    val explicacao: String = "",
    val competencia: String = "",
    val dificuldade: String = "",
)

@Serializable
data class ResolverPendenteRequest(val approved: Boolean)

/** Questão da fila de aprovação, já achatada para a tela. */
data class QuestaoPendente(
    val id: String,
    val tema: String = "",
    val enunciado: String = "",
    val alternativas: List<Alternative> = emptyList(),
    val gabarito: String = "",
    val explicacao: String = "",
    val confianca: Double = 0.0,
)

// --- IA ---

@Serializable
data class ExplainRequest(val question: Question, @SerialName("user_answer") val userAnswer: String? = null)

@Serializable
data class ExplainResponse(val explicacao: String = "")

@Serializable
data class EssayRequest(
    val theme: String,
    val text: String,
    val prompt: String? = null,
)

@Serializable
data class EssayCorrection(
    @SerialName("essay_id") val essayId: String? = null,
    val nota: Int = 0,
    @SerialName("nota_min") val notaMin: Int = 0,
    @SerialName("nota_max") val notaMax: Int = 0,
    val confianca: String = "media",
    val divergencia: Int = 0,
    @SerialName("usado_validador") val usadoValidador: Boolean = false,
    val resumo: String = "",
    val competencias: List<Competencia> = emptyList(),
    val avisos: List<String> = emptyList(),
    @SerialName("avaliadores") val avaliadores: List<Avaliador> = emptyList(),
)

@Serializable
data class Competencia(
    val numero: Int,
    val nota: Int = 0,
    val maximo: Int = 200,
    @SerialName("notas_individuais") val notasIndividuais: List<Int> = emptyList(),
    val feedback: String = "",
)

@Serializable
data class Avaliador(
    val modelo: String = "",
    val nota: Int = 0,
)
