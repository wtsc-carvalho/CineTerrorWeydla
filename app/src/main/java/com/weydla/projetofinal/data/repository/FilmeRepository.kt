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

    suspend fun buscarCatalogoTerror(termo: String = "horror"): List<FilmeTerror> {
        val classicosCurados = obterClassicosDoTerror()
        return try {
            val respostaApi = apiService.buscarProducoesTerror(termo)
            val convertidosDaApi = respostaApi.mapNotNull { item ->
                val show = item.show ?: return@mapNotNull null
                val resumoLimpo = show.summary
                    ?.replace(Regex("<.*?>"), "")
                    ?.trim()
                    .orEmpty()
                    .ifEmpty { "Produção de mistério e terror psicológico carregada via API." }

                FilmeTerror(
                    id = show.id + 1000,
                    titulo = show.name ?: "Título Desconhecido",
                    ano = show.premiered?.take(4) ?: "2023",
                    diretor = show.network?.name ?: "Estúdio Internacional",
                    sinopse = resumoLimpo,
                    notaImdb = show.rating?.average?.toString() ?: "7.8",
                    posterUrl = show.image?.medium?.replace("http://", "https://") ?: "",
                    subgenero = show.genres?.joinToString(", ")?.ifEmpty { "Terror / Suspense" } ?: "Terror / Suspense"
                )
            }
            if (termo == "horror") {
                classicosCurados + convertidosDaApi
            } else {
                val filtradosLocais = classicosCurados.filter {
                    it.titulo.contains(termo, ignoreCase = true) ||
                            it.subgenero.contains(termo, ignoreCase = true)
                }
                filtradosLocais + convertidosDaApi
            }
        } catch (e: Exception) {
            if (termo == "horror") {
                classicosCurados
            } else {
                classicosCurados.filter {
                    it.titulo.contains(termo, ignoreCase = true) ||
                            it.subgenero.contains(termo, ignoreCase = true)
                }
            }
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
            FilmeTerror(
                id = 1,
                titulo = "Invocação do Mal (The Conjuring)",
                ano = "2013",
                diretor = "James Wan",
                sinopse = "Os investigadores paranormais Ed e Lorraine Warren trabalham para ajudar uma família aterrorizada por uma presença sombria em sua fazenda isolada.",
                notaImdb = "8.5",
                posterUrl = "https://static.tvmaze.com/uploads/images/medium_portrait/476/1191729.jpg",
                subgenero = "Sobrenatural / Possessão"
            ),
            FilmeTerror(
                id = 2,
                titulo = "Hereditário (Hereditary)",
                ano = "2018",
                diretor = "Ari Aster",
                sinopse = "Após a morte da matriarca reclusa da família, uma mãe enlutada e seus filhos começam a desvendar segredos ancestrais cada vez mais aterrorizantes.",
                notaImdb = "8.3",
                posterUrl = "https://static.tvmaze.com/uploads/images/medium_portrait/163/409060.jpg",
                subgenero = "Terror Psicológico / Ocultismo"
            ),
            FilmeTerror(
                id = 3,
                titulo = "O Iluminado (The Shining)",
                ano = "1980",
                diretor = "Stanley Kubrick",
                sinopse = "Uma família se dirige a um hotel isolado para o inverno, onde uma presença espiritual sinistra influencia o pai à violência enquanto o filho tem visões.",
                notaImdb = "8.9",
                posterUrl = "https://static.tvmaze.com/uploads/images/medium_portrait/218/546602.jpg",
                subgenero = "Terror Psicológico / Clássico"
            ),
            FilmeTerror(
                id = 4,
                titulo = "Corra! (Get Out)",
                ano = "2017",
                diretor = "Jordan Peele",
                sinopse = "Um jovem fotógrafo visita a propriedade da família de sua namorada no fim de semana, onde a recepção excessivamente acolhedora esconde um segredo perturbador.",
                notaImdb = "8.6",
                posterUrl = "https://static.tvmaze.com/uploads/images/medium_portrait/128/321026.jpg",
                subgenero = "Suspense / Terror Social"
            ),
            FilmeTerror(
                id = 5,
                titulo = "Pânico (Scream)",
                ano = "1996",
                diretor = "Wes Craven",
                sinopse = "Um ano após o assassinato de sua mãe, uma adolescente é aterrorizada por um novo assassino mascarado que usa clichês de filmes de terror em seus jogos mortais.",
                notaImdb = "8.1",
                posterUrl = "https://static.tvmaze.com/uploads/images/medium_portrait/22/56603.jpg",
                subgenero = "Slasher / Mistério"
            ),
            FilmeTerror(
                id = 6,
                titulo = "O Exorcista (The Exorcist)",
                ano = "1973",
                diretor = "William Friedkin",
                sinopse = "Quando uma garota de 12 anos é possuída por uma entidade misteriosa, sua mãe busca a ajuda de dois padres para tentar salvá-la.",
                notaImdb = "8.8",
                posterUrl = "https://static.tvmaze.com/uploads/images/medium_portrait/75/189521.jpg",
                subgenero = "Sobrenatural / Clássico"
            )
        )
    }
}