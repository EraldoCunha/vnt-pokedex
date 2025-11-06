package com.app.vntpokedex.data.network

import com.app.vntpokedex.data.model.PokemonDetail
import com.app.vntpokedex.data.model.PokemonResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokemonApiService {

    @GET("pokemon")
    suspend fun getPokemons(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<PokemonResponse>

    @GET("pokemon/{name}")
    suspend fun getPokemonDetail(@Path("name") name: String): Response<PokemonDetail>

}