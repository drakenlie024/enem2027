package com.franferreira.enem2027.data.local

import com.franferreira.enem2027.data.model.Alternative
import com.franferreira.enem2027.data.model.Question
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Conversão entre a entity do Room e o modelo que o app usa.
 *
 * Fica num arquivo só porque dois lugares precisam: o `Repositorio` (que
 * baixa o acervo) e o `MontadorTrilhas` (que monta as etapas a partir das
 * queries do banco). Ter duas cópias é pedir divergência.
 */
val JsonCompartilhado = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
}

fun QuestionEntity.paraModelo(): Question = Question(
    id = id,
    ano = ano,
    indice = indice,
    area = area,
    idioma = idioma,
    origem = origem,
    enunciado = enunciado,
    contexto = contexto,
    arquivos = runCatching {
        JsonCompartilhado.decodeFromString<List<String>>(arquivosJson)
    }.getOrDefault(emptyList()),
    alternativas = runCatching {
        JsonCompartilhado.decodeFromString<List<Alternative>>(alternativasJson)
    }.getOrDefault(emptyList()),
    gabarito = gabarito,
    tema = tema,
    competencia = competencia,
    dificuldade = dificuldade,
    subassunto = subassunto,
    explicacao = explicacao,
)

fun Question.paraEntidade(versao: String?): QuestionEntity = QuestionEntity(
    id = id,
    ano = ano,
    indice = indice,
    area = area,
    idioma = idioma,
    origem = origem,
    enunciado = enunciado,
    contexto = contexto,
    arquivosJson = JsonCompartilhado.encodeToString(arquivos),
    alternativasJson = JsonCompartilhado.encodeToString(alternativas),
    gabarito = gabarito,
    tema = tema,
    competencia = competencia,
    dificuldade = dificuldade,
    subassunto = subassunto,
    explicacao = explicacao,
    versaoAcervo = versao,
)
