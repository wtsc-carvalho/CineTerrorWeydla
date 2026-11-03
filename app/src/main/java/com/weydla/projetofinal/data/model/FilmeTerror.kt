package com.weydla.projetofinal.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filmes_favoritos")
data class FilmeTerror(
    @PrimaryKey
    val id: Int,
    val titulo: String,
    val ano: String,
    val diretor: String,
    val sinopse: String,
    val notaImdb: String,
    val posterUrl: String,
    val subgenero: String,
    val assistido: Boolean = false,
    val comentarioPessoal: String = ""
)

data class TvMazeSearchResponse(
    val score: Double?,
    val show: TvMazeShowDto?
)

data class TvMazeShowDto(
    val id: Int,
    val name: String?,
    val premiered: String?,
    val summary: String?,
    val genres: List<String>?,
    val rating: TvMazeRatingDto?,
    val image: TvMazeImageDto?,
    val network: TvMazeNetworkDto?
)

data class TvMazeRatingDto(
    val average: Double?
)

data class TvMazeImageDto(
    val medium: String?,
    val original: String?
)

data class TvMazeNetworkDto(
    val name: String?
)