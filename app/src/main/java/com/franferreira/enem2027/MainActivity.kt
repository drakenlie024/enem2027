package com.franferreira.enem2027

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.franferreira.enem2027.ui.ViewModelPrincipal
import com.franferreira.enem2027.ui.screens.TelaAgenda
import com.franferreira.enem2027.ui.components.DivisorRabisco
import androidx.compose.foundation.layout.Arrangement
import com.franferreira.enem2027.ui.screens.TelaAbertura
import com.franferreira.enem2027.ui.screens.TelaAprovacao
import com.franferreira.enem2027.ui.screens.TelaEtapa
import com.franferreira.enem2027.ui.screens.TelaHoje
import com.franferreira.enem2027.ui.screens.TelaLogin
import com.franferreira.enem2027.ui.screens.TelaMontarSimulado
import com.franferreira.enem2027.ui.screens.TelaProgresso
import com.franferreira.enem2027.data.model.PASSO_MINIMO_PCT
import com.franferreira.enem2027.ui.screens.TelaAula
import com.franferreira.enem2027.ui.screens.TelaAulas
import com.franferreira.enem2027.ui.screens.TelaAulasDeArea
import com.franferreira.enem2027.ui.screens.TelaCaderno
import com.franferreira.enem2027.ui.screens.TelaCronograma
import com.franferreira.enem2027.ui.screens.TelaDiagnostico
import com.franferreira.enem2027.ui.screens.TelaOnboarding
import com.franferreira.enem2027.ui.screens.TelaResultadoDiagnostico
import com.franferreira.enem2027.ui.screens.TelaNota
import com.franferreira.enem2027.ui.screens.TelaQuestao
import com.franferreira.enem2027.ui.screens.TelaRedacao
import com.franferreira.enem2027.ui.screens.TelaResultadoSimulado
import com.franferreira.enem2027.ui.screens.TelaRevisao
import com.franferreira.enem2027.ui.screens.TelaRevisaoSimulado
import com.franferreira.enem2027.ui.screens.ResumoEtapa
import com.franferreira.enem2027.ui.screens.TelaSimulado
import com.franferreira.enem2027.ui.screens.TelaTrilha
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.EnemTheme
import com.franferreira.enem2027.ui.theme.Primary
import com.franferreira.enem2027.ui.theme.Surface
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextPrimary
import com.franferreira.enem2027.ui.theme.corDaArea

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EnemTheme {
                EnemApp()
            }
        }
    }
}

private sealed class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Hoje : Destination("hoje", "Hoje", Icons.Filled.Home)
    data object Aulas : Destination("aulas", "Aulas", Icons.Filled.PlayArrow)
    data object Revisao : Destination("revisao", "Revisão", Icons.Filled.CheckCircle)
    data object Redacao : Destination("redacao", "Redação", Icons.Filled.Edit)
    data object Progresso : Destination("progresso", "Progresso", Icons.Filled.Insights)
    data object Agenda : Destination("agenda", "Agenda", Icons.Filled.CalendarMonth)
    data object Simulado : Destination("simulado", "Simulado", Icons.Filled.Timer)
    data object Aprovacao : Destination("aprovacao", "Aprovação", Icons.Filled.FactCheck)
}

private val destinations = listOf(
    Destination.Hoje,
    Destination.Aulas,
    Destination.Simulado,
    Destination.Redacao,
    Destination.Progresso,
)

@Composable
fun EnemApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val vm: ViewModelPrincipal = viewModel()
    val estado by vm.estado.collectAsStateSafe()

    val rotaAtual = currentDestination?.route

    // Mensagem é de uma ação, não da sessão inteira: ao trocar de tela ela
    // some. Antes, "12 respostas sincronizadas" reaparecia na Agenda como se
    // fosse confirmação do "Salvar agenda".
    LaunchedEffect(rotaAtual) { vm.limparMensagem() }

    if (!estado.autenticado) {
        // Enquanto a sessão não foi conferida, nada de login: ele piscava na
        // tela antes de o app saber que a pessoa já tinha sessão.
        if (!estado.sessaoVerificada) {
            TelaAbertura()
            return
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
        ) {
            TelaLogin(
                aoComecar = vm::comecarSemConta,
                aoEntrar = vm::autenticar,
            )
        }
        return
    }

    // Primeiro acesso: metas, agenda e notificação, todas puláveis.
    estado.onboardingEtapa?.let { etapa ->
        TelaOnboarding(
            etapa = etapa,
            agenda = estado.agenda,
            aoSalvarMetas = vm::salvarMetasOnboarding,
            aoAlternarAgenda = vm::alternarAgenda,
            aoSalvarAgenda = vm::salvarAgendaOnboarding,
            aoPular = vm::pularEtapaOnboarding,
            aoAvancar = vm::avancarOnboarding,
        )
        return
    }

    // Diagnóstico de entrada: enquanto estiver pendente, o app fica só nele.
    if (estado.diagPendente) {
        val resultado = estado.diagResultado
        val questao = estado.diagQuestoes.getOrNull(estado.diagIndice)
        when {
            resultado != null -> TelaResultadoDiagnostico(
                resultado = resultado,
                previsao = estado.diagPrevisao,
                notaAlvo = estado.notaAlvo,
                aoComecar = vm::concluirDiagnostico,
            )
            questao != null -> TelaDiagnostico(
                questao = questao,
                indice = estado.diagIndice,
                total = estado.diagQuestoes.size,
                podeSair = !estado.diagObrigatorio,
                aoSair = vm::cancelarDiagnostico,
                aoConfirmar = vm::responderDiagnostico,
            )
            else -> Box(Modifier.fillMaxSize().background(Background))
        }
        return
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            // Telas de prova ficam sem a barra: sair de um simulado no meio por
            // um toque na aba jogava fora as respostas marcadas. Mas a tela de
            // MONTAR o simulado não é prova: ela mantém a barra.
            val emProva = rotaAtual == "simulado" &&
                (estado.simulado != null || estado.diagnostico != null)
            val semBarra = rotaAtual in setOf("questao", "etapa", "aulapacote") || emProva
            if (!semBarra && estado.autenticado) {
                // Barra inferior desenhada à mão: fundo creme, separador
                // rabiscado e rótulo na fonte manuscrita, como no DESIGN.md.
                Column(Modifier.background(Surface)) {
                    DivisorRabisco()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        destinations.forEach { destino ->
                            val selecionado =
                                currentDestination?.hierarchy?.any { it.route == destino.route } == true
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        navController.navigate(destino.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Icon(
                                    imageVector = destino.icon,
                                    contentDescription = destino.label,
                                    tint = if (selecionado) Primary else TextMuted,
                                    modifier = Modifier.size(22.dp),
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = destino.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = com.franferreira.enem2027.ui.theme.FonteManuscrita,
                                    ),
                                    color = if (selecionado) Primary else TextMuted,
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Hoje.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Destination.Hoje.route) {
                TelaHoje(
                    estado = estado,
                    aoFiltrarArea = vm::filtrarPorArea,
                    aoFiltrarTema = vm::filtrarPorTema,
                    aoBaixarAno = vm::baixarAcervo,
                    aoSincronizar = vm::sincronizar,
                    aoAbrirQuestao = { indice ->
                        estado.sessao.getOrNull(indice)?.let { questao ->
                            abrirQuestao(navController, vm, questao)
                        }
                    },
                    aoAbrirTrilha = { trilha ->
                        vm.abrirTrilha(trilha)
                        navController.navigate("trilha")
                    },
                    aoJogarEtapa = { trilha, etapa ->
                        vm.iniciarEtapa(trilha, etapa)
                        navController.navigate("etapa")
                    },
                    aoAjustarMeta = vm::ajustarMeta,
                    aoAtualizarAcervo = vm::atualizarAcervo,
                    aoIniciarFoco = { vm.iniciarFoco() },
                    aoPararFoco = vm::pararFoco,
                    aoRevisar = { navController.navigate(Destination.Revisao.route) },
                    aoAulas = { navController.navigate(Destination.Aulas.route) },
                    aoMudarMetaSemanal = vm::ajustarMetaSemanal,
                    aoFazerDiagnostico = vm::iniciarDiagnostico,
                    aoAbrirCronograma = { navController.navigate("cronograma") },
                )
            }

            // Trilha: o caminho vertical com os nós.
            composable("trilha") {
                val trilha = estado.trilhaAberta
                if (trilha == null) {
                    TelaVazia(
                        titulo = "Trilha não encontrada",
                        subtitulo = "Abra a trilha de novo pela tela Hoje.",
                        aoVoltar = { navController.popBackStack() },
                    )
                } else {
                    TelaTrilha(
                        trilha = trilha,
                        aoVoltar = {
                            vm.fecharTrilha()
                            navController.popBackStack()
                        },
                        aoJogar = { etapa ->
                            vm.iniciarEtapa(trilha, etapa)
                            navController.navigate("etapa")
                        },
                    )
                }
            }

            // Etapa: as 12 questões de um tema.
            composable("etapa") {
                val etapa = estado.etapaEmAndamento
                val trilha = estado.trilhaAberta
                val resumo = estado.resumoEtapa
                when {
                    // Resumo da etapa concluída: fica no lugar até o usuário
                    // fechar. Antes ele era gravado no estado e nunca
                    // mostrado — a etapa acabava sem nenhum retorno visual.
                    resumo != null -> ResumoEtapa(
                        tema = resumo.tema,
                        acertos = resumo.acertos,
                        total = resumo.total,
                        segundos = resumo.segundos,
                        xp = resumo.xp,
                        cor = corDaArea(trilha?.area),
                        // O corte é 80% de acerto — o mesmo do PASSO_MINIMO_PCT
                        // do modelo de etapa.
                        passou = resumo.total > 0 &&
                            (resumo.acertos * 100f / resumo.total) >= PASSO_MINIMO_PCT,
                        percentual = if (resumo.total == 0) 0
                        else resumo.acertos * 100 / resumo.total,
                        aoFechar = {
                            vm.fecharResumo()
                            navController.popBackStack()
                        },
                    )
                    etapa == null || trilha == null -> TelaVazia(
                        titulo = "Etapa não encontrada",
                        subtitulo = "Comece a etapa de novo pela trilha.",
                        aoVoltar = { navController.popBackStack() },
                    )
                    else -> TelaEtapa(
                        etapa = etapa,
                        area = trilha.area,
                        // Etapa já concluída: é revisão, não dá XP nem mexe
                        // no SRS.
                        revisando = etapa.concluida,
                        aoResponder = { questionId, correta, segundos ->
                            vm.responder(questionId, correta, segundos)
                        },
                        aoConcluir = { acertos, segundos ->
                            vm.concluirEtapa(acertos, segundos)
                        },
                        aoVoltar = {
                            vm.fecharEtapa()
                            navController.popBackStack()
                        },
                    )
                }
            }

            composable("questao") {
                val questao = estado.questaoAberta
                if (questao != null) {
                    TelaQuestao(
                        questao = questao,
                        aoResponder = { correta, segundos, confianca ->
                            vm.responder(questao.id, correta, segundos, confianca)
                        },
                        aoVoltar = {
                            vm.fecharQuestao()
                            navController.popBackStack()
                        },
                        aoPedirExplicacao = { vm.pedirExplicacao(questao) },
                        aoAbrirAula = { id ->
                            vm.abrirPacoteAula(id)
                            navController.navigate("aulapacote")
                        },
                    )
                } else {
                    TelaVazia(
                        titulo = if (estado.questaoPendente != null) {
                            "Abrindo a questão…"
                        } else {
                            "Questão não encontrada"
                        },
                        subtitulo = if (estado.questaoPendente != null) {
                            "Só um instante."
                        } else {
                            "Ela pode ter saído do acervo do aparelho."
                        },
                        aoVoltar = if (estado.questaoPendente != null) {
                            null
                        } else {
                            { navController.popBackStack() }
                        },
                    )
                }
            }

            composable(Destination.Revisao.route) {
                TelaRevisao(
                    aoAbrir = { questao -> abrirQuestao(navController, vm, questao) },
                    aoSincronizar = vm::sincronizar,
                    sincronizando = estado.sincronizando,
                    treinos = estado.treinos,
                    treinoAberto = estado.treinoAberto,
                    treinoQuestoes = estado.treinoQuestoes,
                    aoAbrirTreino = vm::abrirTreino,
                    aoFecharTreino = vm::fecharTreino,
                    buscaTexto = estado.buscaTexto,
                    resultadoBusca = estado.resultadoBusca,
                    aoBuscar = vm::buscarQuestoes,
                )
            }
            composable(Destination.Redacao.route) {
                TelaRedacao(
                    temas = vm.temasRedacao,
                    aoCorrigir = { tema, texto -> vm.corrigirRedacao(tema, texto) },
                    historico = estado.historicoRedacoes,
                    rascunho = estado.redacaoRascunho,
                    aoSalvarRascunho = { tema, texto -> vm.salvarRascunhoRedacao(tema, texto) },
                    corrigindo = estado.corrigindoRedacao,
                    erro = estado.erroRedacao,
                    resultado = estado.resultadoRedacao,
                )
            }
            composable(Destination.Progresso.route) {
                TelaProgresso(
                    progresso = estado.progresso,
                    aoAbrirAgenda = { navController.navigate(Destination.Agenda.route) },
                    aoAbrirAprovacao = { navController.navigate(Destination.Aprovacao.route) },
                    aoAbrirNota = { navController.navigate("nota") },
                    aoAbrirCaderno = { navController.navigate("caderno") },
                    aoAbrirAulas = { navController.navigate("aulas") },
                    pendentesAprovacao = estado.filaAprovacao.size,
                )
            }

            // Nota prevista: quanto o usuário faria hoje, e a nota de corte.
            composable("nota") {
                // Recarrega quando a tela volta de uma resposta: a previsão
                // depende do que foi respondido agora.
                LaunchedEffect(estado.questaoAberta?.id) { vm.carregarPrevisao() }
                TelaNota(
                    previsao = estado.previsao,
                    aoTrocarAno = vm::trocarAnoCorte,
                    aoTrocarAlvo = vm::trocarNotaAlvo,
                    aoVoltar = { navController.popBackStack() },
                )
            }

            // ---- video-aulas ----
            composable("aulas") {
                LaunchedEffect(Unit) { vm.carregarAreasAula() }
                TelaAulas(
                    areas = estado.areasAula,
                    aoAbrirArea = { area ->
                        vm.carregarAulasDe(area)
                        navController.navigate("aulasarea")
                    },
                    aoVoltar = { navController.popBackStack() },
                    aoContinuar = { id ->
                        vm.abrirPacoteAula(id)
                        navController.navigate("aulapacote")
                    },
                )
            }

            composable("aulasarea") {
                TelaAulasDeArea(
                    dados = estado.aulasDaArea,
                    aoAbrirAula = { aula ->
                        vm.abrirPacoteAula(aula.id)
                        navController.navigate("aulapacote")
                    },
                    aoVoltar = { navController.popBackStack() },
                )
            }

            // Cronograma até a prova: o que estudar em cada semana.
            composable("cronograma") {
                TelaCronograma(
                    semanas = estado.cronograma,
                    cobertura = estado.coberturaTemas,
                    aoVoltar = { navController.popBackStack() },
                )
            }

            composable("aulapacote") {
                val pacote = estado.pacoteAula
                when {
                    pacote != null -> TelaAula(
                        pacote = pacote,
                        aoVoltar = {
                            vm.fecharAula()
                            navController.popBackStack()
                        },
                    )
                    estado.carregandoAulas -> Box(
                        Modifier.fillMaxSize().background(Background),
                        androidx.compose.ui.Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                    // Sem cache e sem rede: antes disto a tela ficava com o
                    // spinner eterno, sem nem a seta de voltar.
                    else -> TelaVazia(
                        titulo = "Aula não abriu",
                        subtitulo = "Ela precisa de conexão na primeira vez.",
                        aoVoltar = {
                            vm.fecharAula()
                            navController.popBackStack()
                        },
                    )
                }
            }

            // Caderno de erros: o que errou, para treinar de novo.
            // Revisão do simulado: questão a questão, com gabarito e explicação.
            composable("revisaosimulado") {
                val simulado = estado.simuladoRevisao
                if (simulado == null) {
                    TelaVazia(
                        titulo = "Nada para revisar",
                        subtitulo = "Faça um simulado primeiro.",
                        aoVoltar = { navController.popBackStack() },
                    )
                } else {
                    TelaRevisaoSimulado(
                        simulado = simulado,
                        respostas = estado.respostasRevisao,
                        tempos = estado.temposRevisao,
                        aoVoltar = { navController.popBackStack() },
                    )
                }
            }

            composable("caderno") {
                // Recarrega ao voltar de uma questão corrigida: o erro
                // corrigido muda o caderno.
                LaunchedEffect(estado.questaoAberta?.id) { vm.carregarCaderno() }
                TelaCaderno(
                    itens = estado.caderno,
                    aoAbrir = { id ->
                        vm.abrirQuestao(id)
                        navController.navigate("questao")
                    },
                    aoVoltar = { navController.popBackStack() },
                    aoAbrirAula = { id ->
                        vm.abrirPacoteAula(id)
                        navController.navigate("aulapacote")
                    },
                )
            }

            composable(Destination.Aprovacao.route) {
                TelaAprovacao(
                    itens = estado.filaAprovacao,
                    aoAprovar = { id -> vm.resolverAprovacao(id, true) },
                    aoRejeitar = { id -> vm.resolverAprovacao(id, false) },
                )
            }

            composable(Destination.Agenda.route) {
                TelaAgenda(
                    diasSelecionados = estado.agenda,
                    aoAlternar = vm::alternarAgenda,
                    aoSalvar = vm::salvarAgenda,
                    mensagem = estado.mensagem,
                )
            }

            composable(Destination.Simulado.route) {
                val simulado = estado.simulado
                val diagnostico = estado.diagnostico
                when {
                    diagnostico != null -> TelaResultadoSimulado(
                        diagnostico = diagnostico,
                        aoFechar = {
                            vm.fecharSimulado()
                            navController.popBackStack()
                        },
                        aoRevisar = { navController.navigate("revisaosimulado") },
                    )
                    simulado != null -> TelaSimulado(
                        simulado = simulado,
                        iniciadoEm = estado.simuladoInicio,
                        aoFinalizar = { respostas, duracao, tempos ->
                            vm.finalizarSimulado(respostas, duracao, tempos)
                        },
                        aoVoltar = {
                            vm.fecharSimulado()
                            navController.popBackStack()
                        },
                    )
                    else -> TelaMontarSimulado(
                        anosDisponiveis = estado.anos,
                        carregando = estado.carregandoSimulado,
                        diagnosticoPrevio = estado.mensagemSimulado,
                        aoIniciar = { tipo, area, ano, quantidade ->
                            vm.gerarSimulado(tipo, area, ano, quantidade)
                        },
                    )
                }
            }
        }
    }
}

/**
 * Abre a tela de questão.
 *
 * Passa só o ID pelo `savedStateHandle`: o `savedStateHandle` do Navigation
 * exige que o valor seja Parcelable ou Serializable, e a data class
 * `Question` do Kotlin não é nenhuma das duas — passar o objeto inteiro
 * fazia o app fechar ao tocar na questão.
 *
 * O ViewModel é quem carrega a questão do banco local e a expõe no estado;
 * a função de composite não pode chamar um `suspend`.
 */
private fun abrirQuestao(
    navController: androidx.navigation.NavHostController,
    vm: ViewModelPrincipal,
    questao: com.franferreira.enem2027.data.model.Question,
) {
    vm.abrirQuestao(questao.id)
    navController.navigate("questao")
}

@Composable
private fun TelaVazia(
    titulo: String,
    subtitulo: String,
    aoVoltar: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
            if (aoVoltar != null) {
                // Sem este botão, um process death no meio do caminho deixava
                // a tela morta: a rota voltava, o ViewModel não.
                Spacer(Modifier.height(16.dp))
                androidx.compose.material3.TextButton(onClick = aoVoltar) {
                    Text(text = "Voltar", color = Primary)
                }
            }
        }
    }
}

@Composable
private fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectAsStateSafe() =
    collectAsStateWithLifecycle()
