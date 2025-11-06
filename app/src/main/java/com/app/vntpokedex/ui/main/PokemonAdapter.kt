package com.app.vntpokedex.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.app.vntpokedex.data.model.Pokemon
import com.app.vntpokedex.databinding.ItemPokemonBinding

class PokemonAdapter(
    private val onClick: (Pokemon) -> Unit
) : ListAdapter<Pokemon, PokemonAdapter.PokemonViewHolder>(PokemonDiffCallback()) {

    inner class PokemonViewHolder(val binding: ItemPokemonBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PokemonViewHolder {
        val binding = ItemPokemonBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PokemonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = getItem(position)
        holder.binding.textName.text = pokemon.name.replaceFirstChar { it.uppercase() }
        holder.binding.textIdPokemon.text = "#${pokemon.id.toString().padStart(3, '0')}"
        holder.binding.imagePokemon.load(pokemon.imageUrl)
        holder.binding.root.setOnClickListener { onClick(pokemon) }
    }

    class PokemonDiffCallback : DiffUtil.ItemCallback<Pokemon>() {
        override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon) =
            oldItem == newItem
    }
}