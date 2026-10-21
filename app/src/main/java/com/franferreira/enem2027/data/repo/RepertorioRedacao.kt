package com.franferreira.enem2027.data.repo

/**
 * Temas de redacao do ENEM e repertorio sugerido.
 *
 * Os temas aqui sao os que mais cairam e os que sao cobrados com mais
 * frequencia. O repertorio e o material que o aluno usa como apoio:
 * literatura, cinema, filosofia, historia - com a ancora de por que serve
 * para aquele tema.
 *
 * Isto e conteudo de ESTUDO. O enunciado oficial da prova vem do Inep;
 * aqui fica so o apoio para escrever.
 */
object RepertorioRedacao {

    data class Tema(
        val titulo: String,
        val enunciado: String,
        val repertorio: List<Repertorio>,
    )

    data class Repertorio(
        val obra: String,
        val tipo: String,      // literatura | cinema | filosofia | historia | sociologia
        val ancora: String,     // por que serve para esse tema
    )

    val temas: List<Tema> = listOf(
        Tema(
            titulo = "Impacto das redes sociais na saude mental dos jovens",
            enunciado = "Analise o impacto do uso das redes sociais na saude mental dos jovens.",
            repertorio = listOf(
                Repertorio(
                    obra = "O App -径",
                    tipo = "filme",
                    ancora = "Mostra como a rede molda a identidade e a saude mental.",
                ),
                Repertorio(
                    obra = "Frankl, Viktor - Em busca de sentido",
                    tipo = "filosofia",
                    ancora = "O vazio de sentido como motor da ansiedade.",
                ),
                Repertorio(
                    obra = "Bauman, Zygmunt - Modernidade liquida",
                    tipo = "sociologia",
                    ancora = "A inseguranca das relacoes na modernidade.",
                ),
            ),
        ),
        Tema(
            titulo = "Desigualdade de acesso a educacao no Brasil",
            enunciado = "Discuta os desafios para garantir o acesso a educacao de qualidade no Brasil.",
            repertorio = listOf(
                Repertorio(
                    obra = "Grande Sertao: Veredas - Guimaraes Rosa",
                    tipo = "literatura",
                    ancora = "A desigualdade que se revela no cotidiano do sertao.",
                ),
                Repertorio(
                    obra = "Flaherty - Louisiana Story",
                    tipo = "sociologia",
                    ancora = "A medicao do que nao se ve revela a desigualdade.",
                ),
                Repertorio(
                    obra = "LDB - Lei de Diretrizes e Bases",
                    tipo = "historia",
                    ancora = "Marco legal do acesso a educacao.",
                ),
            ),
        ),
        Tema(
            titulo = "Violencia contra a mulher no Brasil",
            enunciado = "Reflita sobre os desafios para o combate a violencia contra a mulher no Brasil.",
            repertorio = listOf(
                Repertorio(
                    obra = "Lei Maria da Penha - Lei 11.340/2006",
                    tipo = "historia",
                    ancora = "Marco legal do combate a violencia domestica.",
                ),
                Repertorio(
                    obra = "Tess - Fernando Dini de Almeida",
                    tipo = "literatura",
                    ancora = "A dominacao domestica atravessa os seculos.",
                ),
            ),
        ),
        Tema(
            titulo = "Descarte de residuos solidos e meio ambiente",
            enunciado = "Avalie os desafios impostos pelo descarte inadequado de residuos solidos ao meio ambiente.",
            repertorio = listOf(
                Repertorio(
                    obra = "Principio da responsabilidade - Hans Jonas",
                    tipo = "filosofia",
                    ancora = "A responsabilidade ambiental como dever etico.",
                ),
                Repertorio(
                    obra = "Lei 12.305/2010 - Politica Nacional de Residuos Solidos",
                    tipo = "historia",
                    ancora = "A base legal do tratamento e da logistica reversa.",
                ),
            ),
        ),
        Tema(
            titulo = "Influencia das redes sociais na formacao de opiniao",
            enunciado = "Analise a influencia das redes sociais na formacao de opinioes politicas no Brasil.",
            repertorio = listOf(
                Repertorio(
                    obra = "A Abolicao do homem - Aldous Huxley",
                    tipo = "literatura",
                    ancora = "Como a razao e substituida pelo costume.",
                ),
                Repertorio(
                    obra = "Habermas - Esfera publica",
                    tipo = "filosofia",
                    ancora = "O espaco de debate publico que as redes fragmentam.",
                ),
            ),
        ),
        Tema(
            titulo = "Valorizacao da cultura brasileira",
            enunciado = "Discuta a importancia da valorizacao da cultura brasileira no Brasil contemporaneo.",
            repertorio = listOf(
                Repertorio(
                    obra = "Grande Sertao: Veredas - Guimaraes Rosa",
                    tipo = "literatura",
                    ancora = "O rico vernacular do sertao como identidade.",
                ),
                Repertorio(
                    obra = "Os Sertoes - Euclides da Cunha",
                    tipo = "literatura",
                    ancora = "A questão da terra e da sobrevivencia.",
                ),
            ),
        ),
        Tema(
            titulo = "Inteligencia artificial e o mercado de trabalho",
            enunciado = "Avalie o impacto da inteligencia artificial no mercado de trabalho brasileiro.",
            repertorio = listOf(
                Repertorio(
                    obra = "Mais igual que diferente - Hannah Arendt",
                    tipo = "filosofia",
                    ancora = "O trabalho como criacao de valor.",
                ),
                Repertorio(
                    obra = "A tecnica e o medo - Gunther Anders",
                    tipo = "filosofia",
                    ancora = "A tecnica e o medo da obsolescencia.",
                ),
            ),
        ),
        Tema(
            titulo = "Fome e inseguranca alimentar no Brasil",
            enunciado = "Reflita sobre as causas da fome e da inseguranca alimentar no Brasil.",
            repertorio = listOf(
                Repertorio(
                    obra = "Os Sertoes - Euclides da Cunha",
                    tipo = "literatura",
                    ancora = "A questao da terra e da sobrevivencia.",
                ),
                Repertorio(
                    obra = "Direito humano a alimentacao - FAO",
                    tipo = "historia",
                    ancora = "Obrigacao do Estado e a seguranca alimentar.",
                ),
            ),
        ),
    )
}
