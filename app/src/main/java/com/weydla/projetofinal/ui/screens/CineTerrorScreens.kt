package com.weydla.projetofinal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.weydla.projetofinal.data.model.FilmeTerror
import com.weydla.projetofinal.ui.theme.*
import com.weydla.projetofinal.ui.viewmodel.CatalogoUiState

@Composable
fun TelaCatalogo(
    uiState: CatalogoUiState,
    favoritosIds: Set<Int>,
    onBuscaChange: (String) -> Unit,
    onExecutarBusca: () -> Unit,
    onRecarregarPadrao: () -> Unit,
    onSelecionarFilme: (FilmeTerror) -> Unit,
    onAlternarFavorito: (FilmeTerror) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.termoBusca,
            onValueChange = onBuscaChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar filme ou série na API (ex: Scream, Ghost)...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Ícone de busca"
                )
            },
            trailingIcon = {
                if (uiState.termoBusca.isNotEmpty()) {
                    IconButton(onClick = {
                        onBuscaChange("")
                        onRecarregarPadrao()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpar texto de busca"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
                onExecutarBusca()
            })
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = {
                    onBuscaChange("")
                    onRecarregarPadrao()
                },
                label = { Text("Destaques") },
                leadingIcon = {
                    Icon(Icons.Default.Whatshot, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            )
            AssistChip(
                onClick = {
                    onBuscaChange("vampire")
                    onExecutarBusca()
                },
                label = { Text("Vampiros") }
            )
            AssistChip(
                onClick = {
                    onBuscaChange("zombie")
                    onExecutarBusca()
                },
                label = { Text("Zumbis") }
            )
            AssistChip(
                onClick = {
                    onBuscaChange("ghost")
                    onExecutarBusca()
                },
                label = { Text("Fantasmas") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.carregando) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CrimsonPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Carregando catálogo de terror via Retrofit...",
                        color = TextSecondary
                    )
                }
            }
        } else if (uiState.filmes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MovieFilter,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nenhum título encontrado para a busca.",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onRecarregarPadrao) {
                        Text("Restaurar Catálogo Principal")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(uiState.filmes, key = { it.id }) { filme ->
                    val ehFavorito = favoritosIds.contains(filme.id)
                    CardFilmeTerror(
                        filme = filme,
                        ehFavorito = ehFavorito,
                        onClick = { onSelecionarFilme(filme) },
                        onFavoritarClick = { onAlternarFavorito(filme) }
                    )
                }
            }
        }
    }
}

@Composable
fun TelaFavoritosRoom(
    favoritos: List<FilmeTerror>,
    onSelecionarFilme: (FilmeTerror) -> Unit,
    onAlternarAssistido: (FilmeTerror) -> Unit,
    onRemoverFavorito: (FilmeTerror) -> Unit
) {
    var filmeParaConfirmarExclusao by remember { mutableStateOf<FilmeTerror?>(null) }

    filmeParaConfirmarExclusao?.let { filme ->
        AlertDialog(
            onDismissRequest = { filmeParaConfirmarExclusao = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonPrimary) },
            title = { Text("Remover dos Favoritos?") },
            text = {
                Text("Deseja realmente remover \"${filme.titulo}\" da sua lista salva no banco de dados local?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoverFavorito(filme)
                        filmeParaConfirmarExclusao = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("Sim, Remover")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { filmeParaConfirmarExclusao = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        val totalAssistidos = favoritos.count { it.assistido }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TerrorCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Minha Videoteca de Terror (Room)",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${favoritos.size} salvos • $totalAssistidos assistidos",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "Banco de Dados Local Ativo",
                    tint = AmberStar,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (favoritos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sua lista de favoritos está vazia",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Toque no ícone de coração no Catálogo ou adicione um novo filme no botão '+' para salvar no banco local Room.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(favoritos, key = { it.id }) { filme ->
                    CardFavoritoRoom(
                        filme = filme,
                        onClick = { onSelecionarFilme(filme) },
                        onAlternarAssistido = { onAlternarAssistido(filme) },
                        onSolicitarExclusao = { filmeParaConfirmarExclusao = filme }
                    )
                }
            }
        }
    }
}

@Composable
fun TelaDetalhesFilme(
    filme: FilmeTerror,
    ehFavorito: Boolean,
    onVoltar: () -> Unit,
    onAlternarFavorito: (FilmeTerror) -> Unit,
    onSalvarComentario: (FilmeTerror, String) -> Unit
) {
    var comentario by remember(filme.id) { mutableStateOf(filme.comentarioPessoal) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onVoltar) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar para a tela anterior",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Detalhes da Produção",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TerrorCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 110.dp, height = 160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TerrorSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        SubcomposeAsyncImage(
                            model = filme.posterUrl,
                            contentDescription = "Pôster do filme ${filme.titulo}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    CircularProgressIndicator(color = CrimsonPrimary, modifier = Modifier.size(24.dp))
                                }
                            },
                            error = {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = "Imagem indisponível",
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.fillMaxSize().padding(16.dp)
                                )
                            }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = filme.titulo,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ano: ${filme.ano}",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Direção/Rede: ${filme.diretor}",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = CrimsonDark.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = filme.subgenero,
                                color = Color(0xFFFFCDD2),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Nota",
                                tint = AmberStar,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${filme.notaImdb} / 10",
                                color = AmberStar,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = TerrorSurface)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Sinopse",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = filme.sinopse,
                    color = TextSecondary,
                    lineHeight = 20.sp,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onAlternarFavorito(filme) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (ehFavorito) TerrorSurface else CrimsonPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (ehFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (ehFavorito) CrimsonPrimary else Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (ehFavorito) "Remover dos Favoritos (Room)" else "Salvar nos Favoritos (Room)"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TerrorCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Diário de Terror da Weydla (Persistência Room)",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Escreva sua avaliação ou susto favorito...") },
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onSalvarComentario(filme, comentario) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salvar Anotação")
                }
            }
        }
    }
}

@Composable
fun TelaSobreWeydla() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(CrimsonPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MovieFilter,
                contentDescription = "Logotipo CineTerror Weydla",
                tint = Color.White,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "CineTerror Weydla",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Versão 1.0.0 • Projeto Final Módulo 3",
            color = AmberStar,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TerrorCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Identificação Acadêmica",
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                ItemInfo("Discente:", "Weydla Tainá Silva de Carvalho")
                ItemInfo("Instituição:", "IFPE - Campus Palmares")
                ItemInfo("Curso / Turma:", "Integrado em Informática - Turma 3A")
                ItemInfo("Disciplina:", "Desenvolvimento Móvel (2026.2)")
                ItemInfo("Docente:", "Prof. Hélio Fernando Bentzen Pessoa Filho")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TerrorCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Requisitos Técnicos e IHC Aplicados",
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Prática 01 e 02: Kotlin + Jetpack Compose Material 3", color = TextSecondary, fontSize = 14.sp)
                Text("• Prática 03: Navigation Compose (Navegação Inferior)", color = TextSecondary, fontSize = 14.sp)
                Text("• Prática 04 e 06: Arquitetura MVVM + StateFlow + Coroutines", color = TextSecondary, fontSize = 14.sp)
                Text("• Prática 05: Listas dinâmicas com LazyColumn", color = TextSecondary, fontSize = 14.sp)
                Text("• Prática 07: Persistência Local SQLite com Room (CRUD completo)", color = TextSecondary, fontSize = 14.sp)
                Text("• Prática 08: Consumo de API REST na Internet com Retrofit", color = TextSecondary, fontSize = 14.sp)
                Text("• IHC / Usabilidade: Feedback imediato (Snackbar), confirmação de exclusão, alto contraste no escuro e áreas de toque acessíveis.", color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ItemInfo(rotulo: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = rotulo, color = TextSecondary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Text(text = valor, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun CardFilmeTerror(
    filme: FilmeTerror,
    ehFavorito: Boolean,
    onClick: () -> Unit,
    onFavoritarClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = TerrorCard),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 72.dp, height = 104.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TerrorSurface),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = filme.posterUrl,
                    contentDescription = "Capa de ${filme.titulo}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(color = CrimsonPrimary, modifier = Modifier.size(24.dp))
                        }
                    },
                    error = {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Imagem indisponível",
                            tint = CrimsonPrimary,
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = filme.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${filme.ano} • ${filme.subgenero}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = filme.sinopse,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AmberStar,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = filme.notaImdb,
                        color = AmberStar,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            IconButton(
                onClick = onFavoritarClick,
                modifier = Modifier
                    .size(48.dp)
                    .semantics {
                        contentDescription = if (ehFavorito) "Remover dos favoritos" else "Adicionar aos favoritos"
                    }
            ) {
                Icon(
                    imageVector = if (ehFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (ehFavorito) CrimsonPrimary else TextSecondary
                )
            }
        }
    }
}

@Composable
fun CardFavoritoRoom(
    filme: FilmeTerror,
    onClick: () -> Unit,
    onAlternarAssistido: () -> Unit,
    onSolicitarExclusao: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = TerrorCard),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = filme.titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${filme.ano} • Nota ${filme.notaImdb}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                AssistChip(
                    onClick = onAlternarAssistido,
                    label = {
                        Text(
                            text = if (filme.assistido) "Assistido" else "Para Assistir",
                            color = Color.White
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (filme.assistido) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (filme.assistido) WatchedGreen else TerrorSurface
                    )
                )

                IconButton(onClick = onSolicitarExclusao) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Excluir filme dos favoritos",
                        tint = CrimsonPrimary
                    )
                }
            }

            if (filme.comentarioPessoal.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = TerrorSurface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nota da Weydla: ${filme.comentarioPessoal}",
                        color = AmberStar,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DialogNovoFilmeTerror(
    onDismiss: () -> Unit,
    onConfirmar: (String, String, String, String, String, String) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var ano by remember { mutableStateOf("2026") }
    var diretor by remember { mutableStateOf("") }
    var subgenero by remember { mutableStateOf("Terror Sobrenatural") }
    var nota by remember { mutableStateOf("8.5") }
    var sinopse by remember { mutableStateOf("") }
    var erroTitulo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cadastrar Filme no Room", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = {
                        titulo = it
                        erroTitulo = false
                    },
                    label = { Text("Título do Filme *") },
                    isError = erroTitulo,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (erroTitulo) {
                    Text(
                        text = "Por favor, informe o título do filme.",
                        color = CrimsonPrimary,
                        fontSize = 12.sp
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ano,
                        onValueChange = { ano = it },
                        label = { Text("Ano") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = nota,
                        onValueChange = { nota = it },
                        label = { Text("Nota (0-10)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = diretor,
                    onValueChange = { diretor = it },
                    label = { Text("Diretor(a)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subgenero,
                    onValueChange = { subgenero = it },
                    label = { Text("Subgênero (ex: Slasher, Psicólogico)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sinopse,
                    onValueChange = { sinopse = it },
                    label = { Text("Sinopse ou Comentário") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isBlank()) {
                        erroTitulo = true
                    } else {
                        onConfirmar(titulo, ano, diretor, subgenero, nota, sinopse)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
            ) {
                Text("Salvar no Room")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}