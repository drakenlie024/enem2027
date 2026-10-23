package com.franferreira.enem2027.data.repo

import android.content.Context
import java.time.LocalDate
import java.time.temporal.WeekFields

/**
 * Meta semanal: quantos dias da semana a pessoa quer estudar, e quais já fez.
 *
 * O streak premia a sequência; a meta semanal perdoa o dia perdido. Fica em
 * SharedPreferences porque é um alvo pessoal, não dado de estudo.
 */
object MetaSemanal {

    private const val ARQUIVO = "metas"
    private const val CHAVE_META = "meta_semanal"
    private const val CHAVE_DIAS = "dias_estudados"
    const val META_PADRAO = 5

    private fun prefs(context: Context) =
        context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    fun meta(context: Context): Int = prefs(context).getInt(CHAVE_META, META_PADRAO)

    fun definirMeta(context: Context, dias: Int) {
        prefs(context).edit().putInt(CHAVE_META, dias.coerceIn(1, 7)).apply()
    }

    /** Marca o dia local de hoje como estudado e descarta os dias de semanas antigas. */
    fun marcarHoje(context: Context) {
        val hoje = LocalDate.now()
        val semanaAtual = semanaDe(hoje)
        val atuais = prefs(context).getStringSet(CHAVE_DIAS, emptySet()).orEmpty()
            .filter { data ->
                runCatching { semanaDe(LocalDate.parse(data)) == semanaAtual }.getOrDefault(false)
            }
            .toMutableSet()
        atuais.add(hoje.toString())
        prefs(context).edit().putStringSet(CHAVE_DIAS, atuais).apply()
    }

    /** Quantos dias distintos desta semana já foram estudados. */
    fun diasNaSemana(context: Context): Int {
        val semanaAtual = semanaDe(LocalDate.now())
        return prefs(context).getStringSet(CHAVE_DIAS, emptySet()).orEmpty()
            .count { data ->
                runCatching { semanaDe(LocalDate.parse(data)) == semanaAtual }.getOrDefault(false)
            }
    }

    /** Quais dias (1 = segunda ... 7 = domingo) da semana atual já têm estudo. */
    fun diasMarcados(context: Context): Set<Int> {
        val semanaAtual = semanaDe(LocalDate.now())
        val campo = WeekFields.ISO.dayOfWeek()
        return prefs(context).getStringSet(CHAVE_DIAS, emptySet()).orEmpty()
            .mapNotNull { data ->
                runCatching {
                    val d = LocalDate.parse(data)
                    if (semanaDe(d) == semanaAtual) d.get(campo) else null
                }.getOrNull()
            }
            .toSet()
    }

    private fun semanaDe(data: LocalDate): String {
        val campo = WeekFields.ISO.weekOfWeekBasedYear()
        return "${data.get(campo)}-${data.get(WeekFields.ISO.weekBasedYear())}"
    }
}