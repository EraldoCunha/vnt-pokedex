package com.app.vntpokedex.data.model

data class Pokemon(
    val name: String,
    val url: String
) {
    val id: Int
        get() = url.split("/").dropLast(1).last().toInt()

    val imageUrl: String
        get() = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
}