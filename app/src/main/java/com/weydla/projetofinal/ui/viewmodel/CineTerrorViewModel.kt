package com.weydla.projetofinal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.weydla.projetofinal.data.model.FilmeTerror
import com.weydla.projetofinal.data.repository.FilmeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CatalogoUiState(
    val filmes: List<FilmeTerror> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null,
    val termoBusca: String = "",
    val mensagemFeedback: String? = null
)

class CineTerrorViewModel(
    private val repository: FilmeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogoUiState(carregando = true))
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    private val _filmeSelecionado = MutableStateFlow<FilmeTerror?>(null)
    val filmeSelecionado: StateFlow<FilmeTerror?> = _filmeSelecionado.asStateFlow()

    val favoritosState: StateFlow<List<FilmeTerror>> = repository.favoritosFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        carregarCatalogo("horror")
    }

    fun selecionarFilme(filme: FilmeTerror?) {
        _filmeSelecionado.value = filme
    }

    fun carregarCatalogo(termo: String = "horror") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(carregando = true, erro = null, mensagemFeedback = null)
            val termoBusca = termo.ifBlank { "horror" }
            
            val filmesOffline = repository.buscarOffline(termoBusca)

            try {
                val filmesOnline = repository.buscarOnline(termoBusca)
                _uiState.value = _uiState.value.copy(
                    filmes = filmesOnline + filmesOffline,
                    carregando = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    filmes = filmesOffline,
                    erro = "Sem conexão com a internet. Exibindo apenas o catálogo offline.",
                    carregando = false
                )
            }
        }
    }

    fun atualizarTermoBusca(novoTermo: String) {
        _uiState.value = _uiState.value.copy(termoBusca = novoTermo)
    }

    fun executarBusca() {
        val termo = _uiState.value.termoBusca.trim()
        carregarCatalogo(termo.ifEmpty { "horror" })
    }

    fun alternarFavorito(filme: FilmeTerror) {
        viewModelScope.launch {
            try {
                val jaFavorito = favoritosState.value.any { it.id == filme.id }
                if (jaFavorito) {
                    repository.removerFavorito(filme)
                    mostrarMensagem("${filme.titulo} removido da sua lista local.")
                } else {
                    repository.salvarFavorito(filme)
                    mostrarMensagem("${filme.titulo} salvo nos Favoritos (Room)!")
                }
            } catch (e: Exception) {
                mostrarMensagem("Erro ao atualizar os favoritos.")
            }
        }
    }

    fun alternarAssistido(filme: FilmeTerror) {
        viewModelScope.launch {
            try {
                val atualizado = filme.copy(assistido = !filme.assistido)
                repository.atualizarFilme(atualizado)
                val statusTexto = if (atualizado.assistido) "marcado como Assistido!" else "marcado como Pendente."
                mostrarMensagem("\"${filme.titulo}\" $statusTexto")
            } catch (e: Exception) {
                mostrarMensagem("Erro ao atualizar o status do filme.")
            }
        }
    }

    fun salvarComentarioPessoal(filme: FilmeTerror, comentario: String) {
        viewModelScope.launch {
            try {
                val atualizado = filme.copy(comentarioPessoal = comentario.trim())
                repository.salvarFavorito(atualizado)
                mostrarMensagem("Anotação salva com sucesso no banco de dados!")
            } catch (e: Exception) {
                mostrarMensagem("Erro ao salvar anotação.")
            }
        }
    }

    fun adicionarFilmePersonalizado(
        titulo: String,
        ano: String,
        diretor: String,
        subgenero: String,
        nota: String,
        sinopse: String
    ) {
        viewModelScope.launch {
            try {
                val novoId = (System.currentTimeMillis() % 100000).toInt() + 5000
                val novoFilme = FilmeTerror(
                    id = novoId,
                    titulo = titulo.trim(),
                    ano = ano.trim().ifEmpty { "2026" },
                    diretor = diretor.trim().ifEmpty { "Diretor Independente" },
                    subgenero = subgenero.trim().ifEmpty { "Terror Sobrenatural" },
                    notaImdb = nota.trim().ifEmpty { "8.0" },
                    sinopse = sinopse.trim().ifEmpty { "Filme cadastrado manualmente pela Weydla." },
                    posterUrl = "",
                    assistido = false
                )
                repository.salvarFavorito(novoFilme)
                _uiState.value = _uiState.value.copy(
                    filmes = listOf(novoFilme) + _uiState.value.filmes
                )
                mostrarMensagem("Filme \"${novoFilme.titulo}\" cadastrado e salvo no Room!")
            } catch (e: Exception) {
                mostrarMensagem("Erro ao cadastrar o filme.")
            }
        }
    }

    fun limparMensagemFeedback() {
        _uiState.value = _uiState.value.copy(mensagemFeedback = null)
    }

    private fun mostrarMensagem(msg: String) {
        _uiState.value = _uiState.value.copy(mensagemFeedback = msg)
    }
}

class CineTerrorViewModelFactory(
    private val repository: FilmeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CineTerrorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CineTerrorViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecida")
    }
}