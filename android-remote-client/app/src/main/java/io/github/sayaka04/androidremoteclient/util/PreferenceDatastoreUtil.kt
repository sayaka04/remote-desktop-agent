package io.github.sayaka04.androidremoteclient.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.sayaka04.androidremoteclient.api.ApiClient
//import io.github.sayaka04.androidremoteclient.ui.client.ClientViewModel
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "local")

object PreferenceDatastoreUtil {

    suspend fun saveString(
        context: Context,
        key: String,
        value: String
    ) {
        context.dataStore.edit { preferences ->

            preferences[stringPreferencesKey(key)] = value
        }
    }

    suspend fun getString(
        context: Context,
        key: String
    ): String? {

        return context.dataStore.data.first()[stringPreferencesKey(key)]
    }

    suspend fun remove(
        context: Context,
        key: String
    ) {

        context.dataStore.edit { preferences ->

            preferences.remove(stringPreferencesKey(key))
        }
    }

    suspend fun clear(
        context: Context
    ) {

        context.dataStore.edit { preferences ->

            preferences.clear()
        }
    }

    suspend fun fetchAll(
        context: Context
    ): String {

        val preferences = context.dataStore.data.first()

        return buildString {

            preferences.asMap().forEach { (key, value) ->

                append("${key.name} = $value\n")
            }
        }
    }
}