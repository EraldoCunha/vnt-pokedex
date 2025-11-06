package com.app.vntpokedex.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.vntpokedex.R
import com.app.vntpokedex.viewmodel.PokemonViewModel
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class PokemonListFragment : Fragment() {

    private val viewModel: PokemonViewModel by viewModels()
    private lateinit var adapter: PokemonAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var typeFilter: MaterialAutoCompleteTextView
    private lateinit var genFilter: MaterialAutoCompleteTextView

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
        progressBar = view.findViewById(R.id.progress_bar)
        typeFilter = view.findViewById(R.id.typeFilter)
        genFilter = view.findViewById(R.id.genFilter)

        view.clearFocus()

        adapter = PokemonAdapter { pokemon ->
            val action = PokemonListFragmentDirections
                .actionPokemonListFragmentToPokemonDetailFragment(pokemon.name)
            findNavController().navigate(action)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.pokemonList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) {
            if (it != null) Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

        if (!viewModel.hasData()) viewModel.fetchAllPokemons()

        setupSearchView()
        setupFilters()

        searchView.clearFocus()
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
        val types = resources.getStringArray(R.array.pokemon_types)
        val generations = resources.getStringArray(R.array.pokemon_generations)

        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        val genAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, generations)

        typeFilter.setAdapter(typeAdapter)
        genFilter.setAdapter(genAdapter)

        typeFilter.isFocusable = false
        typeFilter.isClickable = true
        genFilter.isFocusable = false
        genFilter.isClickable = true

        typeFilter.setOnClickListener { typeFilter.showDropDown() }
        genFilter.setOnClickListener { genFilter.showDropDown() }

        typeFilter.setOnItemClickListener { parent, _, position, _ ->
            val selectedType = parent.getItemAtPosition(position) as String
            viewModel.filterByType(selectedType)
        }

        genFilter.setOnItemClickListener { parent, _, position, _ ->
            val selectedGen = parent.getItemAtPosition(position) as String
            viewModel.filterByGeneration(selectedGen)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
