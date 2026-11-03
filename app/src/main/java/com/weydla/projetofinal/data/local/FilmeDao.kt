package com.weydla.projetofinal.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.weydla.projetofinal.data.model.FilmeTerror
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações no banco de dados SQLite via Room (Prática 07)
 */
@Dao
interface FilmeDao {

    @Query("SELECT * FROM filmes_favoritos ORDER BY titulo ASC")
    fun listarFavoritos(): Flow<List<FilmeTerror>>

    @Query("SELECT * FROM filmes_favoritos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): FilmeTerror?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarFavorito(filme: FilmeTerror)

    @Update
    suspend fun atualizarFilme(filme: FilmeTerror)

    @Delete
    suspend fun removerFavorito(filme: FilmeTerror)
}
