package com.franferreira.enem2027.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import com.franferreira.enem2027.data.local.TemaStatsEntity
import com.franferreira.enem2027.data.local.TrilhaEtapaEntity

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun porId(id: String): QuestionEntity?

    @Query("SELECT COUNT(*) FROM questions WHERE ano = :ano")
    suspend fun totalDoAno(ano: Int): Int

    @Query("SELECT DISTINCT ano FROM questions WHERE ano > 0 ORDER BY ano DESC")
    suspend fun anos(): List<Int>

    @Query("SELECT DISTINCT area FROM questions WHERE area IS NOT NULL ORDER BY area")
    suspend fun areas(): List<String>

    @Query("SELECT DISTINCT tema FROM questions WHERE tema IS NOT NULL ORDER BY tema")
    suspend fun temas(): List<String>

    /**
     * Questões de um tema dentro de uma área — é o que monta as etapas da
     * trilha. A ordem (mais recentes primeiro) faz a etapa varied.
     */
    @Query(
        """
        SELECT * FROM questions
        WHERE tema = :tema AND area = :area
        ORDER BY ano DESC, indice ASC
        """
    )
    suspend fun porTemaEArea(tema: String, area: String): List<QuestionEntity>

    /** Questões de um tema em qualquer área (usado na trilha adaptativa). */
    @Query(
        """
        SELECT * FROM questions
        WHERE tema = :tema
        ORDER BY ano DESC, indice ASC
        """
    )
    suspend fun porTema(tema: String): List<QuestionEntity>

    /**
     * Acertos, erros e total por tema, pelo estado do SRS local.
     *
     * É daqui que sai a trilha adaptativa "Para você": os temas com mais
     * erros e taxa baixa sobem na lista.
     */
    @Query(
        """
        SELECT q.tema AS tema,
               MIN(q.area) AS area,
               SUM(CASE WHEN p.lastQuality >= 3 THEN 1 ELSE 0 END) AS acertos,
               SUM(CASE WHEN p.lastQuality < 3 THEN 1 ELSE 0 END) AS erros,
               COUNT(*) AS total
        FROM questions q
        JOIN progress p ON p.questionId = q.id
        WHERE q.tema IS NOT NULL AND p.lastQuality IS NOT NULL
        GROUP BY q.tema
        """
    )
    suspend fun statsPorTema(): List<TemaStatsEntity>

    /**
     * Questões para a sessão de hoje: as que vencem no SRS, priorizando as
     * que o usuário escolheu como tema. Quando não há nada vencido, cai
     * para as nunca vistas da área escolhida.
     *
     * O estado do SRS vem junto (`progressState`), para a tela de revisão
     * separar o que está vencendo do que é novo.
     */
    @Query(
        """
        SELECT q.*, p.state AS progressState FROM questions q
        LEFT JOIN progress p ON p.questionId = q.id
        WHERE (:area IS NULL OR q.area = :area)
          AND (:tema IS NULL OR q.tema = :tema)
          AND (
            p.questionId IS NULL
            OR (p.state != 'seen' AND p.dueAt <= :agora)
          )
        ORDER BY
          CASE WHEN p.questionId IS NULL THEN 1 ELSE 0 END,
          p.dueAt ASC,
          q.ano DESC,
          q.indice ASC
        LIMIT :limite
        """
    )
    suspend fun paraRevisao(
        area: String?,
        tema: String?,
        agora: Long,
        limite: Int,
    ): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM progress WHERE dueAt <= :agora AND state != 'seen'")
    suspend fun pendentesProgresso(agora: Long): Int

    /**
     * Quantas questões foram respondidas hoje.
     *
     * É o que o anel da meta mostra. Antes, a tela usava o saldo do SRS
     * (`pendentes`), que é o que FALTA — cada resposta diminuía o anel.
     */
    @Query("SELECT COUNT(*) FROM progress WHERE lastReviewedAt >= :inicioDoDia")
    suspend fun respondidasHoje(inicioDoDia: Long): Int

    @Query("SELECT COUNT(*) FROM questions WHERE id IN (SELECT questionId FROM progress WHERE dueAt <= :agora AND state != 'seen')")
    suspend fun pendentes(agora: Long): Int

    /**
     * Versão do acervo local, por ano.
     *
     * Serve para saber se o servidor tem conteúdo mais novo que o aparelho —
     * por exemplo, quando as explicações offline foram geradas depois do
     * download. Sem isso, o usuário ficaria com questões sem explicação
     * até rebaixar o ano na mão.
     */
    @Query("SELECT ano, versaoAcervo FROM questions WHERE ano > 0 GROUP BY ano")
    suspend fun versoesLocais(): List<VersaoAnoEntity>

    @Query("SELECT COUNT(*) FROM progress WHERE questionId = :id")
    suspend fun temProgresso(id: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(questions: List<QuestionEntity>)

    @Query("DELETE FROM questions WHERE ano = :ano")
    suspend fun limparAno(ano: Int)

    @Transaction
    suspend fun substituirAno(ano: Int, questions: List<QuestionEntity>) {
        limparAno(ano)
        inserir(questions)
    }
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress WHERE questionId = :id")
    suspend fun porId(id: String): ProgressEntity?

    @Query("SELECT * FROM progress WHERE synced = 0")
    suspend fun naoSincronizados(): List<ProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: ProgressEntity)

    @Query("UPDATE progress SET synced = 1 WHERE questionId IN (:ids)")
    suspend fun marcarSincronizados(ids: List<String>)

    @Query("SELECT COUNT(*) FROM progress WHERE state = 'review'")
    suspend fun emRevisao(): Int
}

@Dao
interface AgendaDao {
    @Query("SELECT * FROM agenda")
    suspend fun todos(): List<AgendaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(itens: List<AgendaEntity>)

    @Query("DELETE FROM agenda")
    suspend fun limpar()

    /**
     * Troca a agenda inteira numa transação só.
     *
     * Sem a transação, um crash no meio do `limpar()` deixava a agenda
     * vazia — e é dela que o alarme do lembrete lê.
     */
    @Transaction
    suspend fun substituir(itens: List<AgendaEntity>) {
        limpar()
        if (itens.isNotEmpty()) inserir(itens)
    }
}

@Dao
interface GamificationDao {
    @Query("SELECT * FROM gamification WHERE id = 0")
    suspend fun atual(): GamificationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(item: GamificationEntity)
}

@Dao
interface EssayDao {
    @Query("SELECT * FROM essays ORDER BY createdAt DESC")
    suspend fun todos(): List<EssayEntity>

    @Query("SELECT * FROM essays WHERE id = :id")
    suspend fun porId(id: String): EssayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(essay: EssayEntity)

    @Query("UPDATE essays SET synced = 1 WHERE id = :id")
    suspend fun marcarSincronizado(id: String)
}

@Dao
interface PendingAnswerDao {
    @Insert
    suspend fun inserir(resposta: PendingAnswerEntity)

    @Query("SELECT * FROM pending_answers WHERE synced = 0")
    suspend fun naoSincronizadas(): List<PendingAnswerEntity>

    @Query("UPDATE pending_answers SET synced = 1 WHERE localId IN (:ids)")
    suspend fun marcarSincronizadas(ids: List<Long>)

    @Query("DELETE FROM pending_answers WHERE synced = 1")
    suspend fun limparSincronizadas()
}

@Dao
interface SimuladoDao {
    @Query("SELECT * FROM simulados ORDER BY createdAt DESC")
    suspend fun todos(): List<SimuladoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(simulado: SimuladoEntity)
}

/**
 * Progresso das etapas de trilha.
 *
 * Fica local porque a trilha é montada no aparelho — o servidor só recebe o
 * resultado para backup e para o caso de trocar de aparelho.
 */
@Dao
interface TrilhaEtapaDao {
    @Query("SELECT * FROM trilha_etapas")
    suspend fun todos(): List<TrilhaEtapaEntity>

    @Query("SELECT * FROM trilha_etapas WHERE trilhaId = :trilhaId AND etapa = :etapa")
    suspend fun porEtapa(trilhaId: String, etapa: Int): TrilhaEtapaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(item: TrilhaEtapaEntity)

    @Query("UPDATE trilha_etapas SET synced = 1 WHERE trilhaId = :trilhaId AND etapa = :etapa")
    suspend fun marcarSincronizado(trilhaId: String, etapa: Int)

    @Query("SELECT * FROM trilha_etapas WHERE synced = 0")
    suspend fun naoSincronizadas(): List<TrilhaEtapaEntity>
}

@Database(
    entities = [
        QuestionEntity::class,
        ProgressEntity::class,
        AgendaEntity::class,
        GamificationEntity::class,
        EssayEntity::class,
        PendingAnswerEntity::class,
        SimuladoEntity::class,
        TrilhaEtapaEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class EnemDatabase : RoomDatabase() {
    abstract fun questions(): QuestionDao
    abstract fun progress(): ProgressDao
    abstract fun agenda(): AgendaDao
    abstract fun gamification(): GamificationDao
    abstract fun essays(): EssayDao
    abstract fun pendingAnswers(): PendingAnswerDao
    abstract fun simulados(): SimuladoDao
    abstract fun trilhaEtapas(): TrilhaEtapaDao

    companion object {
        @Volatile
        private var instance: EnemDatabase? = null

        /**
         * Migração 1 → 2: as trilhas.
         *
         * Adiciona a coluna `explicacao` em `questions` (a explicação
         * offline, gerada no servidor) e cria a tabela `trilha_etapas`.
         *
         * IMPORTANTE: não existe `fallbackToDestructiveMigration()`. O
         * fallback do Room apaga TODAS as tabelas no upgrade — e aqui o banco
         * local É o produto: o acervo baixado, o SRS inteiro e as respostas
         * ainda não sincronizadas vivem só no aparelho. Destruir isso no
         * upgrade seria perder o trabalho de semanas sem aviso.
         */
        val MIGRACAO_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE questions ADD COLUMN explicacao TEXT"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `trilha_etapas` (
                        `trilhaId` TEXT NOT NULL,
                        `etapa` INTEGER NOT NULL,
                        `area` TEXT NOT NULL,
                        `tema` TEXT NOT NULL,
                        `acertos` INTEGER NOT NULL,
                        `total` INTEGER NOT NULL,
                        `segundos` INTEGER NOT NULL,
                        `xp` INTEGER NOT NULL,
                        `concluidaEm` INTEGER NOT NULL,
                        `synced` INTEGER NOT NULL,
                        PRIMARY KEY(`trilhaId`, `etapa`)
                    )
                    """.trimIndent()
                )
            }
        }

        /**
         * Migração 2 → 3: o schema não muda (a v3 é a v2 com a migration
         * 1→2 no lugar do fallback destrutivo).
         *
         * Existe porque o APK intermediário foi publicado com `version = 2` e
         * `fallbackToDestructiveMigration()`: quem o instalou tem um banco v2,
         * e o Room precisa de um caminho declarado de 2 para 3 — mesmo que
         * o caminho seja vazio.
         */
        val MIGRACAO_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Nada a fazer: o schema da v3 é idêntico ao da v2.
            }
        }

        fun get(context: android.content.Context): EnemDatabase =
            instance ?: synchronized(this) {
                instance ?: androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    EnemDatabase::class.java,
                    "enem2027.db",
                )
                    .addMigrations(MIGRACAO_1_2, MIGRACAO_2_3)
                    .build().also { instance = it }
            }
    }
}
