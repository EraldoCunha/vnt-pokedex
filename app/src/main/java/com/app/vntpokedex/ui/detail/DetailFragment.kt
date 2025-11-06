package com.app.vntpokedex.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.app.vntpokedex.R
import com.app.vntpokedex.viewmodel.PokemonDetailViewModel
import com.app.vntpokedex.util.getColorByType

class DetailFragment : Fragment() {

    private val viewModel: PokemonDetailViewModel by viewModels()

    private lateinit var detailRoot: View
    private lateinit var imageLoading: ImageView
    private lateinit var imagePokemon: ImageView
    private lateinit var textName: TextView
    private lateinit var textId: TextView
    private lateinit var textTypes: TextView
    private lateinit var textMeasures: TextView
    private lateinit var textStats: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.detail_toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.detail_title)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        detailRoot = view.findViewById(R.id.detail_root)
        imageLoading = view.findViewById(R.id.image_loading)
        imagePokemon = view.findViewById(R.id.image_pokemon)
        textName = view.findViewById(R.id.text_name)
        textId = view.findViewById(R.id.text_id)
        textTypes = view.findViewById(R.id.text_types)
        textMeasures = view.findViewById(R.id.pokemon_measures)
        textStats = view.findViewById(R.id.pokemon_stats)

        val defaultColor = requireContext().getColorByType("normal")
        detailRoot.setBackgroundColor(defaultColor)
        toolbar.setBackgroundColor(defaultColor)

        val pokemonName = arguments?.getString("name")
        if (!pokemonName.isNullOrBlank()) {
            viewModel.fetchPokemonDetail(pokemonName)
        }

        viewModel.pokemonDetail.observe(viewLifecycleOwner) { detail ->
            detail?.let {

                imageLoading.visibility = View.GONE
                imagePokemon.visibility = View.VISIBLE

                imagePokemon.load(it.sprites.front_default)

                textName.text = it.name.replaceFirstChar { c -> c.uppercase() }
                textId.text = "#${it.id.toString().padStart(3, '0')}"

                textTypes.text = it.types.joinToString(" / ") { slot ->
                    slot.type.name.replaceFirstChar { c -> c.uppercase() }
                }

                textMeasures.text = getString(R.string.pokemon_height_weight, it.height/10.0, it.weight/10.0)

                val hp = it.stats.find { s -> s.stat.name == "hp" }?.base_stat ?: 0
                val attack = it.stats.find { s -> s.stat.name == "attack" }?.base_stat ?: 0
                val defense = it.stats.find { s -> s.stat.name == "defense" }?.base_stat ?: 0
                textStats.text = getString(R.string.pokemon_stats_format, hp, attack, defense)

                val mainType = it.types.firstOrNull()?.type?.name ?: "normal"
                val color = requireContext().getColorByType(mainType)
                detailRoot.setBackgroundColor(color)

                toolbar.setBackgroundColor(color)
            }
        }
    }
}
