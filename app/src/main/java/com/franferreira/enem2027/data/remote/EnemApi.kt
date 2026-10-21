package com.franferreira.enem2027.data.remote

import com.franferreira.enem2027.data.model.AgendaItem
import com.franferreira.enem2027.data.model.ExplainRequest
import com.franferreira.enem2027.data.model.ExplainResponse
import com.franferreira.enem2027.data.model.DiagnosticoSimulado
import com.franferreira.enem2027.data.model.EssayCorrection
import com.franferreira.enem2027.data.model.EssayRequest
import com.franferreira.enem2027.data.model.GamificationResponse
import com.franferreira.enem2027.data.model.LoginRequest
import com.franferreira.enem2027.data.model.ProgressBatch
import com.franferreira.enem2027.data.model.ProgressoResponse
import com.franferreira.enem2027.data.model.RegisterRequest
import com.franferreira.enem2027.data.model.RespostaSimulado
import com.franferreira.enem2027.data.model.SimuladoMontado
import com.franferreira.enem2027.data.model.SimuladoRequest
import com.franferreira.enem2027.data.model.SyncResponse
import com.franferreira.enem2027.data.model.TokenResponse
import com.franferreira.enem2027.data.model.YearInfo
import com.franferreira.enem2027.data.model.YearPackage
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface EnemApi {
    @POST("auth/register")
    suspend fun registrar(@Body body: RegisterRequest): TokenResponse

    @POST("auth/login")
    suspend fun entrar(@Body body: LoginRequest): TokenResponse

    @GET("acervo/years")
    suspend fun anos(): List<YearInfo>

    @GET("acervo/enem/{ano}")
    suspend fun ano(
        @Path("ano") ano: Int,
        @Query("limit") limit: Int = 200,
        @Query("offset") offset: Int = 0,
    ): YearPackage
    @POST("sync/progress")
    suspend fun sincronizar(@Body body: ProgressBatch): SyncResponse

    @GET("sync/gamification")
    suspend fun gamificacao(): GamificationResponse

    @GET("sync/progresso")
    suspend fun progresso(@Query("dias") dias: Int = 30): ProgressoResponse

    @GET("sync/agenda")
    suspend fun agenda(): Map<String, List<AgendaItem>>

    @PUT("sync/agenda")
    suspend fun salvarAgenda(@Body itens: List<AgendaItem>): Map<String, Any>

    @POST("ai/explain")
    suspend fun explicar(@Body body: ExplainRequest): ExplainResponse

    @POST("simulados/gerar")
    suspend fun gerarSimulado(@Body body: SimuladoRequest): SimuladoMontado

    @POST("simulados/diagnostico")
    suspend fun diagnosticoSimulado(@Body body: RespostaSimulado): DiagnosticoSimulado

    @GET("simulados/historico")
    suspend fun historicoSimulados(): Map<String, List<Map<String, String>>>

    @POST("ai/essay/correct")
    suspend fun corrigirRedacao(@Body body: EssayRequest): EssayCorrection
}

/**
 * Cliente HTTP do app.
 *
 * Sem CDN e sem biblioteca de imagem pesada: as figuras das questões vêm por
 * URL absoluta do acervo e são carregadas por um carregador simples.
 */
object ApiClient {
    const val BASE_URL = "https://enem-api.franferreira.cloud"

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        // Um campo null na fonte vira o default do modelo, em vez de
        // derrubar o parse do pacote inteiro de uma vez.
        coerceInputValues = true
    }

    @Volatile
    var token: String? = null

    val http: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val req = original.newBuilder().apply {
                    token?.let { header("Authorization", "Bearer $it") }
                    header("Accept", "application/json")
                }.build()
                chain.proceed(req)
            }
            .build()
    }

    val api: EnemApi by lazy {
        Retrofit.Builder()
            .baseUrl("$BASE_URL/")
            .client(http)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(EnemApi::class.java)
    }
}
