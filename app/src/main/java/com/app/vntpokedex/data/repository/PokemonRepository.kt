package com.app.vntpokedex.data.repository

import com.app.vntpokedex.data.model.Pokemon
import com.app.vntpokedex.data.model.PokemonDetail
import com.app.vntpokedex.data.network.RetrofitInstance

class PokemonRepository {

    private val api = RetrofitInstance.api

    suspend fun getAllPokemons(): List<Pokemon> {
        return api.getPokemons(limit = 2000, offset = 0).body()?.results ?: emptyList()
    }

    suspend fun getPokemonDetail(name: String): PokemonDetail? {
        return api.getPokemonDetail(name).body()
    }
}