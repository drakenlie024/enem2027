package com.franferreira.enem2027.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Questão baixada do acervo. Guarda tudo local para funcionar offline.
 *
 * A origem importa: [origem] é "enem" (oficial, Inep/enem.dev) ou "ia"
 * (gerada). O app mostra isso na tela — o gabarito de questão oficial nunca
 * vem da IA.
 */
@Entity(
    tableName = "questions",
    indices = [Index("ano"), Index("area"), Index("tema"), Index("origem")],
)
data class QuestionEntity(
    @PrimaryKey val id: String,
    val ano: Int,
    val indice: Int,
    val area: String?,
    val idioma: String?,
    val origem: String,          // "enem" | "ia"
    val enunciado: String?,
    val contexto: String?,
    val arquivosJson: String,    // lista de URLs de figuras
    val alternativasJson: String,
    val gabarito: String?,
    val tema: String?,
    val competencia: String?,
    val dificuldade: String?,
    val subassunto: String?,
    /** Explicação offline (gerada no servidor). Vem junto com o acervo. */
    val explicacao: String? = null,
    val versaoAcervo: String?,
    val baixadoEm: Long = System.currentTimeMillis(),
    /**
     * Estado do SRS dessa questão, preenchido pela query de revisão
     * (LEFT JOIN na tabela progress). Null = nunca respondida.
     */
    val progressState: String? = null,
)

/** Estado de repetição espaçada de uma questão, por usuário. */
@Entity(
    tableName = "progress",
    primaryKeys = ["questionId"],
    indices = [Index("dueAt"), Index("state")],
)
data class ProgressEntity(
    val questionId: String,
    val ease: Float = 2.5f,
    val intervalDays: Float = 0f,
    val repetitions: Int = 0,
    val lapses: Int = 0,
    val state: String = "new",   // new | learning | review | relearning | seen
    val dueAt: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long? = null,
    val lastQuality: Int? = null,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    /** Se ainda não foi sincronizado com o servidor. */
    val synced: Boolean = false,
)

/** Configuração de agenda: quando o usuário pode estudar. */
@Entity(tableName = "agenda", primaryKeys = ["id"])
data class AgendaEntity(
    val id: Long = 0,
    val weekday: Int,            // 0 = segunda ... 6 = domingo
    val period: String,          // manha | tarde | noite
    val minutesTarget: Int = 30,
)

/** Progresso de gamificação local (espelha o servidor). */
@Entity(tableName = "gamification", primaryKeys = ["id"])
data class GamificationEntity(
    val id: Long = 0,
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val longestStreak: Int = 0,
    val lastStudyDate: String? = null,
    val unlockedJson: String = "[]",
    val synced: Boolean = false,
)

/** Redação do usuário. */
@Entity(tableName = "essays", indices = [Index("createdAt")])
data class EssayEntity(
    @PrimaryKey val id: String,
    val theme: String,
    val prompt: String?,
    val text: String,
    val score: Int? = null,
    val feedbackJson: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
)

/** Fila de respostas feitas offline, aguardando sync. */
@Entity(tableName = "pending_answers")
data class PendingAnswerEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val questionId: String,
    val quality: Int,
    val correct: Boolean,
    val secondsTaken: Float?,
    val reviewedAt: Long,
    val synced: Boolean = false,
)

/** Progresso acumulado de uma etapa de trilha. */
@Entity(tableName = "trilha_etapas", primaryKeys = ["trilhaId", "etapa"])
data class TrilhaEtapaEntity(
    val trilhaId: String,
    val etapa: Int,
    val area: String = "",
    val tema: String = "",
    val acertos: Int = 0,
    val total: Int = 0,
    val segundos: Int = 0,
    val xp: Int = 0,
    val concluidaEm: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
)

/**
 * Estatísticas por tema, vindas de uma query agregada.
 *
 * Não é uma tabela: é o resultado do GROUP BY por tema que monta a trilha
 * adaptativa "Para você".
 */
data class TemaStatsEntity(
    val tema: String,
    val area: String?,
    val acertos: Int,
    val erros: Int,
    val total: Int,
)

/** Simulado feito pelo usuário. */
@Entity(tableName = "simulados", indices = [Index("createdAt")])
data class SimuladoEntity(
    @PrimaryKey val id: String,
    val kind: String,            // area | completo | adaptativo
    val label: String,
    val totalQuestions: Int = 0,
    val correctQuestions: Int = 0,
    val durationSeconds: Int = 0,
    val scoreByAreaJson: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val finished: Boolean = false,
    val synced: Boolean = false,
)
