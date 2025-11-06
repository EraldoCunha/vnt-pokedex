package com.app.vntpokedex.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.vntpokedex.data.model.Pokemon
import com.app.vntpokedex.data.repository.PokemonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PokemonViewModel : ViewModel() {

    private val repository = PokemonRepository()

    private val _pokemonList = MutableLiveData<List<Pokemon>>()
    val pokemonList: LiveData<List<Pokemon>> get() = _pokemonList

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private var allPokemons: List<Pokemon> = emptyList()

    private var currentType: String? = null
    private var currentGen: IntRange? = null

    private val typeCache = mutableMapOf<Int, String>()

    fun hasData() = allPokemons.isNotEmpty()

    fun fetchAllPokemons() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val pokemons = repository.getAllPokemons()
                allPokemons = pokemons
                _pokemonList.value = pokemons
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun search(query: String?) {
        val filtered = if (query.isNullOrBlank()) allPokemons
        else allPokemons.filter { it.name.contains(query, ignoreCase = true) }

        _pokemonList.value = filtered
    }

    fun filterByGeneration(label: String) {
        currentGen = when (label) {
            "Gen 1 (Kanto)" -> 1..151
            "Gen 2 (Johto)" -> 152..251
            "Gen 3 (Hoenn)" -> 252..386
            "Gen 4 (Sinnoh)" -> 387..493
            "Gen 5 (Unova)" -> 494..649
            "Gen 6 (Kalos)" -> 650..721
            "Gen 7 (Alola)" -> 722..809
            "Gen 8 (Galar)" -> 810..905
            "Gen 9 (Paldea)" -> 906..1010
            else -> null
        }
        applyFilters()
    }

    fun filterByType(type: String) {
        currentType = if (type.equals("todos", ignoreCase = true)) null else type.lowercase()
        applyFilters()
    }

    private fun applyFilters() {
        viewModelScope.launch {
            var list = allPokemons

            currentGen?.let { range ->
                list = list.filter { it.id in range }
            }

            currentType?.let { type ->
                list = list.filter { pokemon ->
                    val pokemonType = typeCache[pokemon.id] ?: fetchAndCacheType(pokemon.id)
                    pokemonType == type
                }
            }

            _pokemonList.value = list
        }
    }

    private suspend fun fetchAndCacheType(id: Int): String? {
        return withContext(Dispatchers.IO) {
            val detail = repository.getPokemonDetail(id.toString())
            val mainType = detail?.types?.firstOrNull()?.type?.name
            if (mainType != null) typeCache[id] = mainType
            mainType
        }
    }
}
