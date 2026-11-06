package com.weydla.projetofinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.weydla.projetofinal.data.local.AppDatabase
import com.weydla.projetofinal.data.remote.TerrorApiService
import com.weydla.projetofinal.data.repository.FilmeRepository
import com.weydla.projetofinal.ui.screens.*
import com.weydla.projetofinal.ui.theme.CineTerrorWeydlaTheme
import com.weydla.projetofinal.ui.theme.CrimsonPrimary
import com.weydla.projetofinal.ui.theme.TerrorSurface
import com.weydla.projetofinal.ui.viewmodel.CineTerrorViewModel
import com.weydla.projetofinal.ui.viewmodel.CineTerrorViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        val apiService = TerrorApiService.create()
        val repository = FilmeRepository(database.filmeDao(), apiService)
        val viewModelFactory = CineTerrorViewModelFactory(repository)

        setContent {
            CineTerrorWeydlaTheme {
                val viewModel: CineTerrorViewModel = viewModel(factory = viewModelFactory)
                CineTerrorApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CineTerrorApp(viewModel: CineTerrorViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val favoritos by viewModel.favoritosState.collectAsState()
    val filmeSelecionado by viewModel.filmeSelecionado.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var exibirModalNovoFilme by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.mensagemFeedback) {
        uiState.mensagemFeedback?.let { mensagem ->
            snackbarHostState.showSnackbar(
                message = mensagem,
                duration = SnackbarDuration.Short
            )
            viewModel.limparMensagemFeedback()
        }
    }

    if (exibirModalNovoFilme) {
        DialogNovoFilmeTerror(
            onDismiss = { exibirModalNovoFilme = false },
            onConfirmar = { titulo, ano, diretor, subgenero, nota, sinopse ->
                viewModel.adicionarFilmePersonalizado(titulo, ano, diretor, subgenero, nota, sinopse)
            }
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = navBackStackEntry?.destination?.route ?: "catalogo"
    val favoritosIds = remember(favoritos) { favoritos.map { it.id }.toSet() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CineTerror • Weydla",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TerrorSurface,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (rotaAtual == "catalogo" || rotaAtual == "favoritos") {
                ExtendedFloatingActionButton(
                    onClick = { exibirModalNovoFilme = true },
                    containerColor = CrimsonPrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Adicionar Filme") },
                    text = { Text("Novo Filme") }
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = TerrorSurface) {
                NavigationBarItem(
                    selected = rotaAtual == "catalogo",
                    onClick = {
                        navController.navigate("catalogo") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Movie, contentDescription = "Aba Catálogo") },
                    label = { Text("Catálogo API") }
                )
                NavigationBarItem(
                    selected = rotaAtual == "favoritos",
                    onClick = {
                        navController.navigate("favoritos") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (favoritos.isNotEmpty()) {
                                    Badge { Text(favoritos.size.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = "Aba Favoritos Room")
                        }
                    },
                    label = { Text("Favoritos (${favoritos.size})") }
                )
                NavigationBarItem(
                    selected = rotaAtual == "sobre",
                    onClick = {
                        navController.navigate("sobre") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Aba Sobre") },
                    label = { Text("Weydla 3A") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "catalogo",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("catalogo") {
                TelaCatalogo(
                    uiState = uiState,
                    favoritosIds = favoritosIds,
                    onBuscaChange = viewModel::atualizarTermoBusca,
                    onExecutarBusca = viewModel::executarBusca,
                    onRecarregarPadrao = { viewModel.carregarCatalogo("horror") },
                    onSelecionarFilme = { filme ->
                        viewModel.selecionarFilme(favoritos.find { it.id == filme.id } ?: filme)
                        navController.navigate("detalhes")
                    },
                    onAlternarFavorito = viewModel::alternarFavorito
                )
            }

            composable("favoritos") {
                TelaFavoritosRoom(
                    favoritos = favoritos,
                    onSelecionarFilme = { filme ->
                        viewModel.selecionarFilme(filme)
                        navController.navigate("detalhes")
                    },
                    onAlternarAssistido = viewModel::alternarAssistido,
                    onRemoverFavorito = viewModel::alternarFavorito
                )
            }

            composable("detalhes") {
                val filmeAtual = filmeSelecionado
                if (filmeAtual != null) {
                    val versaoSalva = favoritos.find { it.id == filmeAtual.id } ?: filmeAtual
                    TelaDetalhesFilme(
                        filme = versaoSalva,
                        ehFavorito = favoritosIds.contains(versaoSalva.id),
                        onVoltar = { navController.popBackStack() },
                        onAlternarFavorito = viewModel::alternarFavorito,
                        onSalvarComentario = { filme, nota ->
                            viewModel.salvarComentarioPessoal(filme, nota)
                        }
                    )
                }
            }

            composable("sobre") {
                TelaSobreWeydla()
            }
        }
    }
}