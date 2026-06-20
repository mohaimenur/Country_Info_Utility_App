package com.example.countryinfo_utilityapp.viewmodels

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.countryinfo_utilityapp.preferences.LanguagePreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class LanguageViewModel(application: Application) : AndroidViewModel(application) {

    // LanguagePreference instance using app context
    private val languagePreference = LanguagePreference(application)

    // Exposes saved language code as StateFlow — UI observes this
    val selectedLanguageCode: StateFlow<String> = languagePreference.languageCode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "en",  // default before DataStore loads
        )

    // Called when user taps a language — saves to DataStore + applies locale
    fun selectLanguage(code: String, context: android.content.Context) {
        viewModelScope.launch {
            languagePreference.saveLanguage(code)
            applyLocale(code, context)
        }
    }

    // Applies the selected locale to the app
    private fun applyLocale(code: String, context: android.content.Context) {
        val locale = Locale.forLanguageTag(code)
        Locale.setDefault(locale)
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        // Restart activity to apply language change
        val activity = context as? Activity
        val intent = activity?.intent
        if ((activity != null) && (intent != null)) {
            activity.finish()
            activity.startActivity(intent)
        }
    }
}
