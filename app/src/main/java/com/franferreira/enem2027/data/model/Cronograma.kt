package com.franferreira.enem2027.data.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Um tema dentro de uma semana do cronograma. */
data class TemaDaSemana(
    val tema: String,
    val area: String,
    val totalQuestoes: Int,
    /** Já respondeu alguma questão deste tema. */
    val coberto: Boolean,
)

/** Uma semana do plano até a prova. */
data class SemanaCronograma(
    val numero: Int,
    val inicio: LocalDate,
    val temas: List<TemaDaSemana> = emptyList(),
    /** Temas das semanas anteriores que voltam para fixar. */
    val revisao: List<TemaDaSemana> = emptyList(),
    val atual: Boolean = false,
    /** Já passou e ficou tema sem cobrir. */
    val atrasada: Boolean = false,
) {
    val feitos: Int get() = temas.count { it.coberto }
    val pendentes: Int get() = temas.size - feitos
}

/**
 * O plano de estudo até a prova.
 *
 * Não é um calendário fixo: é recalculado a partir do que já foi coberto, então
 * tema que atrasou entra na semana seguinte e o resto anda sozinho. A ordem
 * coloca primeiro o que a pessoa erra mais.
 */
object Cronograma {

    /** Semanas entre hoje e a prova, no mínimo 4 para o plano não nascer vazio. */
    fun semanasAte(hoje: LocalDate, prova: LocalDate): Int {
        val dias = ChronoUnit.DAYS.between(hoje, prova)
        if (dias <= 0) return 4
        return maxOf(4, (dias / 7).toInt())
    }

    /**
     * Reparte os temas nas semanas, alternando as áreas para a semana não
     * virar "só Humanas" e cada semana ter no máximo [porSemana] temas novos.
     */
    fun montar(
        temas: List<TemaDaSemana>,
        semanas: Int,
        inicio: LocalDate,
        hoje: LocalDate,
    ): List<SemanaCronograma> {
        if (temas.isEmpty()) return emptyList()
        val pendentes = temas.filter { !it.coberto }
        val cobertos = temas.filter { it.coberto }
        // Sobra espaço para revisar o que já foi visto antes de avançar.
        val porSemana = maxOf(1, kotlin.math.ceil(pendentes.size.toDouble() / semanas).toInt())

        val fila = alternarAreas(pendentes).toMutableList()
        val saida = mutableListOf<SemanaCronograma>()
        var revisao = mutableListOf<TemaDaSemana>()
        for (n in 0 until semanas) {
            val inicioSemana = inicio.plusWeeks(n.toLong())
            val fimSemana = inicioSemana.plusDays(6)
            val desta = mutableListOf<TemaDaSemana>()
            while (desta.size < porSemana && fila.isNotEmpty()) {
                desta += fila.removeAt(0)
            }
            val atual = !hoje.isBefore(inicioSemana) && !hoje.isAfter(fimSemana)
            saida += SemanaCronograma(
                numero = n + 1,
                inicio = inicioSemana,
                temas = desta,
                revisao = revisao.toList(),
                atual = atual,
                atrasada = hoje.isAfter(fimSemana) && desta.any { !it.coberto },
            )
            // O que foi estudado nesta semana volta na próxima; o que já estava
            // em revisão sai, para não acumular para sempre.
            revisao = (desta + revisao).take(6).toMutableList()
        }
        if (cobertos.isNotEmpty() && saida.isNotEmpty()) {
            saida[0] = saida[0].copy(revisao = (cobertos.take(6) + saida[0].revisao).distinctBy { it.tema })
        }
        return saida
    }

    /** Quantos temas das listas já foram cobertos, por área (para o resumo). */
    fun cobertura(temas: List<TemaDaSemana>): Map<String, Pair<Int, Int>> =
        temas.groupBy { it.area }.mapValues { (_, lista) -> lista.count { it.coberto } to lista.size }

    /** Uma questão de cada área por rodada: a semana fica variada. */
    private fun alternarAreas(temas: List<TemaDaSemana>): List<TemaDaSemana> {
        val porArea = temas.groupBy { it.area }.values.map { it.toMutableList() }.toMutableList()
        val saida = mutableListOf<TemaDaSemana>()
        while (porArea.isNotEmpty()) {
            val iterador = porArea.iterator()
            while (iterador.hasNext()) {
                val lista = iterador.next()
                saida += lista.removeAt(0)
                if (lista.isEmpty()) iterador.remove()
            }
        }
        return saida
    }
}
