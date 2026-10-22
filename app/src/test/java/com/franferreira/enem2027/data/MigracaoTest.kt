package com.franferreira.enem2027.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.franferreira.enem2027.data.local.EnemDatabase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Teste de UPGRADE do banco.
 *
 * O offline-guarantee test apaga o banco antes de abrir, então ele nunca
 * exercita migração. Este aqui cria o banco na versão 1, enche de dados e
 * abre na versão 3 — o caminho que um usuário real faz ao instalar a
 * atualização.
 *
 * A razão de existir: a versão intermediária foi publicada com
 * `fallbackToDestructiveMigration()`, que APAGA todas as tabelas no upgrade.
 * Como o banco local é o produto (acervo baixado, SRS, respostas não
 * sincronizadas), isso é perda de dado sem aviso. A migração precisa
 * preservar tudo.
 *
 * O banco v1 é criado à mão, com o DDL EXATO que o Room gerava para as
 * entidades da v1 — as 7 tabelas, os índices e a ordem das colunas. Se
 * divergir do schema que o Room espera, ele rejeita a abertura com
 * "Migration didn't properly handle", e o teste falha — que é o ponto.
 */
@RunWith(AndroidJUnit4::class)
class MigracaoTest {

    private val nome = "migracao-teste.db"

    private fun caminho(context: Context): String =
        context.getDatabasePath(nome).absolutePath

    /** Cria o banco na versão 1 (o schema anterior às trilhas). */
    private fun criarVersao1(context: Context) {
        val arquivo = File(caminho(context))
        arquivo.parentFile?.mkdirs()
        arquivo.delete()
        val db = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(nome)
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        // questions (v1: SEM a coluna `explicacao`, que a
                        // migração 1->2 adiciona).
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `questions` (
                                `id` TEXT NOT NULL, `ano` INTEGER NOT NULL, `indice` INTEGER NOT NULL,
                                `area` TEXT, `idioma` TEXT, `origem` TEXT NOT NULL,
                                `enunciado` TEXT, `contexto` TEXT, `arquivosJson` TEXT NOT NULL,
                                `alternativasJson` TEXT NOT NULL, `gabarito` TEXT, `tema` TEXT,
                                `competencia` TEXT, `dificuldade` TEXT, `subassunto` TEXT,
                                `versaoAcervo` TEXT, `baixadoEm` INTEGER NOT NULL,
                                `progressState` TEXT,
                                PRIMARY KEY(`id`))
                            """.trimIndent()
                        )
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_questions_ano` ON `questions` (`ano`)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_questions_area` ON `questions` (`area`)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_questions_tema` ON `questions` (`tema`)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_questions_origem` ON `questions` (`origem`)")

                        // progress
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `progress` (
                                `questionId` TEXT NOT NULL, `ease` REAL NOT NULL, `intervalDays` REAL NOT NULL,
                                `repetitions` INTEGER NOT NULL, `lapses` INTEGER NOT NULL, `state` TEXT NOT NULL,
                                `dueAt` INTEGER NOT NULL, `lastReviewedAt` INTEGER, `lastQuality` INTEGER,
                                `correctCount` INTEGER NOT NULL, `wrongCount` INTEGER NOT NULL,
                                `synced` INTEGER NOT NULL,
                                PRIMARY KEY(`questionId`))
                            """.trimIndent()
                        )
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_progress_dueAt` ON `progress` (`dueAt`)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_progress_state` ON `progress` (`state`)")

                        // agenda
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `agenda` (
                                `id` INTEGER NOT NULL, `weekday` INTEGER NOT NULL,
                                `period` TEXT NOT NULL, `minutesTarget` INTEGER NOT NULL,
                                PRIMARY KEY(`id`))
                            """.trimIndent()
                        )

                        // gamification
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `gamification` (
                                `id` INTEGER NOT NULL, `xp` INTEGER NOT NULL, `level` INTEGER NOT NULL,
                                `streak` INTEGER NOT NULL, `longestStreak` INTEGER NOT NULL,
                                `lastStudyDate` TEXT, `unlockedJson` TEXT NOT NULL, `synced` INTEGER NOT NULL,
                                PRIMARY KEY(`id`))
                            """.trimIndent()
                        )

                        // essays
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `essays` (
                                `id` TEXT NOT NULL, `theme` TEXT NOT NULL, `prompt` TEXT, `text` TEXT NOT NULL,
                                `score` INTEGER, `feedbackJson` TEXT, `createdAt` INTEGER NOT NULL,
                                `synced` INTEGER NOT NULL,
                                PRIMARY KEY(`id`))
                            """.trimIndent()
                        )
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_essays_createdAt` ON `essays` (`createdAt`)")

                        // pending_answers
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `pending_answers` (
                                `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `questionId` TEXT NOT NULL, `quality` INTEGER NOT NULL, `correct` INTEGER NOT NULL,
                                `secondsTaken` REAL, `reviewedAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL)
                            """.trimIndent()
                        )

                        // simulados
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `simulados` (
                                `id` TEXT NOT NULL, `kind` TEXT NOT NULL, `label` TEXT NOT NULL,
                                `totalQuestions` INTEGER NOT NULL, `correctQuestions` INTEGER NOT NULL,
                                `durationSeconds` INTEGER NOT NULL, `scoreByAreaJson` TEXT,
                                `createdAt` INTEGER NOT NULL, `finished` INTEGER NOT NULL, `synced` INTEGER NOT NULL,
                                PRIMARY KEY(`id`))
                            """.trimIndent()
                        )
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_simulados_createdAt` ON `simulados` (`createdAt`)")
                    }

                    override fun onUpgrade(
                        db: androidx.sqlite.db.SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int,
                    ) = Unit
                })
                .build()
        )
        // Insere dados que NÃO podem ser perdidos no upgrade.
        db.writableDatabase.execSQL(
            "INSERT INTO questions (id, ano, indice, origem, arquivosJson, alternativasJson, baixadoEm) " +
                "VALUES ('enem-2023-q001', 2023, 1, 'enem', '[]', '[]', 1000)"
        )
        db.writableDatabase.execSQL(
            "INSERT INTO progress (questionId, ease, intervalDays, repetitions, lapses, state, dueAt, correctCount, wrongCount, synced) " +
                "VALUES ('enem-2023-q001', 2.5, 3.0, 2, 0, 'review', 999999, 2, 0, 0)"
        )
        db.close()
    }

    @Test
    fun upgrade_preserva_o_progresso() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        criarVersao1(context)

        // Abre na versão atual: as migrações têm que rodar.
        val db = Room.databaseBuilder(context, EnemDatabase::class.java, nome)
            .addMigrations(EnemDatabase.MIGRACAO_1_2, EnemDatabase.MIGRACAO_2_3)
            .build()

        val questao = db.questions().porId("enem-2023-q001")
        assertNotNull("A questão do acervo não pode sumir no upgrade", questao)
        assertEquals(2023, questao!!.ano)

        val progresso = db.progress().porId("enem-2023-q001")
        assertNotNull("O progresso do SRS não pode sumir no upgrade", progresso)
        assertEquals("review", progresso!!.state)
        assertEquals(2, progresso.repetitions)
        assertEquals(2, progresso.correctCount)

        db.close()
    }

    @Test
    fun upgrade_cria_a_coluna_de_explicacao() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        criarVersao1(context)

        val db = Room.databaseBuilder(context, EnemDatabase::class.java, nome)
            .addMigrations(EnemDatabase.MIGRACAO_1_2, EnemDatabase.MIGRACAO_2_3)
            .build()

        // A coluna nova existe e aceita valor (a explicação offline).
        val questao = db.questions().porId("enem-2023-q001")
        assertNotNull(questao)
        // Se a coluna não existisse, o Room nem teria aberto o banco.
        db.close()
    }
}
