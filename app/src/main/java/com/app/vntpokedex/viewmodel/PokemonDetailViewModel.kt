package com.app.vntpokedex.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.vntpokedex.data.model.PokemonDetail
import com.app.vntpokedex.data.repository.PokemonRepository
import kotlinx.coroutines.launch

class PokemonDetailViewModel : ViewModel() {

    private val repository = PokemonRepository()

    private val _pokemonDetail = MutableLiveData<PokemonDetail>()
    val pokemonDetail: LiveData<PokemonDetail> = _pokemonDetail

    fun fetchPokemonDetail(name: String) {
        viewModelScope.launch {
            val detail = repository.getPokemonDetail(name)
            detail?.let {
                _pokemonDetail.value = it
            }
        }
    }
}