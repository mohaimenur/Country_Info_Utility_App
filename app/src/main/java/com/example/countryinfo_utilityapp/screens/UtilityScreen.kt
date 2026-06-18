package com.example.countryinfo_utilityapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.countryinfo_utilityapp.api.CountryNowItem
import com.example.countryinfo_utilityapp.viewmodels.CountryViewModel

@Composable
fun UtilityScreen(viewModel: CountryViewModel = viewModel()) {
    val countries by viewModel.countries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // These now come from ViewModel so they survive screen rotation (new for rotation handling)
    val tempSelectedCountry by viewModel.selectedCountry.collectAsState()
    val confirmedCountryForDisplay by viewModel.confirmedCountry.collectAsState()

    // These are fine in remember - dialog closes and search clears on rotation is acceptable
    var showDialog by remember { mutableStateOf(false) }
    var internalSearchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Explore Countries",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Show error + retry button if loading failed and no countries loaded
        if (error != null && countries.isEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = { viewModel.fetchCountries() }) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        } else {
            // Country selector dropdown box
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Country",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.small
                        )
                        .clickable { if (!isLoading) showDialog = true }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Show loading text or selected country name
                        Text(
                            text = if (isLoading && countries.isEmpty()) "Loading countries..."
                            else tempSelectedCountry?.name ?: "Select Country",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (tempSelectedCountry == null)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        // Show spinner while loading or arrow icon when ready
                        if (isLoading && countries.isEmpty()) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Get Info button - only enabled when a country is selected
            Button(
                onClick = {
                    viewModel.confirmCountry()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = tempSelectedCountry != null,
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Explore", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Show country info card when a country is confirmed
        confirmedCountryForDisplay?.let { country ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Flag image loaded from flagcdn.com using iso2 code
                        AsyncImage(
                            model = country.flag,
                            contentDescription = "Flag of ${country.name}",
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.LightGray, MaterialTheme.shapes.small)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = country.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            // Show both ISO2 and ISO3 codes
                            Text(
                                text = "ISO: ${country.iso2} / ${country.iso3}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Country detail rows
                    DetailItem(
                        label = "Capital",
                        value = country.capital ?: "N/A"
                    )
                    DetailItem(
                        label = "Population",
                        value = "%,d".format(country.population ?: 0)
                    )
                    // Build currency string: e.g. "Bangladeshi Taka (৳) BDT"
                    DetailItem(
                        label = "Currency",
                        value = buildString {
                            append(country.currency_name ?: country.currency ?: "N/A")
                            if (country.currency_symbol != null) append(" (${country.currency_symbol})")
                            if (country.currency != null && country.currency_name != null) append(" ${country.currency}")
                        }
                    )
                }
            }
        }
    }

    // Country selection dialog
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f),
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Select Country",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Search field to filter countries
                    OutlinedTextField(
                        value = internalSearchQuery,
                        onValueChange = { internalSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search country name...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter countries based on search query
                    val filteredList = countries.filter {
                        it.name.contains(internalSearchQuery, ignoreCase = true)
                    }

                    if (filteredList.isEmpty() && !isLoading) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text("No countries found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        // Scrollable list of countries
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredList) { country ->
                                CountryListItem(
                                    country = country,
                                    onClick = {
                                        viewModel.selectCountry(country)
                                        showDialog = false
                                        internalSearchQuery = ""
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("CANCEL", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Single country row item shown in the dialog list
@Composable
fun CountryListItem(country: CountryNowItem, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small flag thumbnail next to country name
            AsyncImage(
                model = country.flag,
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.LightGray, MaterialTheme.shapes.extraSmall)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = country.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Reusable row for label + value pairs in the info card
@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}
