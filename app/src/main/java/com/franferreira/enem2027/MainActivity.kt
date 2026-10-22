package com.franferreira.enem2027

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.franferreira.enem2027.ui.screens.TelaAprovacao
import com.franferreira.enem2027.ui.screens.TelaEtapa
import com.franferreira.enem2027.ui.screens.TelaHoje
import com.franferreira.enem2027.ui.screens.TelaLogin
import com.franferreira.enem2027.ui.screens.TelaMontarSimulado
import com.franferreira.enem2027.ui.screens.TelaProgresso
import com.franferreira.enem2027.data.model.PASSO_MINIMO_PCT
import com.franferreira.enem2027.ui.screens.TelaQuestao
import com.franferreira.enem2027.ui.screens.TelaRedacao
import com.franferreira.enem2027.ui.screens.TelaResultadoSimulado
import com.franferreira.enem2027.ui.screens.TelaRevisao
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
    data object Revisao : Destination("revisao", "Revisão", Icons.Filled.CheckCircle)
    data object Redacao : Destination("redacao", "Redação", Icons.Filled.Edit)
    data object Progresso : Destination("progresso", "Progresso", Icons.Filled.Insights)
    data object Agenda : Destination("agenda", "Agenda", Icons.Filled.CalendarMonth)
    data object Simulado : Destination("simulado", "Simulado", Icons.Filled.Timer)
    data object Aprovacao : Destination("aprovacao", "Aprovação", Icons.Filled.FactCheck)
}

private val destinations = listOf(
    Destination.Hoje,
    Destination.Revisao,
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

    if (!estado.autenticado) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
        ) {
            if (estado.carregando) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = Primary,
                        strokeWidth = 2.dp,
                    )
                }
            } else {
                TelaLogin(aoEntrar = vm::autenticar)
            }
        }
        return
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (rotaAtual != "questao" && estado.autenticado) {
                NavigationBar(containerColor = Surface, contentColor = TextPrimary) {
                    destinations.forEach { destino ->
                        val selecionado =
                            currentDestination?.hierarchy?.any { it.route == destino.route } == true
                        NavigationBarItem(
                            selected = selecionado,
                            onClick = {
                                navController.navigate(destino.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destino.icon,
                                    contentDescription = destino.label,
                                )
                            },
                            label = { Text(destino.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextPrimary,
                                selectedTextColor = TextPrimary,
                                indicatorColor = Primary.copy(alpha = 0.18f),
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                            ),
                        )
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
                )
            }

            // Trilha: o caminho vertical com os nós.
            composable("trilha") {
                val trilha = estado.trilhaAberta
                if (trilha == null) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Background),
                    ) {
                        Text(
                            text = "Trilha não encontrada",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted,
                        )
                    }
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
                    etapa == null || trilha == null -> Box(
                        Modifier
                            .fillMaxSize()
                            .background(Background),
                    ) {
                        Text(
                            text = "Etapa não encontrada",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted,
                        )
                    }
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
                        aoResponder = { correta, segundos ->
                            vm.responder(questao.id, correta, segundos)
                        },
                        aoVoltar = {
                            vm.fecharQuestao()
                            navController.popBackStack()
                        },
                        aoPedirExplicacao = { vm.pedirExplicacao(questao) },
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Background),
                    ) {
                        Text(
                            text = "Questão não encontrada",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted,
                        )
                    }
                }
            }

            composable(Destination.Revisao.route) {
                TelaRevisao(
                    vencendo = estado.fila.vencendo,
                    novas = estado.fila.novas,
                    erradas = estado.fila.erradas,
                    aoAbrir = { questao -> abrirQuestao(navController, vm, questao) },
                    aoSincronizar = vm::sincronizar,
                    sincronizando = estado.sincronizando,
                )
            }
            composable(Destination.Redacao.route) {
                TelaRedacao(
                    temas = vm.temasRedacao.map { it.titulo },
                    aoCorrigir = { tema, texto -> vm.corrigirRedacao(tema, texto) },
                    historico = estado.historicoRedacoes,
                    rascunho = estado.redacaoRascunho,
                    aoSalvarRascunho = { tema, texto -> vm.salvarRascunhoRedacao(tema, texto) },
                )
            }
            composable(Destination.Progresso.route) {
                TelaProgresso(
                    progresso = estado.progresso,
                    aoAbrirAgenda = { navController.navigate(Destination.Agenda.route) },
                    aoAbrirAprovacao = { navController.navigate(Destination.Aprovacao.route) },
                    aoAbrirNota = { navController.navigate("nota") },
                    aoAbrirCaderno = { navController.navigate("caderno") },
                    pendentesAprovacao = estado.filaAprovacao.size,
                )
            }

            // Nota prevista: quanto o usuário faria hoje, e a nota de corte.
            composable("nota") {
                TelaNota(
                    previsao = estado.previsao,
                    aoTrocarAno = vm::trocarAnoCorte,
                    aoTrocarAlvo = vm::trocarNotaAlvo,
                    aoVoltar = { navController.popBackStack() },
                )
            }

            // Caderno de erros: o que errou, para treinar de novo.
            composable("caderno") {
                TelaCaderno(
                    itens = estado.caderno,
                    aoAbrir = { id ->
                        vm.abrirQuestao(id)
                        navController.navigate("questao")
                    },
                    aoVoltar = { navController.popBackStack() },
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
                    )
                    simulado != null -> TelaSimulado(
                        simulado = simulado,
                        aoFinalizar = { respostas, duracao ->
                            vm.finalizarSimulado(respostas, duracao)
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
private fun TelaVazia(titulo: String, subtitulo: String) {
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
        }
    }
}

@Composable
private fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectAsStateSafe() =
    collectAsStateWithLifecycle()
