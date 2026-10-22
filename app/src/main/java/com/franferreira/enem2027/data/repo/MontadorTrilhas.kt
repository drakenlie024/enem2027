package com.franferreira.enem2027.data.repo

import com.franferreira.enem2027.data.local.EnemDatabase
import com.franferreira.enem2027.data.local.paraModelo
import com.franferreira.enem2027.data.model.CurriculoTrilhas
import com.franferreira.enem2027.data.model.Etapa
import com.franferreira.enem2027.data.model.EtapaConcluida
import com.franferreira.enem2027.data.model.Question
import com.franferreira.enem2027.data.model.Trilha
import com.franferreira.enem2027.data.model.TrilhaCurriculo

/**
 * Monta as trilhas no aparelho.
 *
 * O servidor entrega o **currículo** (quais trilhas e temas existem). As
 * questões de cada etapa saem do banco local, e a trilha adaptativa sai do
 * SRS local. Tudo isso acontece offline: o app baixa o acervo por ano e
 * funciona sem rede.
 */
class MontadorTrilhas(
    private val db: EnemDatabase,
    private val questoesPorEtapa: Int = 12,
) {

    /**
     * Monta as trilhas fixas e a adaptativa.
     *
     * @param curriculo o que o servidor mandou
     * @param statsPorTema acertos/erros por tema (vindo do SRS local)
     * @param etapasConcluidas {trilha_id: [indice, ...]}
     * @param resultadoPorEtapa {trilha_id: {indice: (acertos, total, segundos, xp)}}
     */
    suspend fun montar(
        curriculo: CurriculoTrilhas,
        statsPorTema: Map<String, StatsTema>,
        etapasConcluidas: Map<String, List<Int>> = emptyMap(),
        resultadoPorEtapa: Map<String, Map<Int, ResultadoEtapa>> = emptyMap(),
    ): List<Trilha> {
        val trilhas = mutableListOf<Trilha>()

        // --- adaptativa: os temas onde o usuário mais erra, por prioridade
        val temasFracos = statsPorTema.entries
            .filter { it.value.total >= 2 }
            .sortedWith(
                compareBy({ it.value.taxa }, { -it.value.erros }, { it.key }),
            )
            .map { it.key }
            .take(4)

        if (temasFracos.isNotEmpty()) {
            trilhas += montarAdaptativa(
                temas = temasFracos,
                statsPorTema = statsPorTema,
                etapasConcluidas = etapasConcluidas,
                resultadoPorEtapa = resultadoPorEtapa,
            )
        }

        // --- fixas: as do currículo
        for (definicao in curriculo.trilhas) {
            trilhas += montarFixa(
                definicao = definicao,
                etapasConcluidas = etapasConcluidas,
                resultadoPorEtapa = resultadoPorEtapa,
            )
        }

        return trilhas
    }

    private suspend fun montarFixa(
        definicao: TrilhaCurriculo,
        etapasConcluidas: Map<String, List<Int>>,
        resultadoPorEtapa: Map<String, Map<Int, ResultadoEtapa>>,
    ): Trilha {
        val etapas = mutableListOf<Etapa>()
        // Cada trilha tem suas próprias questões: uma questão não se repete
        // dentro da mesma trilha.
        val usadas = mutableSetOf<String>()

        for (passo in definicao.etapas) {
            val questoes = selecionar(
                area = definicao.area,
                tema = passo.nome,
                apoio = passo.apoio,
                usadas = usadas,
            )
            usadas += questoes.map { it.id }
            etapas += montarEtapa(
                indice = passo.indice,
                nome = passo.nome,
                apoio = passo.apoio,
                questoes = questoes,
                trilhaId = definicao.id,
                etapasConcluidas = etapasConcluidas,
                resultadoPorEtapa = resultadoPorEtapa,
            )
        }

        return Trilha(
            id = definicao.id,
            nome = definicao.nome,
            descricao = definicao.descricao,
            area = definicao.area,
            areaNome = definicao.areaNome,
            cor = definicao.cor,
            tipo = definicao.tipo,
            etapas = etapas,
        )
    }

    private suspend fun montarAdaptativa(
        temas: List<String>,
        statsPorTema: Map<String, StatsTema>,
        etapasConcluidas: Map<String, List<Int>>,
        resultadoPorEtapa: Map<String, Map<Int, ResultadoEtapa>>,
    ): Trilha {
        val etapas = mutableListOf<Etapa>()
        val usadas = mutableSetOf<String>()

        // A trilha adaptativa atravessa as áreas: cada etapa soma os temas
        // anteriores, como nas fixas — o caminho vai se aprofundando.
        temas.forEachIndexed { indice, tema ->
            val acumulados = temas.take(indice + 1)
            val questoes = mutableListOf<Question>()
            for (nome in acumulados) {
                val area = statsPorTema[nome]?.area
                val candidatas = if (area != null) {
                    db.questions().porTemaEArea(nome, area)
                } else {
                    db.questions().porTema(nome)
                }
                for (q in candidatas) {
                    if (q.id !in usadas) {
                        questoes += q.paraModelo()
                        usadas += q.id
                    }
                    if (questoes.size >= questoesPorEtapa) break
                }
                if (questoes.size >= questoesPorEtapa) break
            }
            etapas += montarEtapa(
                indice = indice,
                nome = tema,
                apoio = emptyList(),
                questoes = questoes,
                trilhaId = "adaptativa",
                etapasConcluidas = etapasConcluidas,
                resultadoPorEtapa = resultadoPorEtapa,
            )
        }

        return Trilha(
            id = "adaptativa",
            nome = "Para você",
            descricao = "Montada com o que você mais tem errado. Comece por aqui.",
            area = "",
            areaNome = "Todos os temas",
            cor = "#7C6CFF",
            tipo = "adaptativa",
            etapas = etapas,
        )
    }

    private suspend fun montarEtapa(
        indice: Int,
        nome: String,
        apoio: List<String>,
        questoes: List<Question>,
        trilhaId: String,
        etapasConcluidas: Map<String, List<Int>>,
        resultadoPorEtapa: Map<String, Map<Int, ResultadoEtapa>>,
    ): Etapa {
        val concluida = indice in (etapasConcluidas[trilhaId] ?: emptyList())
        val resultado = resultadoPorEtapa[trilhaId]?.get(indice)
        return Etapa(
            indice = indice,
            nome = nome,
            apoio = apoio,
            questoes = questoes,
            concluida = concluida,
            acertos = resultado?.acertos ?: 0,
            total = resultado?.total ?: questoes.size,
            segundos = resultado?.segundos ?: 0,
            xp = resultado?.xp ?: 0,
        )
    }

    /**
     * Escolhe as questões de uma etapa: primeiro o tema, depois os temas de
     * apoio (para completar as 12 quando o tema principal não dá conta).
     */
    private suspend fun selecionar(
        area: String,
        tema: String,
        apoio: List<String>,
        usadas: Set<String>,
    ): List<Question> {
        val escolhidas = mutableListOf<Question>()
        for (nome in listOf(tema) + apoio) {
            val candidatas = db.questions().porTemaEArea(nome, area)
            for (q in candidatas) {
                if (q.id in usadas || q.id in escolhidas.map { it.id }) continue
                escolhidas += q.paraModelo()
                if (escolhidas.size >= questoesPorEtapa) return escolhidas
            }
        }
        return escolhidas
    }
}

/** Estatísticas de um tema, para montar a trilha adaptativa. */
data class StatsTema(
    val tema: String,
    val erros: Int,
    val acertos: Int,
    val total: Int,
    val area: String? = null,
) {
    val taxa: Double get() = if (total == 0) 100.0 else 100.0 * acertos / total
}

/** Resultado de uma etapa concluída. */
data class ResultadoEtapa(
    val acertos: Int,
    val total: Int,
    val segundos: Int,
    val xp: Int,
)
