package com.weydla.projetofinal.data.repository

import com.weydla.projetofinal.data.local.FilmeDao
import com.weydla.projetofinal.data.model.FilmeTerror
import com.weydla.projetofinal.data.remote.TerrorApiService
import kotlinx.coroutines.flow.Flow

class FilmeRepository(
    private val filmeDao: FilmeDao,
    private val apiService: TerrorApiService
) {
    val favoritosFlow: Flow<List<FilmeTerror>> = filmeDao.listarFavoritos()

    suspend fun buscarOnline(termo: String): List<FilmeTerror> {
        val respostaApi = apiService.buscarProducoesTerror(termo)
        return respostaApi.mapNotNull { item ->
            val show = item.show ?: return@mapNotNull null
            val resumoLimpo = show.summary
                ?.replace(Regex("<.*?>"), "")
                ?.trim()
                .orEmpty()
                .ifEmpty { "Produção de mistério e terror carregada via API." }

            FilmeTerror(
                id = show.id + 1000,
                titulo = show.name ?: "Título Desconhecido",
                ano = show.premiered?.take(4) ?: "2023",
                diretor = show.network?.name ?: "Estúdio Internacional",
                sinopse = resumoLimpo,
                notaImdb = show.rating?.average?.toString() ?: "7.8",
                posterUrl = show.image?.medium?.replace("http://", "https://") ?: "",
                subgenero = show.genres?.joinToString(", ")?.ifEmpty { "Terror" } ?: "Terror"
            )
        }
    }

    fun buscarOffline(termo: String): List<FilmeTerror> {
        val classicos = obterClassicosDoTerror()
        if (termo.equals("horror", ignoreCase = true)) {
            return classicos
        }
        return classicos.filter {
            it.subgenero.contains(termo, ignoreCase = true) ||
                    it.titulo.contains(termo, ignoreCase = true)
        }
    }

    suspend fun salvarFavorito(filme: FilmeTerror) {
        filmeDao.salvarFavorito(filme)
    }

    suspend fun atualizarFilme(filme: FilmeTerror) {
        filmeDao.atualizarFilme(filme)
    }

    suspend fun removerFavorito(filme: FilmeTerror) {
        filmeDao.removerFavorito(filme)
    }

    private fun obterClassicosDoTerror(): List<FilmeTerror> {
        return listOf(
            // 2 Destaques / Horror
            FilmeTerror(
                id = 1, titulo = "O Exorcista", ano = "1973", diretor = "William Friedkin",
                sinopse = "Quando uma garota de 12 anos é possuída por uma entidade misteriosa, sua mãe busca a ajuda de dois padres para tentar salvá-la.",
                notaImdb = "8.8", posterUrl = "https://static.tvmaze.com/uploads/images/medium_portrait/75/189521.jpg", subgenero = "Terror / Horror"
            ),
            FilmeTerror(
                id = 2, titulo = "O Iluminado", ano = "1980", diretor = "Stanley Kubrick",
                sinopse = "Uma família se dirige a um hotel isolado para o inverno, onde uma presença espiritual sinistra influencia o pai à violência enquanto o filho tem visões.",
                notaImdb = "8.9", posterUrl = "https://static.tvmaze.com/uploads/images/medium_portrait/218/546602.jpg", subgenero = "Terror / Horror"
            ),
            // 2 Vampiros (vampire)
            FilmeTerror(
                id = 3, titulo = "Drácula de Bram Stoker", ano = "1992", diretor = "Francis Ford Coppola",
                sinopse = "O Conde Drácula viaja para Londres em busca da mulher que ele acredita ser a reencarnação de sua esposa morta há séculos.",
                notaImdb = "7.4", posterUrl = "https://static.tvmaze.com/uploads/images/medium_portrait/476/1191730.jpg", subgenero = "Terror / Vampire"
            ),
            FilmeTerror(
                id = 4, titulo = "Entrevista com o Vampiro", ano = "1994", diretor = "Neil Jordan",
                sinopse = "Um vampiro centenário conta a sua épica e trágica história de amor, traição e imortalidade para um repórter moderno.",
                notaImdb = "7.5", posterUrl = "https://static.tvmaze.com/uploads/images/medium_portrait/34/86620.jpg", subgenero = "Terror / Vampire"
            ),
            // 2 Zumbis (zombie)
            FilmeTerror(
                id = 5, titulo = "A Noite dos Mortos-Vivos", ano = "1968", diretor = "George A. Romero",
                sinopse = "Um grupo heterogêneo de indivíduos se abriga em uma fazenda abandonada enquanto cadáveres reanimados começam a se levantar.",
                notaImdb = "7.9", posterUrl = "https://static.tvmaze.com/uploads/images/medium_portrait/128/321028.jpg", subgenero = "Terror / Zombie"
            ),
            FilmeTerror(
                id = 6, titulo = "Madrugada dos Mortos", ano = "2004", diretor = "Zack Snyder",
                sinopse = "Sobreviventes de uma praga mundial que produz zumbis carnívoros se refugiam em um shopping center gigantesco.",
                notaImdb = "7.3", posterUrl = "https://static.tvmaze.com/uploads/images/medium_portrait/163/409062.jpg", subgenero = "Terror / Zombie"
            ),
            // 2 Fantasmas (ghost)
            FilmeTerror(
                id = 7, titulo = "Invocação do Mal", ano = "2013", diretor = "James Wan",
                sinopse = "Os investigadores paranormais Ed e Lorraine Warren trabalham para ajudar uma família aterrorizada por uma presença sombria.",
                notaImdb = "7.5", posterUrl = "https://static.tvmaze.com/uploads/images/medium_portrait/476/1191729.jpg", subgenero = "Terror / Ghost"
            ),
            FilmeTerror(
                id = 8, titulo = "Poltergeist", ano = "1982", diretor = "Tobe Hooper",
                sinopse = "Fantasmas invadem a casa de uma família californiana, começando de forma lúdica antes de aterrorizá-los e sequestrar a filha.",
                notaImdb = "7.3", posterUrl = "https://static.tvmaze.com/uploads/images/medium_portrait/22/56605.jpg", subgenero = "Terror / Ghost"
            )
        )
    }
}