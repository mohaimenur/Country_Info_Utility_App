package com.example.countryinfo_utilityapp.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creates a DataStore instance tied to the app context
val Context.dataStore by preferencesDataStore(name = "settings")

class LanguagePreference(private val context: Context) {

    companion object {
        // Key used to store/retrieve the language code in DataStore
        val LANGUAGE_KEY = stringPreferencesKey("selected_language")
    }

    // Returns a Flow that emits the saved language code, defaults to "en"
    val languageCode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: "en"
        }

    // Saves the selected language code to DataStore
    suspend fun saveLanguage(code: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = code
        }
    }
}