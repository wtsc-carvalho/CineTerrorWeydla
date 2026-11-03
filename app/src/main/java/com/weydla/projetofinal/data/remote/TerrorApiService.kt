package com.weydla.projetofinal.data.remote

import com.weydla.projetofinal.data.model.TvMazeSearchResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface Retrofit para consumo de dados da internet (Prática 08)
 * Consome dados reais de produções de terror e suspense sem exigir chave de API paga.
 */
interface TerrorApiService {

    @GET("search/shows")
    suspend fun buscarProducoesTerror(
        @Query("q") termoBusca: String = "horror"
    ): List<TvMazeSearchResponse>

    companion object {
        private const val BASE_URL = "https://api.tvmaze.com/"

        fun create(): TerrorApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TerrorApiService::class.java)
        }
    }
}
