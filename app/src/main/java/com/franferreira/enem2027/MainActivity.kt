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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
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
import com.franferreira.enem2027.ui.screens.TelaHoje
import com.franferreira.enem2027.ui.screens.TelaLogin
import com.franferreira.enem2027.ui.screens.TelaQuestao
import com.franferreira.enem2027.ui.screens.TelaRedacao
import com.franferreira.enem2027.ui.screens.TelaRevisao
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.EnemTheme
import com.franferreira.enem2027.ui.theme.Primary
import com.franferreira.enem2027.ui.theme.Surface
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextPrimary

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
}

private val destinations = listOf(
    Destination.Hoje,
    Destination.Revisao,
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
                )
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
                    historico = emptyList(),
                )
            }
            composable(Destination.Progresso.route) { TelaVazia("Progresso", "A evolução por área vem na próxima etapa.") }
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
