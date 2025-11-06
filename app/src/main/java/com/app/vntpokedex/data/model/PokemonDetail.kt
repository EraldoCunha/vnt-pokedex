package com.app.vntpokedex.data.model

data class PokemonDetail(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<TypeSlot>,
    val stats: List<StatSlot>,
    val sprites: Sprites
)

data class TypeSlot(
    val slot: Int,
    val type: Type
)

data class Type(
    val name: String
)

data class StatSlot(
    val base_stat: Int,
    val stat: Stat
)

data class Stat(
    val name: String
)

data class Sprites(
    val front_default: String
)
