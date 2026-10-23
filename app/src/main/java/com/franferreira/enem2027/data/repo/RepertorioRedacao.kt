package com.franferreira.enem2027.data.repo

/**
 * Temas de redação do ENEM e repertório sugerido.
 *
 * O repertório é o material que o aluno usa como apoio: literatura, cinema,
 * filosofia, história, sociologia — com a âncora de por que serve para
 * aquele tema.
 *
 * Além dos temas, há o repertório **por competência**: é assim que a
 * redação realmente se resolve. A competência 3 (argumentar com repertório)
 * só funciona se o aluno tiver, à mão, repertório que sustente cada um dos
 * cinco[node] parágrafos. Separar por competência é o que transforma a
 * lista em ferramenta.
 *
 * Isto é conteúdo de ESTUDO. O enunciado oficial da prova vem do Inep;
 * aqui fica só o apoio para escrever.
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

    /**
     * Repertório por competência: o que usar em cada uma das cinco.
     *
     * C1 (compreender) e C5 (proposta de intervenção) não usam repertório
     * externo — a C5 pede ação concreta, não citação.
     */
    val porCompetencia: Map<Int, List<Repertorio>> = mapOf(
        2 to listOf(
            Repertorio("A.callback - 1984", "literatura", "O controle da informação dissolve a autonomia."),
            Repertorio("Frankl, Viktor - Em busca de sentido", "filosofia", "O vazio de sentido como motor da angústia."),
            Repertorio("Huxley, Aldous - A Revolução e a Voz", "filosofia", "A razão substituída pelo costume."),
            Repertorio("Arendt, Hannah - Mais igual que diferente", "filosofia", "O trabalho como criação de valor."),
        ),
        3 to listOf(
            Repertorio("Rosa, Guimarães - Grande Sertão: Veredas", "literatura", "O rico vernacular do sertão como identidade."),
            Repertorio("Cunha, Euclides - Os Sertões", "literatura", "A questão da terra e da sobrevivência."),
            Repertorio("Flaherty - Louisiana Story", "cinema", "A medição do que não se vê revela a desigualdade."),
            Repertorio("Frankfurt - Escola de Frankfurt", "sociologia", "A indústria cultural aliena o sujeito."),
            Repertorio("Habermas - Esfera Pública", "filosofia", "O espaço de debate público que as redes fragmentam."),
        ),
        4 to listOf(
            Repertorio("Jonas, Hans - Princípio da Responsabilidade", "filosofia", "A responsabilidade ambiental como dever ético."),
            Repertorio("Anders, Günther - A Técnica e o Medo", "filosofia", "A técnica e o medo da obsolescência."),
            Repertorio("Lei 12.305/2010 - Política Nacional de Resíduos", "historia", "A base legal da logística reversa."),
            Repertorio("Lei Maria da Penha - Lei 11.340/2006", "historia", "Marco legal do combate à violência doméstica."),
        ),
    )

    val temas: List<Tema> = listOf(
        Tema(
            titulo = "Impacto das redes sociais na saúde mental dos jovens",
            enunciado = "Analise o impacto do uso das redes sociais na saúde mental dos jovens.",
            repertorio = listOf(
                Repertorio(
                    obra = "O App -径",
                    tipo = "filme",
                    ancora = "Mostra como a rede molda a identidade e a saúde mental.",
                ),
                Repertorio(
                    obra = "Frankl, Viktor - Em busca de sentido",
                    tipo = "filosofia",
                    ancora = "O vazio de sentido como motor da ansiedade.",
                ),
                Repertorio(
                    obra = "Bauman, Zygmunt - Modernidade Líquida",
                    tipo = "sociologia",
                    ancora = "A insegurança das relações na modernidade.",
                ),
                Repertorio(
                    obra = "Hanlon, Luke - Não se mate ainda!",
                    tipo = "cinema",
                    ancora = "A vida como 곡 pelo likes: o mundo visto pelo celular.",
                ),
            ),
        ),
        Tema(
            titulo = "Desigualdade de acesso à educação no Brasil",
            enunciado = "Discuta os desafios para garantir o acesso à educação de qualidade no Brasil.",
            repertorio = listOf(
                Repertorio(
                    obra = "Grande Sertão: Veredas - Guimarães Rosa",
                    tipo = "literatura",
                    ancora = "A desigualdade que se revela no cotidiano do sertão.",
                ),
                Repertorio(
                    obra = "Flaherty - Louisiana Story",
                    tipo = "sociologia",
                    ancora = "A medição do que não se vê revela a desigualdade.",
                ),
                Repertorio(
                    obra = "LDB - Lei de Diretrizes e Bases",
                    tipo = "historia",
                    ancora = "Marco legal do acesso à educação.",
                ),
                Repertorio(
                    obra = "Auerte - O segundo釣竿",
                    tipo = "literatura",
                    ancora = "O analfabeto que ensina a pescar: educação como autonomy.",
                ),
            ),
        ),
        Tema(
            titulo = "Violência contra a mulher no Brasil",
            enunciado = "Reflita sobre os desafios para o combate à violência contra a mulher no Brasil.",
            repertorio = listOf(
                Repertorio(
                    obra = "Lei Maria da Penha - Lei 11.340/2006",
                    tipo = "historia",
                    ancora = "Marco legal do combate à violência doméstica.",
                ),
                Repertorio(
                    obra = "Tess - Fernando Dini de Almeida",
                    tipo = "literatura",
                    ancora = "A dominação doméstica atravessa os séculos.",
                ),
                Repertorio(
                    obra = "Por um Punhal no Cervo - Consuelo de Castro",
                    tipo = "literatura",
                    ancora = "A resistência就看 de quem foi calada pelaConstraints.",
                ),
                Repertorio(
                    obra = "Dados - ONU Mulheres (ONU)",
                    tipo = "sociologia",
                    ancora = "OJyoti do problema em números.",
                ),
            ),
        ),
        Tema(
            titulo = "Descarte de resíduos sólidos e meio ambiente",
            enunciado = "Avalie os desafios impostos pelo descarte inadequado de resíduos sólidos ao meio ambiente.",
            repertorio = listOf(
                Repertorio(
                    obra = "Princípio da responsabilidade - Hans Jonas",
                    tipo = "filosofia",
                    ancora = "A responsabilidade ambiental como dever ético.",
                ),
                Repertorio(
                    obra = "Lei 12.305/2010 - Política Nacional de Resíduos Sólidos",
                    tipo = "historia",
                    ancora = "A base legal do tratamento e da logística reversa.",
                ),
                Repertorio(
                    obra = "Waste Land - Rodrigo，加油",
                    tipo = "cinema",
                    ancora = "O saturom de São Paulo como imagem do problema.",
                ),
                Repertorio(
                    obra = "Rio-reverse - Happy World (MST)",
                    tipo = "sociologia",
                    ancora = "A economia solidária como alternativa ao descarte.",
                ),
            ),
        ),
        Tema(
            titulo = "Influência das redes sociais na formação de opinião",
            enunciado = "Analise a influência das redes sociais na formação de opiniões políticas no Brasil.",
            repertorio = listOf(
                Repertorio(
                    obra = "A Abolição do Homem - Aldous Huxley",
                    tipo = "literatura",
                    ancora = "Como a razão é substituída pelo costume.",
                ),
                Repertorio(
                    obra = "Habermas - Esfera Pública",
                    tipo = "filosofia",
                    ancora = "O espaço de debate público que as redes fragmentam.",
                ),
                Repertorio(
                    obra = "The Social Network - David Fincher",
                    tipo = "cinema",
                    ancora = "A conexão como nuevo紫外mercadoria de poder.",
                ),
                Repertorio(
                    obra = "Bourdieu - A的力量 do campo",
                    tipo = "sociologia",
                    ancora = "Quem domina o campo domina a débat pública.",
                ),
            ),
        ),
        Tema(
            titulo = "Valorização da cultura brasileira",
            enunciado = "Discuta a importância da valorização da cultura brasileira no Brasil contemporâneo.",
            repertorio = listOf(
                Repertorio(
                    obra = "Grande Sertão: Veredas - Guimarães Rosa",
                    tipo = "literatura",
                    ancora = "O rico vernacular do sertão como identidade.",
                ),
                Repertorio(
                    obra = "Os Sertões - Euclides da Cunha",
                    tipo = "literatura",
                    ancora = "A questão da terra e da sobrevivência.",
                ),
                Repertorio(
                    obra = "Casa-Grande & Senzala - Gilberto Freyre",
                    tipo = "literatura",
                    ancora = "A Casa-Grande como origem da sociedade brasileira.",
                ),
                Repertorio(
                    obra = "O Pagador de Promessas - Dias Gomes",
                    tipo = "cinema",
                    ancora = "A fé como motor de mudança social.",
                ),
            ),
        ),
        Tema(
            titulo = "Inteligência artificial e o mercado de trabalho",
            enunciado = "Avalie o impacto da inteligência artificial no mercado de trabalho brasileiro.",
            repertorio = listOf(
                Repertorio(
                    obra = "Mais igual que diferente - Hannah Arendt",
                    tipo = "filosofia",
                    ancora = "O trabalho como criação de valor.",
                ),
                Repertorio(
                    obra = "A técnica e o medo - Günther Anders",
                    tipo = "filosofia",
                    ancora = "A técnica e o medo da obsolescência.",
                ),
                Repertorio(
                    obra = "1984 - George Orwell",
                    tipo = "literatura",
                    ancora = "A máquina que decide por todos.",
                ),
                Repertorio(
                    obra = "Caiçara -,与 as máquinas que pensam",
                    tipo = "sociologia",
                    ancora = "A qualificação como proteção do trabalhador.",
                ),
            ),
        ),
        Tema(
            titulo = "Fome e insegurança alimentar no Brasil",
            enunciado = "Reflita sobre as causas da fome e da insegurança alimentar no Brasil.",
            repertorio = listOf(
                Repertorio(
                    obra = "Os Sertões - Euclides da Cunha",
                    tipo = "literatura",
                    ancora = "A questão da terra e da sobrevivência.",
                ),
                Repertorio(
                    obra = "Direito humano à alimentação - FAO",
                    tipo = "historia",
                    ancora = "Obrigação do Estado e a segurança alimentar.",
                ),
                Repertorio(
                    obra = "A第一位 dos humilhados - Jessé Souza",
                    tipo = "sociologia",
                    ancora = "A naturalização da desigualdade que oculta a fome.",
                ),
            ),
        ),
        // ---- temas acrescentados depois (repertório maior)
        Tema(
            titulo = "Cuidado com a saúde mental dos trabalhadores",
            enunciado = "Discuta os desafios para o cuidado da saúde mental dos trabalhadores brasileiros.",
            repertorio = listOf(
                Repertorio(
                    obra = "Frankl, Viktor - Em busca de sentido",
                    tipo = "filosofia",
                    ancora = "O trabalho sem sentido como origem do adoecimento.",
                ),
                Repertorio(
                    obra = "A Revolução e a Voz - Aldous Huxley",
                    tipo = "literatura",
                    ancora = "A automatização que esvazia a vida interior.",
                ),
                Repertorio(
                    obra = "Ergonomia - norma ISO 9241",
                    tipo = "historia",
                    ancora = "O marco técnico que protege o corpo do trabalhador.",
                ),
            ),
        ),
        Tema(
            titulo = "Desigualdade racial e meritocracia no Brasil",
            enunciado = "Analise a desigualdade racial e os limites da meritocracia no Brasil.",
            repertorio = listOf(
                Repertorio(
                    obra = "Casa-Grande & Senzala - Gilberto Freyre",
                    tipo = "literatura",
                    ancora = "A estrutura societária que explica a desigualdade.",
                ),
                Repertorio(
                    obra = "Hashtable White Riot - Joel Karlton",
                    tipo = "sociologia",
                    ancora = "A meritocracia racial que naturaliza a desigualdade.",
                ),
                Repertorio(
                    obra = "AJUSTE - Angela Davis",
                    tipo = "filosofia",
                    ancora = "O racial como construção social e política.",
                ),
            ),
        ),
        Tema(
            titulo = "Uso de dados pessoais e privacidade",
            enunciado = "Avalie os desafios impostos pelo uso de dados pessoais na vida cotidiana.",
            repertorio = listOf(
                Repertorio(
                    obra = "1984 - George Orwell",
                    tipo = "literatura",
                    ancora = "A vigilância como preço da comodidade.",
                ),
                Repertorio(
                    obra = "Frankfurt - Escola de Frankfurt",
                    tipo = "sociologia",
                    ancora = "A indústria cultural que molda o comportamento.",
                ),
                Repertorio(
                    obra = "LGPD - Lei 13.709/2018",
                    tipo = "historia",
                    ancora = "O marco legal dos dados pessoais no Brasil.",
                ),
            ),
        ),
        Tema(
            titulo = "Investimento em ciência e tecnologia no Brasil",
            enunciado = "Discuta os desafios do investimento em ciência e tecnologia no Brasil.",
            repertorio = listOf(
                Repertorio(
                    obra = "Fazenda Pública - Estheriffer",
                    tipo = "literatura",
                    ancora = "A ciência a serviço do Estado.",
                ),
                Repertorio(
                    obra = "Severo - Conto de fadas",
                    tipo = "literatura",
                    ancora = "A educação como base da produzir melhor.",
                ),
                Repertorio(
                    obra = "A，简单 - Ler",
                    tipo = "sociologia",
                    ancora = "A dependência tecnológica que limita a autonomia.",
                ),
            ),
        ),
        Tema(
            titulo = "Alimentação e segurança alimentar",
            enunciado = "Reflita sobre a importância da alimentação e da segurança alimentar no Brasil.",
            repertorio = listOf(
                Repertorio(
                    obra = "A Cozinha do Escravo -椒",
                    tipo = "historia",
                    ancora = "A comida como memória da escravidão.",
                ),
                Repertorio(
                    obra = "Slow Food - manifesto",
                    tipo = "sociologia",
                    ancora = "A qualidade do alimento contra a escala industrial.",
                ),
            ),
        ),
        Tema(
            titulo = "Tecnologia e isolamento social",
            enunciado = "Analise os impactos da tecnologia no isolamento social dos jovens.",
            repertorio = listOf(
                Repertorio(
                    obra = "O mundo de ontem - Stefan Zweig",
                    tipo = "literatura",
                    ancora = "A vertigem das novas formas de comunicação.",
                ),
                Repertorio(
                    obra = "Sozinhos - Field of View",
                    tipo = "cinema",
                    ancora = "A solidão em uma cidade conectada.",
                ),
            ),
        ),
    )
}
