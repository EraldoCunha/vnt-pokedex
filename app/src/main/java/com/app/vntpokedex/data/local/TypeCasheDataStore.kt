package com.app.vntpokedex.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.typeDataStore by preferencesDataStore(name = "type_cache")

class TypeCacheDataStore(private val context: Context) {

    private fun keyFor(id: Int) = stringPreferencesKey("pokemon_type_$id")
    suspend fun saveType(id: Int, type: String) {
        context.typeDataStore.edit { prefs ->
            prefs[keyFor(id)] = type
        }
    }

    suspend fun getType(id: Int): String? {
        val prefs = context.typeDataStore.data.first()
        return prefs[keyFor(id)]
    }
}