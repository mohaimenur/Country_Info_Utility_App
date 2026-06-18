package com.example.countryinfo_utilityapp.screens

import android.app.Activity
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.countryinfo_utilityapp.R
import com.example.countryinfo_utilityapp.viewmodels.LanguageViewModel

// List of supported languages with their display name and language code
data class Language(
    val displayName: String,  // shown in UI e.g. "English"
    val nativeName: String,   // shown in native script e.g. "বাংলা"
    val code: String          // language code e.g. "en"
)

val supportedLanguages = listOf(
    Language("English", "English", "en"),
    Language("Bengali", "বাংলা", "bn"),
    Language("Hindi", "हिन्दी", "hi"),
    Language("Arabic", "العربية", "ar"),
    Language("French", "Français", "fr"),
    Language("Spanish", "Español", "es"),
    Language("German", "Deutsch", "de"),
    Language("Chinese", "中文", "zh"),
    Language("Japanese", "日本語", "ja"),
    Language("Portuguese", "Português", "pt")
)

@Composable
fun SettingsScreen(languageViewModel: LanguageViewModel = viewModel()) {
    val context = LocalContext.current

    // Observe the saved language code from DataStore via ViewModel
    val selectedLanguageCode by languageViewModel.selectedLanguageCode.collectAsState()

    // Show confirmation dialog before applying language change
    var pendingLanguage by remember { mutableStateOf<Language?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Screen title
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        HorizontalDivider()

        // Section title
        Text(
            text = stringResource(R.string.language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(R.string.language_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Language options list
        supportedLanguages.forEach { language ->
            LanguageItem(
                language = language,
                isSelected = selectedLanguageCode == language.code,
                onClick = {
                    // Only show dialog if selecting a different language
                    if (selectedLanguageCode != language.code) {
                        pendingLanguage = language
                    }
                }
            )
        }

        HorizontalDivider()

        // Show currently selected language at the bottom
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.selected_language),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = supportedLanguages.firstOrNull {
                    it.code == selectedLanguageCode
                }?.displayName ?: "English",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    // Confirmation dialog before restarting app
    pendingLanguage?.let { language ->
        AlertDialog(
            onDismissRequest = { pendingLanguage = null },
            title = {
                Text(stringResource(R.string.change_language_title))
            },
            text = {
                Text(stringResource(R.string.change_language_message, language.displayName, language.nativeName))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Save and apply the new language
                        languageViewModel.selectLanguage(language.code, context as Activity)
                        pendingLanguage = null
                    }
                ) {
                    Text(stringResource(R.string.apply), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingLanguage = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Highlight the selected item with a border and background
    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outline

    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                // Language name in English
                Text(
                    text = language.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                // Language name in its own script
                Text(
                    text = language.nativeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Show checkmark icon on selected language
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
