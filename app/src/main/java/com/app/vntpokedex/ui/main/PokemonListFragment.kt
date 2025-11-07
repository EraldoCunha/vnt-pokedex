package com.app.vntpokedex.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import com.app.vntpokedex.R
import com.app.vntpokedex.viewmodel.PokemonViewModel
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class PokemonListFragment : Fragment() {

    private val viewModel: PokemonViewModel by viewModels {
        PokemonViewModel.Factory(requireContext())
    }
    private lateinit var adapter: PokemonAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    private lateinit var loadingGif: ImageView
    private lateinit var typeFilter: MaterialAutoCompleteTextView
    private lateinit var genFilter: MaterialAutoCompleteTextView

    private lateinit var typeAdapter: NoFilterArrayAdapter
    private lateinit var genAdapter: NoFilterArrayAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_pokemon_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        searchView = view.findViewById(R.id.search_view)
        loadingGif = view.findViewById(R.id.loading_gif)
        typeFilter = view.findViewById(R.id.typeFilter)
        genFilter = view.findViewById(R.id.genFilter)

        val gifLoader = ImageLoader.Builder(requireContext())
            .components {
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()

        loadingGif.load(R.drawable.magikarp_loading, gifLoader)

        view.clearFocus()

        adapter = PokemonAdapter { pokemon ->
            val action = PokemonListFragmentDirections
                .actionPokemonListFragmentToPokemonDetailFragment(pokemon.name)
            findNavController().navigate(action)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        setupSearchView()
        setupFilters()

        viewModel.pokemonList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            restoreFiltersState()
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            updateLoadingState()
        }

        viewModel.error.observe(viewLifecycleOwner) {
            if (it != null) Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

        viewModel.filterLoading.observe(viewLifecycleOwner) { isFiltering ->
            updateLoadingState()
        }

        if (!viewModel.hasData()) viewModel.fetchAllPokemons()
        else {
            restoreFiltersState()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.reapplyFilters()
        restoreFiltersState()
    }

    private fun setupSearchView() {
        searchView.isIconified = false
        searchView.queryHint = "Buscar Pokémon"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.search(query)
                hideKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.search(newText)
                return true
            }
        })

        searchView.setOnCloseListener {
            searchView.setQuery("", false)
            searchView.clearFocus()
            searchView.queryHint = "Buscar Pokémon"
            viewModel.search("")
            true
        }

        searchView.clearFocus()
    }

    private fun setupFilters() {
        val types = resources.getStringArray(R.array.pokemon_types).toList()
        val generations = resources.getStringArray(R.array.pokemon_generations).toList()

        typeAdapter = NoFilterArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            types
        )
        genAdapter = NoFilterArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            generations
        )

        typeFilter.setAdapter(typeAdapter)
        genFilter.setAdapter(genAdapter)

        typeFilter.setOnClickListener {
            typeFilter.showDropDown()
        }

        genFilter.setOnClickListener {
            genFilter.showDropDown()
        }

        typeFilter.setOnItemClickListener { _, _, position, _ ->
            viewModel.filterByType(typeAdapter.getItem(position)!!)
        }

        genFilter.setOnItemClickListener { _, _, position, _ ->
            viewModel.filterByGeneration(genAdapter.getItem(position)!!)
        }
    }

    private fun restoreFiltersState() {
        viewModel.getSelectedType()?.let { type ->
            typeFilter.setText(type.replaceFirstChar { it.uppercase() }, false)
        }

        viewModel.getSelectedGenerationLabel()?.let { gen ->
            genFilter.setText(gen, false)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun updateLoadingState() {
        val isLoading = viewModel.loading.value == true
        val isFiltering = viewModel.filterLoading.value == true

        loadingGif.visibility = if (isLoading || isFiltering) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isLoading || isFiltering) View.INVISIBLE else View.VISIBLE
    }
}