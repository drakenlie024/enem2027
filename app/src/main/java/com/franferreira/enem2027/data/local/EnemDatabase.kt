package com.franferreira.enem2027.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun porId(id: String): QuestionEntity?

    @Query("SELECT COUNT(*) FROM questions WHERE ano = :ano")
    suspend fun totalDoAno(ano: Int): Int

    @Query("SELECT DISTINCT ano FROM questions ORDER BY ano DESC")
    suspend fun anos(): List<Int>

    @Query("SELECT DISTINCT area FROM questions WHERE area IS NOT NULL ORDER BY area")
    suspend fun areas(): List<String>

    @Query("SELECT DISTINCT tema FROM questions WHERE tema IS NOT NULL ORDER BY tema")
    suspend fun temas(): List<String>

    /**
     * Questões para a sessão de hoje: as que vencem no SRS, priorizando as
     * que o usuário escolheu como tema. Quando não há nada vencido, cai
     * para as nunca vistas da área escolhida.
     */
    @Query(
        """
        SELECT q.* FROM questions q
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
    suspend fun pendentes(agora: Long): Int

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
    suspend fun salvar(itens: List<AgendaEntity>)

    @Query("DELETE FROM agenda")
    suspend fun limpar()
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

@Database(
    entities = [
        QuestionEntity::class,
        ProgressEntity::class,
        AgendaEntity::class,
        GamificationEntity::class,
        EssayEntity::class,
        PendingAnswerEntity::class,
        SimuladoEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class EnemDatabase : RoomDatabase() {
    abstract fun questions(): QuestionDao
    abstract fun progress(): ProgressDao
    abstract fun agenda(): AgendaDao
    abstract fun gamification(): GamificationDao
    abstract fun essays(): EssayDao
    abstract fun pendingAnswers(): PendingAnswerDao
    abstract fun simulados(): SimuladoDao

    companion object {
        @Volatile
        private var instance: EnemDatabase? = null

        fun get(context: android.content.Context): EnemDatabase =
            instance ?: synchronized(this) {
                instance ?: androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    EnemDatabase::class.java,
                    "enem2027.db",
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
