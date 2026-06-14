package com.example.countryinfo_utilityapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.countryinfo_utilityapp.api.CountryResponse
import com.example.countryinfo_utilityapp.viewmodels.CountryViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilityScreen(viewModel: CountryViewModel = viewModel()) {
    val countries by viewModel.countries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var expanded by remember { mutableStateOf(value = false) }
    var selectedCountryForDisplay by remember { mutableStateOf<CountryResponse?>(null) }
    
    // Filter by query or show top 20 if empty
    val filteredCountries = if (searchQuery.isEmpty()) {
        countries.take(20)
    } else {
        countries.filter {
            it.name.common.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Country Search",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    viewModel.onSearchQueryChange(it)
                    expanded = true
                    // Reset display if user starts typing again
                    selectedCountryForDisplay = null
                },
                label = { Text("Search Country") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) expanded = true
                    },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            )

            ExposedDropdownMenu(
                expanded = expanded && filteredCountries.isNotEmpty(),
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                filteredCountries.take(15).forEach { country ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = country.flags.png,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = country.name.common,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        onClick = {
                            viewModel.onSearchQueryChange(country.name.common)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Find the country object that matches the text in the search bar
                val found = countries.find { it.name.common.equals(searchQuery, ignoreCase = true) }
                selectedCountryForDisplay = found
                expanded = false
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = searchQuery.isNotEmpty()
        ) {
            Text("Get Info")
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        selectedCountryForDisplay?.let { country ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = country.flags.png,
                            contentDescription = "Flag of ${country.name.common}",
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = country.name.common, style = MaterialTheme.typography.headlineSmall)
                            Text(text = "Code: ${country.cca2}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    country.capital?.let { capitals ->
                        Text(text = "Capital: ${capitals.joinToString(", ")}", style = MaterialTheme.typography.bodyLarge)
                    }
                    
                    country.population?.let { pop ->
                        Text(text = "Population: ${String.format(Locale.getDefault(), "%,d", pop)}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

    }
}
