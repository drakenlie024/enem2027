package com.franferreira.enem2027.data.repo

/**
 * Canais e playlists de aula gratuita, por disciplina.
 *
 * A lista veio do portal Facilita ENEM (UNEMAT, 2022, CC BY-SA 4.0), que
 * seleciona um canal do YouTube por disciplina do ENEM. O app usa isso como
 * ponte com a vídeo-aula: quando a pessoa trava numa questão, o caminho até
 * a aula está a um toque.
 *
 * Três desses playlists são cursos completos e gratuitos de preparação (a
 * Kultivi publishes "curso gratuito completo" de cada área), o que é raro.
 */
object CanaisVideo {

    data class Canal(
        val nome: String,
        val descricao: String,
        val url: String,
    )

    /**
     * Canais por área do ENEM (as mesmas chaves que o acervo usa).
     *
     * Um mesmo canal pode aparecer em mais de uma área — a Kultivi tem curso
     * separado de Biologia, Química, Física, Português, Inglês, Espanhol,
     * Artes e Educação Física.
     */
    private val POR_AREA: Map<String, List<Canal>> = mapOf(
        "matematica" to listOf(
            Canal(
                nome = "Professor Ferretto",
                descricao = "Matemática para ENEM e vestibulares, aula por aula",
                url = "https://www.youtube.com/playlist?list=PLTPg64KdGgYivEK9avhUlxsaJhD0TfpxW",
            ),
        ),
        "linguagens" to listOf(
            Canal(
                nome = "Kultivi — Português",
                descricao = "Curso gratuito completo de Português para o ENEM",
                url = "https://www.youtube.com/playlist?list=PL8vXuI6zmpdiy2Xhd1Tn4_q974HxDUzox",
            ),
            Canal(
                nome = "Poxalulu — Redação",
                descricao = "Método de redação para tirar nota mil",
                url = "https://www.youtube.com/playlist?list=PLhFNxzLJLH3sUihLp7SpjYyf150XcbBpS",
            ),
            Canal(
                nome = "Kultivi — Espanhol",
                descricao = "Espanhol do ENEM: o que o avaliador espera",
                url = "https://www.youtube.com/playlist?list=PL8vXuI6zmpdgygmHqu3IYXcMBQTJhmLJc",
            ),
            Canal(
                nome = "Kultivi — Inglês",
                descricao = "Inglês do ENEM: gramática e interpretação",
                url = "https://www.youtube.com/playlist?list=PL8vXuI6zmpdi5r_8AR3AtCFmj2RXo5ve0",
            ),
        ),
        "ciencias-humanas" to listOf(
            Canal(
                nome = "Parabólica — História",
                descricao = "História do Brasil e do mundo, aula por aula",
                url = "https://www.youtube.com/playlist?list=PLMra4G0-Z7pMYLE-D-ptnHt1IW_Y1hn8H",
            ),
            Canal(
                nome = "Parabólica — Filosofia",
                descricao = "Filósofos e correntes que caem na prova",
                url = "https://www.youtube.com/playlist?list=PLMra4G0-Z7pOBZjKCxen5cRa6iUMAdYKh",
            ),
            Canal(
                nome = "Parabólica — Sociologia",
                descricao = "Conceitos de Sociologia para o ENEM",
                url = "https://www.youtube.com/playlist?list=PLMra4G0-Z7pMnBecUK6b8VpwDceBI7xcE",
            ),
            Canal(
                nome = "Prof Silvester — Geografia",
                descricao = "Geografia para o ENEM, do básico ao advanced",
                url = "https://www.youtube.com/playlist?list=PL2Znz6U0vJHUN75Ujt-HOzJilV4v-xX79",
            ),
            Canal(
                nome = "Kultivi — Artes",
                descricao = "Artes do ENEM: história da arte e leitura de obra",
                url = "https://www.youtube.com/playlist?list=PL8vXuI6zmpdialQfulCLVGKat0DwB6fKX",
            ),
        ),
        "ciencias-natureza" to listOf(
            Canal(
                nome = "Kultivi — Biologia",
                descricao = "Curso gratuito completo de Biologia para o ENEM",
                url = "https://www.youtube.com/playlist?list=PL8vXuI6zmpdgu7TOyarRBU42MXOrUJnOS",
            ),
            Canal(
                nome = "Kultivi — Química",
                descricao = "Curso gratuito completo de Química para o ENEM",
                url = "https://www.youtube.com/playlist?list=PL8vXuI6zmpdj_YFEHTaBDccdSCC1LVNH0",
            ),
            Canal(
                nome = "Kultivi — Física",
                descricao = "Curso gratuito completo de Física para o ENEM",
                url = "https://www.youtube.com/playlist?list=PL8vXuI6zmpdiG6QR-LpXXbUYzPz5rOhF2",
            ),
            Canal(
                nome = "Kultivi — Educação Física",
                descricao = "Ed. Física do ENEM: padrões estéticos contemporâneos",
                url = "https://www.youtube.com/playlist?list=PL8vXuI6zmpdgRR60A9YGAgwkwnf9QTbsw",
            ),
        ),
    )

    /** Canais de uma área. Vazio se a área não estiver mapeada. */
    fun daArea(area: String?): List<Canal> =
        if (area.isNullOrBlank()) emptyList() else POR_AREA[area].orEmpty()

    /**
     * Canal por MATÉRIA (biologia, história, matemática…).
     *
     * É o que resolve os temas sem aula no acervo: a playlist da matéria é o
     * caminho mais próximo de uma aula, sem inventar vídeo que pode sair do ar.
     */
    private val POR_MATERIA: Map<String, Canal> = run {
        val mapa = mutableMapOf<String, Canal>()
        POR_AREA["matematica"].orEmpty().firstOrNull()?.let { mapa["matematica"] = it }
        POR_AREA["linguagens"].orEmpty().forEach { canal ->
            when {
                canal.nome.contains("Português") -> mapa["portugues"] = canal
                canal.nome.contains("Inglês") -> mapa["ingles"] = canal
                canal.nome.contains("Espanhol") -> mapa["espanhol"] = canal
                canal.nome.contains("Redação") -> mapa["redacao"] = canal
            }
        }
        POR_AREA["ciencias-humanas"].orEmpty().forEach { canal ->
            when {
                canal.nome.contains("História") -> mapa["historia"] = canal
                canal.nome.contains("Filosofia") -> mapa["filosofia"] = canal
                canal.nome.contains("Sociologia") -> mapa["sociologia"] = canal
                canal.nome.contains("Geografia") -> mapa["geografia"] = canal
            }
        }
        POR_AREA["ciencias-natureza"].orEmpty().forEach { canal ->
            when {
                canal.nome.contains("Biologia") -> mapa["biologia"] = canal
                canal.nome.contains("Química") -> mapa["quimica"] = canal
                canal.nome.contains("Física") -> mapa["fisica"] = canal
            }
        }
        // Ciências da Terra (geologia, astronomia, clima) cai melhor em
        // Geografia do que em Física.
        mapa["geografia"]?.let { mapa["ciencias-da-terra"] = it }
        mapa
    }

    /** Canal da matéria, se houver um mapeado. */
    fun daMateria(materia: String?): Canal? =
        if (materia.isNullOrBlank()) null else POR_MATERIA[materia]

    /**
     * Canais de uma área, com o canal mais provável de ajudar primeiro.
     *
     * A ordem importa: o primeiro é o que a pessoa vai abrir no automático.
     */
    fun principalDaArea(area: String?): Canal? = daArea(area).firstOrNull()

    /** Todas as áreas que têm canal mapeado. */
    val areasComCanal: List<String> get() = POR_AREA.keys.toList()
}
