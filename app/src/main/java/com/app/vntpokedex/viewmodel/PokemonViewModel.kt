package com.app.vntpokedex.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.vntpokedex.data.local.TypeCacheDataStore
import com.app.vntpokedex.data.model.Pokemon
import com.app.vntpokedex.data.repository.PokemonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PokemonViewModel(private val appContext: Context) : ViewModel() {

    private val repository = PokemonRepository()

    private val typeStore = TypeCacheDataStore(appContext)

    private val _pokemonList = MutableLiveData<List<Pokemon>>()
    val pokemonList: LiveData<List<Pokemon>> get() = _pokemonList

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _filterLoading = MutableLiveData<Boolean>()
    val filterLoading: LiveData<Boolean> get() = _filterLoading

    private var allPokemons: List<Pokemon> = emptyList()

    private var currentType: String? = null
    private var currentGen: IntRange? = null

    private var currentQuery: String? = null

    private val typeCache = mutableMapOf<Int, String>()

    fun hasData() = allPokemons.isNotEmpty()

    fun fetchAllPokemons() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val pokemons = repository.getAllPokemons()
                allPokemons = pokemons
                computeAndPostList()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun search(query: String?) {
        currentQuery = query
        computeAndPostList()
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
        computeAndPostList()
    }

    fun filterByType(type: String) {
        currentType = if (type.equals("todos", ignoreCase = true)) null else type.lowercase()

        viewModelScope.launch {
            _filterLoading.value = true
            ensureTypesLoaded()
            computeAndPostList()
            _filterLoading.value = false
        }
    }

    private fun computeAndPostList() {
        var list = allPokemons

        currentGen?.let { range ->
            list = list.filter { it.id in range }
        }

        currentType?.let { type ->
            list = list.filter { pokemon ->
                val cachedType = typeCache[pokemon.id]
                cachedType == type
            }
        }

        currentQuery?.let { q ->
            if (!q.isNullOrBlank()) {
                list = list.filter { it.name.contains(q, ignoreCase = true) }
            }
        }

        _pokemonList.value = list
    }

    private suspend fun fetchAndCacheType(id: Int): String? {
        val stored = typeStore.getType(id)
        if (stored != null) {
            typeCache[id] = stored
            return stored
        }

        return withContext(Dispatchers.IO) {
            val detail = repository.getPokemonDetail(id.toString())
            val mainType = detail?.types?.firstOrNull()?.type?.name
            if (mainType != null) {
                typeCache[id] = mainType
                typeStore.saveType(id, mainType)
            }
            mainType
        }
    }

    private suspend fun ensureTypesLoaded() {
        if (typeCache.size >= allPokemons.size) return
        withContext(Dispatchers.IO) {
            allPokemons.forEach { pokemon ->
                if (!typeCache.containsKey(pokemon.id)) {
                    val stored = typeStore.getType(pokemon.id)
                    if (stored != null) {
                        typeCache[pokemon.id] = stored
                    } else {
                        val detail = repository.getPokemonDetail(pokemon.id.toString())
                        val mainType = detail?.types?.firstOrNull()?.type?.name
                        if (mainType != null) {
                            typeCache[pokemon.id] = mainType
                            typeStore.saveType(pokemon.id, mainType)
                        }
                    }
                }
            }
        }
    }

    fun getSelectedGenerationLabel(): String? {
        return when (currentGen) {
            1..151 -> "Gen 1 (Kanto)"
            152..251 -> "Gen 2 (Johto)"
            252..386 -> "Gen 3 (Hoenn)"
            387..493 -> "Gen 4 (Sinnoh)"
            494..649 -> "Gen 5 (Unova)"
            650..721 -> "Gen 6 (Kalos)"
            722..809 -> "Gen 7 (Alola)"
            810..905 -> "Gen 8 (Galar)"
            906..1010 -> "Gen 9 (Paldea)"
            else -> null
        }
    }

    fun getSelectedType() = currentType

    fun reapplyFilters() {
        computeAndPostList()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PokemonViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PokemonViewModel(context.applicationContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}