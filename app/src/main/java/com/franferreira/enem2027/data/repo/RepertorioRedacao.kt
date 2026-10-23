package com.franferreira.enem2027.data.repo

/**
 * Temas de redação do ENEM (2009–2024), com textos motivadores e repertório.
 *
 * Os títulos são os temas oficiais de cada ano. Os textos de apoio aqui são
 * **sínteses curtas** do debate de cada tema, escritas para estudar — não são
 * os recortes originais da prova (esses vêm do Inep e são longos).
 *
 * Alén dos temas, há o repertório por competência: é assim que a redação
 * realmente se resolve. C3 (argumentar com repertório) só funciona se houver
 * material que sustente cada parágrafo.
 */
object RepertorioRedacao {

    data class Tema(
        /** Tema oficial do ano. */
        val titulo: String,
        val ano: Int,
        /** Textos motivadores curtos, no espírito dos que o ENEM apresenta. */
        val apoio: List<String>,
        val repertorio: List<Repertorio>,
    )

    data class Repertorio(
        val obra: String,
        val tipo: String,      // literatura | cinema | filosofia | historia | sociologia
        val ancora: String,     // por que serve para esse tema
    )

    val porCompetencia: Map<Int, List<Repertorio>> = mapOf(
        2 to listOf(
            Repertorio("Orwell, George - 1984", "literatura", "O controle da informação dissolve a autonomia."),
            Repertorio("Bauman, Zygmunt - Modernidade líquida", "sociologia", "Laços frágeis e a insegurança do presente."),
            Repertorio("Han, Byung-Chul - Sociedade do cansaço", "filosofia", "O excesso de desempenho vira adoecimento."),
        ),
        3 to listOf(
            Repertorio("Rosa, Guimarães - Grande Sertão: Veredas", "literatura", "O sertão como território de desigualdade e invenção."),
            Repertorio("Cunha, Euclides - Os Sertões", "literatura", "A questão da terra e do abandono."),
            Repertorio("Santos, Milton - O espaço dividido", "sociologia", "O território é disputado de forma desigual."),
            Repertorio("Furtado, Celso - Formação econômica do Brasil", "historia", "A herança da concentração de renda."),
        ),
        4 to listOf(
            Repertorio("Constituição Federal de 1988", "historia", "Direitos sociais como base da intervenção."),
            Repertorio("Lei 12.305/2010 - Política Nacional de Resíduos Sólidos", "historia", "Logística reversa e responsabilidade compartilhada."),
            Repertorio("Lei Maria da Penha - Lei 11.340/2006", "historia", "Marco legal contra a violência doméstica."),
            Repertorio("ONU - Agenda 2030", "sociologia", "Metas globais que o Brasil assumiu."),
        ),
    )

    val temas: List<Tema> = listOf(
        Tema(
            titulo = "O indivíduo frente à ética nacional",
            ano = 2009,
            apoio = listOf(
                "A ética diz respeito às escolhas de cada um, mas ganha outra dimensão quando se torna assunto público.",
                "Pesquisas de opinião mostram que o brasileiro condena a corrupção em abstrato, mas tolera pequenos atalhos no cotidiano — o chamado \"jeitinho\".",
            ),
            repertorio = listOf(
                Repertorio("Kant - Fundamentação da Metafísica dos Costumes", "filosofia", "Agir pela regra que se deseja universal."),
                Repertorio("Aristóteles - Ética a Nicômaco", "filosofia", "A virtude como hábito, não como exceção."),
                Repertorio("Sennett, Richard - A corrosão do caráter", "sociologia", "O individualismo como efeito da vida moderna."),
            ),
        ),
        Tema(
            titulo = "O trabalho na construção da dignidade humana",
            ano = 2010,
            apoio = listOf(
                "A Constituição de 1988 reconhece o trabalho como direito social e fundamento da dignidade.",
                "Ao mesmo tempo, formas de trabalho análogas à escravidão ainda são resgatadas todos os anos no país.",
            ),
            repertorio = listOf(
                Repertorio("Marx, Karl - Manuscritos econômico-filosóficos", "filosofia", "O trabalho como expressão humana, não mercadoria."),
                Repertorio("Arendt, Hannah - A condição humana", "filosofia", "Trabalho, obra e ação como dimensões distintas."),
                Repertorio("CLT - Decreto-Lei 5.452/1943", "historia", "A regulação que sustenta o trabalho formal."),
            ),
        ),
        Tema(
            titulo = "Viver em rede no século XXI: os limites entre o público e o privado",
            ano = 2011,
            apoio = listOf(
                "Redes sociais transformaram a conversa de mesa em palco aberto, com plateia permanente.",
                "Especialistas discutem o que é legítimo compartilhar e o que deveria permanecer no âmbito privado.",
            ),
            repertorio = listOf(
                Repertorio("Habermas, Jürgen - Mudança estrutural da esfera pública", "filosofia", "O espaço público e seus limites."),
                Repertorio("Orwell, George - 1984", "literatura", "A vigilância como controle da vida privada."),
                Repertorio("Han, Byung-Chul - Sociedade da transparência", "filosofia", "A exposição voluntária como nova forma de controle."),
            ),
        ),
        Tema(
            titulo = "O movimento imigratório para o Brasil no século XXI",
            ano = 2012,
            apoio = listOf(
                "O Brasil recebeu, na última década, haitianos, venezuelanos, bolivianos e sírios, entre outros grupos.",
                "A chegada desses grupos levanta debates sobre acolhimento, documentação e inserção no mercado de trabalho.",
            ),
            repertorio = listOf(
                Repertorio("Sayad, Abdelmalek - A imigração ou os paradoxos da alteridade", "sociologia", "O estrangeiro entre acolhida e suspeita."),
                Repertorio("Lei 9.474/1997 - Estatuto do Refugiado", "historia", "O marco legal do refúgio no Brasil."),
                Repertorio("Bauman, Zygmunt - Estranhos à nossa porta", "sociologia", "O medo do outro na era global."),
            ),
        ),
        Tema(
            titulo = "Efeitos da implantação da Lei Seca no Brasil",
            ano = 2013,
            apoio = listOf(
                "A Lei 11.705/2008 proibiu a combinação de álcool e direção e endureceu a fiscalização.",
                "Estudos apontam queda nos acidentes, mas também discutem a efetividade da fiscalização nas cidades pequenas.",
            ),
            repertorio = listOf(
                Repertorio("Lei 11.705/2008 - Lei Seca", "historia", "A norma e seu mecanismo de dissuasão."),
                Repertorio("Hobbes, Thomas - Leviatã", "filosofia", "O Estado como limite à liberdade individual."),
                Repertorio("Durkheim, Émile - As regras do método sociológico", "sociologia", "A norma social e a coerção sobre o indivíduo."),
            ),
        ),
        Tema(
            titulo = "Publicidade infantil em questão no Brasil",
            ano = 2014,
            apoio = listOf(
                "O CONANDA considera abusiva a publicidade dirigida à criança, que ainda não distingue informação de persuasão.",
                "O setor publicitário defende a autorregulação; entidades de defesa da infância pedem regra legal específica.",
            ),
            repertorio = listOf(
                Repertorio("Consumo - Zygmunt Bauman", "sociologia", "O consumo como identidade na infância."),
                Repertorio("CDC - Lei 8.078/1990", "historia", "A proteção do consumidor como direito."),
                Repertorio("Adorno, Theodor - Indústria cultural", "filosofia", "A produção industrial do desejo."),
            ),
        ),
        Tema(
            titulo = "A persistência da violência contra a mulher na sociedade brasileira",
            ano = 2015,
            apoio = listOf(
                "A Lei Maria da Penha completou uma década, mas os índices de violência doméstica seguem altos.",
                "O Brasil ocupa posição preocupante em rankings internacionais de violência de gênero.",
            ),
            repertorio = listOf(
                Repertorio("Beauvoir, Simone de - O segundo sexo", "filosofia", "A construção social do gênero."),
                Repertorio("Lei Maria da Penha - Lei 11.340/2006", "historia", "O marco legal e seus mecanismos de proteção."),
                Repertorio("Bourdieu, Pierre - A dominação masculina", "sociologia", "A violência simbólica que naturaliza a hierarquia."),
            ),
        ),
        Tema(
            titulo = "Caminhos para combater a intolerância religiosa no Brasil",
            ano = 2016,
            apoio = listOf(
                "O Brasil é um dos países com mais denúncias de intolerância religiosa registradas por organismos de direitos humanos.",
                "As religiões de matriz africana concentram boa parte dos casos, o que revela a dimensão racial do problema.",
            ),
            repertorio = listOf(
                Repertorio("Constituição Federal de 1988 - art. 5º, VI", "historia", "A liberdade de crença como direito fundamental."),
                Repertorio("Freire, Paulo - Pedagogia do oprimido", "filosofia", "O diálogo como caminho contra a exclusão."),
                Repertorio("Munanga, Kabengele - Rediscutindo a mestiçagem no Brasil", "sociologia", "O racismo por trás da intolerância religiosa."),
            ),
        ),
        Tema(
            titulo = "Desafios para a formação educacional de surdos no Brasil",
            ano = 2017,
            apoio = listOf(
                "A Lei Brasileira de Inclusão (2015) garantiu direitos, mas a oferta de educação bilíngue ainda é desigual.",
                "O debate opõe a inclusão em escolas regulares e a valorização da Libras como primeira língua.",
            ),
            repertorio = listOf(
                Repertorio("Lei 10.436/2002 - Libras", "historia", "O reconhecimento da língua como direito."),
                Repertorio("Lei 13.146/2015 - Lei Brasileira de Inclusão", "historia", "A inclusão como obrigação do Estado."),
                Repertorio("Sacks, Oliver - Vendo vozes", "filosofia", "A surdez como cultura, não como falta."),
            ),
        ),
        Tema(
            titulo = "Manipulação do comportamento do usuário pelo controle de dados na internet",
            ano = 2018,
            apoio = listOf(
                "Algoritmos moldam o que cada usuário vê, a partir de dados coletados sem transparência.",
                "Casos de vazamento e de uso político de dados expuseram a fragilidade da proteção no Brasil e no mundo.",
            ),
            repertorio = listOf(
                Repertorio("Orwell, George - 1984", "literatura", "O controle da informação como controle do real."),
                Repertorio("LGPD - Lei 13.709/2018", "historia", "A proteção de dados como direito."),
                Repertorio("Han, Byung-Chul - Sociedade da transparência", "filosofia", "A vigilância que o usuário aceita."),
            ),
        ),
        Tema(
            titulo = "Democratização do acesso ao cinema no Brasil",
            ano = 2019,
            apoio = listOf(
                "Grande parte dos municípios brasileiros não tem sala de cinema.",
                "O streaming ampliou o acesso, mas não substitui a experiência coletiva nem resolve a concentração de produção.",
            ),
            repertorio = listOf(
                Repertorio("Adorno, Theodor - Indústria cultural", "filosofia", "O cinema como mercadoria e como arte."),
                Repertorio("Bernardet, Jean-Claude - Cinema brasileiro: propostas para uma história", "historia", "A formação do cinema nacional."),
                Repertorio("Ancine - dados do setor", "sociologia", "A concentração de salas nas capitais."),
            ),
        ),
        Tema(
            titulo = "O estigma associado às doenças mentais na sociedade brasileira",
            ano = 2020,
            apoio = listOf(
                "A depressão e a ansiedade cresceram entre jovens, mas o preconceito ainda impede a busca por ajuda.",
                "A pandemia escancarou o problema e o debate sobre saúde mental no trabalho.",
            ),
            repertorio = listOf(
                Repertorio("Foucault, Michel - História da loucura", "filosofia", "O estigma como forma de exclusão."),
                Repertorio("Lei 10.216/2001 - Reforma Psiquiátrica", "historia", "O cuidado em liberdade no lugar do isolamento."),
                Repertorio("Han, Byung-Chul - Sociedade do cansaço", "filosofia", "O adoecimento como falha individualizada."),
            ),
        ),
        Tema(
            titulo = "Invisibilidade e registro civil: garantia de acesso à cidadania no Brasil",
            ano = 2021,
            apoio = listOf(
                "Há brasileiros sem certidão de nascimento, o que os impede de matricular-se, trabalhar e votar formalmente.",
                "O registro civil é a porta de entrada para os demais direitos.",
            ),
            repertorio = listOf(
                Repertorio("Constituição Federal de 1988", "historia", "A cidadania como fundamento."),
                Repertorio("Marshall, T. H. - Cidadania e classe social", "sociologia", "Direitos civis como base dos demais."),
                Repertorio("Lei 6.015/1973 - Registros Públicos", "historia", "O instrumento legal do registro."),
            ),
        ),
        Tema(
            titulo = "Desafios para a valorização de comunidades e povos tradicionais no Brasil",
            ano = 2022,
            apoio = listOf(
                "Comunidades indígenas, quilombolas, ribeirinhas e extrativistas preservam modos de vida ligados ao território.",
                "A pressão sobre a terra e a falta de políticas específicas ameaçam essas culturas.",
            ),
            repertorio = listOf(
                Repertorio("Krenak, Ailton - Ideias para adiar o fim do mundo", "literatura", "A voz indígena e a crítica ao progresso."),
                Repertorio("Castro, Eduardo Viveiros de - A inconstância da alma selvagem", "sociologia", "Outras formas de habitar o mundo."),
                Repertorio("Convenção 169 da OIT", "historia", "Consulta prévia e direitos territoriais."),
            ),
        ),
        Tema(
            titulo = "Desafios para o enfrentamento da invisibilidade do trabalho de cuidado realizado pela mulher no Brasil",
            ano = 2023,
            apoio = listOf(
                "Cuidar de crianças, idosos e pessoas com deficiência é trabalho — e recai majoritariamente sobre mulheres.",
                "Esse trabalho não é remunerado nem reconhecido como ocupação em grande parte dos casos.",
            ),
            repertorio = listOf(
                Repertorio("Federici, Silvia - Calibã e a bruxa", "sociologia", "O trabalho reprodutivo invisibilizado."),
                Repertorio("Beauvoir, Simone de - O segundo sexo", "filosofia", "A mulher e o papel social imposto."),
                Repertorio("IBGE - Pesquisa Nacional por Amostra de Domicílios", "historia", "Os dados do trabalho não remunerado."),
            ),
        ),
        Tema(
            titulo = "Desafios para a valorização da herança africana no Brasil",
            ano = 2024,
            apoio = listOf(
                "A herança africana está na língua, na música, na culinária e na religiosidade brasileiras.",
                "Ainda assim, o racismo estrutural e o apagamento dessa herança persistem nas escolas e nos espaços públicos.",
            ),
            repertorio = listOf(
                Repertorio("Munanga, Kabengele - Rediscutindo a mestiçagem no Brasil", "sociologia", "A construção do racismo à brasileira."),
                Repertorio("Gonzalez, Lélia - Por um feminismo afro-latino-americano", "filosofia", "A categoria político-cultural da amefricanidade."),
                Repertorio("Lei 10.639/2003", "historia", "O ensino obrigatório de história e cultura afro-brasileira."),
            ),
        ),
    )
}