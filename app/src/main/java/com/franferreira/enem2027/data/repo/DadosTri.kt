package com.franferreira.enem2027.data.repo

/**
 * Dados de TRI: como os acertos viram nota, e qual era a nota de corte de cada
 * ano.
 *
 * Fontes:
 * - **Acertos → nota**: medianas dos microdados do INEP (ENEM 2025). O TRI
 *   não é uma fórmula linear — a curva muda de inclinação conforme o
 *   desempenho. Por isso os dados são pontos, e a app interpola.
 * - **Nota de corte por ano**: faixas observadas na UFSC (TRI ENEM, 2009 a
 *   2024), que é a referência de mercado mais usada.
 *
 * Os números são estimativas de mercado, não promessa de nota. A app diz
 * isso na tela.
 */
object DadosTri {

    /** NomeBonito das áreas, como o Inep escreve. */
    val NOMES_AREA = mapOf(
        "linguagens" to "Linguagens",
        "ciencias-humanas" to "Humanas",
        "ciencias-natureza" to "Naturezas",
        "matematica" to "Matemática",
    )

    /**
     * Quantos acertos a mediana dos aprovados fez para cada nota-alvo (2025).
     *
     * Serve para a direção "nota-alvo → acertos": quantas questões eu
     * preciso acertar para chegar lá.
     */
    private val ACERTOS_POR_NOTA_ALVO: Map<String, List<Pair<Int, Int>>> = mapOf(
        "matematica" to listOf(
            500 to 13, 550 to 15, 600 to 17, 650 to 21, 700 to 25, 800 to 33, 900 to 40,
        ),
        "ciencias-natureza" to listOf(
            500 to 14, 550 to 18, 600 to 22, 650 to 27, 700 to 33, 800 to 41, 900 to 47,
        ),
        "ciencias-humanas" to listOf(
            500 to 15, 550 to 19, 600 to 25, 650 to 31, 700 to 37, 800 to 43, 900 to 49,
        ),
        "linguagens" to listOf(
            500 to 16, 550 to 23, 600 to 30, 650 to 37, 700 to 42, 800 to 48, 900 to 54,
        ),
    )

    /**
     * Mediana de nota por número de acertos (2025).
     *
     * Serve para a direção "acertos → nota": quanto eu faria com o que já
     * acertei. Só Matemática e Naturezas têm a série completa medida.
     */
    private val NOTA_POR_ACERTOS: Map<String, List<Pair<Int, Int>>> = mapOf(
        "matematica" to listOf(
            10 to 440, 15 to 561, 20 to 649, 25 to 711, 30 to 768, 35 to 830, 40 to 909, 45 to 980,
        ),
        "ciencias-natureza" to listOf(
            10 to 436, 15 to 519, 20 to 583, 25 to 634, 30 to 680, 35 to 728, 40 to 786, 45 to 859,
        ),
    )

    /**
     * Faixa de notas por área e ano (UFSC, TRI ENEM).
     *
     * É a "nota de corte" de cada ano: o menor e o maior valor apurado. A
     * referência de mercado para saber se a nota compensa.
     */
    private val FAIXA_POR_ANO: Map<Int, Map<String, Pair<Double, Double>>> = mapOf(
        2024 to mapOf(
            "ciencias-natureza" to (308.1 to 867.2),
            "ciencias-humanas" to (283.8 to 819.7),
            "linguagens" to (294.1 to 795.8),
            "matematica" to (334.3 to 961.9),
        ),
        2023 to mapOf(
            "ciencias-natureza" to (314.4 to 868.4),
            "ciencias-humanas" to (289.9 to 823.0),
            "linguagens" to (287.0 to 820.8),
            "matematica" to (319.8 to 958.6),
        ),
        2022 to mapOf(
            "ciencias-natureza" to (308.7 to 875.3),
            "ciencias-humanas" to (287.5 to 839.2),
            "linguagens" to (269.9 to 801.0),
            "matematica" to (336.8 to 985.7),
        ),
        2021 to mapOf(
            "ciencias-natureza" to (307.6 to 867.1),
            "ciencias-humanas" to (311.6 to 846.9),
            "linguagens" to (295.2 to 826.1),
            "matematica" to (310.4 to 953.1),
        ),
        2020 to mapOf(
            "ciencias-natureza" to (323.8 to 854.8),
            "ciencias-humanas" to (313.7 to 862.6),
            "linguagens" to (288.7 to 801.1),
            "matematica" to (327.1 to 975.0),
        ),
        2019 to mapOf(
            "ciencias-natureza" to (327.9 to 860.9),
            "ciencias-humanas" to (315.9 to 835.1),
            "linguagens" to (322.0 to 801.7),
            "matematica" to (359.0 to 985.5),
        ),
        2018 to mapOf(
            "ciencias-natureza" to (362.5 to 869.6),
            "ciencias-humanas" to (387.2 to 850.4),
            "linguagens" to (318.8 to 816.9),
            "matematica" to (360.0 to 996.1),
        ),
        2017 to mapOf(
            "ciencias-natureza" to (298.0 to 885.6),
            "ciencias-humanas" to (307.7 to 868.3),
            "linguagens" to (299.6 to 788.6),
            "matematica" to (310.4 to 993.9),
        ),
        2016 to mapOf(
            "ciencias-natureza" to (316.5 to 871.3),
            "ciencias-humanas" to (317.4 to 859.1),
            "linguagens" to (287.5 to 846.4),
            "matematica" to (309.7 to 991.5),
        ),
        2015 to mapOf(
            "ciencias-natureza" to (334.3 to 875.2),
            "ciencias-humanas" to (314.3 to 850.6),
            "linguagens" to (302.6 to 825.8),
            "matematica" to (280.2 to 1008.3),
        ),
        2014 to mapOf(
            "ciencias-natureza" to (330.6 to 876.4),
            "ciencias-humanas" to (324.8 to 862.1),
            "linguagens" to (306.2 to 814.2),
            "matematica" to (318.5 to 973.6),
        ),
        2013 to mapOf(
            "ciencias-natureza" to (311.5 to 901.3),
            "ciencias-humanas" to (299.5 to 888.7),
            "linguagens" to (261.3 to 813.3),
            "matematica" to (322.4 to 971.5),
        ),
        2012 to mapOf(
            "ciencias-natureza" to (303.1 to 864.9),
            "ciencias-humanas" to (295.6 to 874.9),
            "linguagens" to (295.2 to 817.9),
            "matematica" to (277.2 to 955.2),
        ),
        2011 to mapOf(
            "ciencias-natureza" to (265.0 to 867.2),
            "ciencias-humanas" to (252.6 to 793.1),
            "linguagens" to (301.2 to 795.5),
            "matematica" to (321.6 to 953.0),
        ),
        2010 to mapOf(
            "ciencias-natureza" to (297.3 to 844.7),
            "ciencias-humanas" to (265.1 to 883.7),
            "linguagens" to (254.0 to 810.1),
            "matematica" to (313.4 to 973.2),
        ),
        2009 to mapOf(
            "ciencias-natureza" to (263.3 to 903.2),
            "ciencias-humanas" to (300.0 to 887.0),
            "linguagens" to (224.3 to 835.6),
            "matematica" to (345.9 to 985.1),
        ),
    )

    /** Quantas questões o ENEM tem por área (dia 1 + dia 2). */
    val TOTAL_QUESTOES_AREA = mapOf(
        "linguagens" to 45,
        "ciencias-humanas" to 45,
        "ciencias-natureza" to 45,
        "matematica" to 45,
    )

    /**
     * Nota estimada a partir dos acertos, por área.
     *
     * Usa a mediana medida quando existe (Matemática, Naturezas) e, nas outras,
     * interpola a curva "nota-alvo → acertos" ao contrário.
     */
    fun notaEstimada(area: String, acertos: Int): Int? {
        if (acertos <= 0) return null

        val medida = NOTA_POR_ACERTOS[area]
        if (medida != null) return _interpolar(acertos, medida)

        // Sem série medida: inverte a tabela de acertos por nota-alvo.
        val tabela = ACERTOS_POR_NOTA_ALVO[area] ?: return null
        val pontos = tabela.map { (nota, ac) -> ac to nota }
        return _interpolar(acertos, pontos)
    }

    /**
     * Quantos acertos a mediana precisou para chegar numa nota.
     */
    fun acertosParaNota(area: String, nota: Int): Int? {
        val tabela = ACERTOS_POR_NOTA_ALVO[area] ?: return null
        val acima = tabela.filter { it.first <= nota }
        if (acima.isEmpty()) return null
        // Entre dois pontos, o acerto necessário é o maior (com服务质量 conservador).
        return acima.maxByOrNull { it.first }?.second
    }

    /** A faixa de notas (corte) de uma área num ano. */
    fun faixaDoAno(ano: Int, area: String): Pair<Double, Double>? =
        FAIXA_POR_ANO[ano]?.get(area)

    /** Os anos que temos corte, do mais novo para o mais antigo. */
    val anosComCorte: List<Int> = FAIXA_POR_ANO.keys.sortedDescending()

    /**
     * Interpola linearmente numa série (x, y) crescente.
     */
    private fun _interpolar(x: Int, pontos: List<Pair<Int, Int>>): Int? {
        if (pontos.isEmpty()) return null
        val ordenados = pontos.sortedBy { it.first }
        if (x <= ordenados.first().first) return ordenados.first().second
        if (x >= ordenados.last().first) return ordenados.last().second
        for (i in 0 until ordenados.size - 1) {
            val (x0, y0) = ordenados[i]
            val (x1, y1) = ordenados[i + 1]
            if (x in x0..x1) {
                if (x1 == x0) return y0
                val t = (x - x0).toDouble() / (x1 - x0)
                return (y0 + t * (y1 - y0)).toInt()
            }
        }
        return ordenados.last().second
    }
}
