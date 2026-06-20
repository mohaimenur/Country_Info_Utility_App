package com.example.countryinfo_utilityapp.screens

import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.countryinfo_utilityapp.R
import com.example.countryinfo_utilityapp.api.CountryNowItem
import com.example.countryinfo_utilityapp.viewmodels.CountryViewModel
import com.example.countryinfo_utilityapp.viewmodels.LanguageViewModel
import java.util.Currency
import java.util.Locale

@Composable
fun UtilityScreen(
    viewModel: CountryViewModel = viewModel(),
    languageViewModel: LanguageViewModel = viewModel(),
) {
    val countries by viewModel.countries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Get current language from DataStore via LanguageViewModel
    val langCode by languageViewModel.selectedLanguageCode.collectAsState()
    val targetLocale = remember(langCode) { 
        try { Locale.forLanguageTag(langCode) } catch (_: Exception) { Locale.getDefault() }
    }

    val tempSelectedCountry by viewModel.selectedCountry.collectAsState()
    val confirmedCountryForDisplay by viewModel.confirmedCountry.collectAsState()

    var showDialog by remember { mutableStateOf(value = false) }
    var internalSearchQuery by remember { mutableStateOf("") }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeLayout(
            countries = countries,
            isLoading = isLoading,
            error = error,
            tempSelectedCountry = tempSelectedCountry,
            confirmedCountryForDisplay = confirmedCountryForDisplay,
            targetLocale = targetLocale,
            onSelectClick = { showDialog = true },
            onExploreClick = { viewModel.confirmCountry() },
            onRetryClick = { viewModel.fetchCountries() }
        )
    } else {
        PortraitLayout(
            countries = countries,
            isLoading = isLoading,
            error = error,
            tempSelectedCountry = tempSelectedCountry,
            confirmedCountryForDisplay = confirmedCountryForDisplay,
            targetLocale = targetLocale,
            onSelectClick = { showDialog = true },
            onExploreClick = { viewModel.confirmCountry() }
        ) { viewModel.fetchCountries() }
    }

    val dismissDialog = { showDialog = false }

    // Country selection dialog
    if (showDialog) {
        Dialog(onDismissRequest = dismissDialog) {
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
                        text = stringResource(R.string.select_country),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = internalSearchQuery,
                        onValueChange = { internalSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Localize country names for filtering/search
                    val filteredList = countries.filter { country ->
                        val localizedName = Locale.Builder().setRegion(country.iso2).build().getDisplayCountry(targetLocale)
                        country.name.contains(internalSearchQuery, ignoreCase = true) || 
                        localizedName.contains(internalSearchQuery, ignoreCase = true)
                    }

                    if (filteredList.isEmpty() && !isLoading) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.no_countries_found), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredList) { country ->
                                CountryListItem(
                                    country = country,
                                    targetLocale = targetLocale,
                                    onClick = {
                                        viewModel.selectCountry(country)
                                        dismissDialog()
                                        internalSearchQuery = ""
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = dismissDialog,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.cancel), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PortraitLayout(
    countries: List<CountryNowItem>,
    isLoading: Boolean,
    error: String?,
    tempSelectedCountry: CountryNowItem?,
    confirmedCountryForDisplay: CountryNowItem?,
    targetLocale: Locale,
    onSelectClick: () -> Unit,
    onExploreClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High spacer to push "GeoBro" towards the middle
        Spacer(modifier = Modifier.height(140.dp))

        // Large "GeoBro" Title Card
        Surface(
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = R.mipmap.ic_launcher,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).background(Color.Transparent, MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 48.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Text(
            text = stringResource(R.string.explore_countries),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if ((error != null) && countries.isEmpty()) {
            ErrorSection(error, onRetryClick)
        } else {
            SelectionSection(isLoading, countries, tempSelectedCountry, targetLocale, onSelectClick, onExploreClick)
        }

        if (confirmedCountryForDisplay != null) {
            Spacer(modifier = Modifier.height(32.dp))
            InfoCard(confirmedCountryForDisplay, targetLocale = targetLocale)
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun LandscapeLayout(
    countries: List<CountryNowItem>,
    isLoading: Boolean,
    error: String?,
    tempSelectedCountry: CountryNowItem?,
    confirmedCountryForDisplay: CountryNowItem?,
    targetLocale: Locale,
    onSelectClick: () -> Unit,
    onExploreClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: Controls
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = R.mipmap.ic_launcher,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                text = stringResource(R.string.explore_countries),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if ((error != null) && countries.isEmpty()) {
                ErrorSection(error, onRetryClick)
            } else {
                SelectionSection(isLoading, countries, tempSelectedCountry, targetLocale, onSelectClick, onExploreClick)
            }
        }

        // Right Column: Info Card
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            confirmedCountryForDisplay?.let { country ->
                InfoCard(country, isLandscape = true, targetLocale = targetLocale)
            } ?: Spacer(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun SelectionSection(
    isLoading: Boolean,
    countries: List<CountryNowItem>,
    tempSelectedCountry: CountryNowItem?,
    targetLocale: Locale,
    onSelectClick: () -> Unit,
    onExploreClick: () -> Unit
) {
    val localizedSelectedName = tempSelectedCountry?.let {
        Locale.Builder().setRegion(it.iso2).build().getDisplayCountry(targetLocale)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.country),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.small
                )
                .clickable { if (!isLoading) onSelectClick() }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLoading && countries.isEmpty()) stringResource(R.string.loading_countries)
                    else localizedSelectedName ?: stringResource(R.string.select_country),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (tempSelectedCountry == null)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (isLoading && countries.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = onExploreClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = tempSelectedCountry != null,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(stringResource(R.string.explore), style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
fun InfoCard(
    country: CountryNowItem, 
    isLandscape: Boolean = false,
    targetLocale: Locale
) {
    val localizedCountryName = remember(country.iso2, targetLocale) {
        Locale.Builder().setRegion(country.iso2).build().getDisplayCountry(targetLocale)
    }

    val localizedCapitalRes = remember(country.capital, targetLocale) {
        val capital = country.capital ?: return@remember null
        when (capital.lowercase()) {
            "canberra" -> R.string.capital_canberra
            "luanda" -> R.string.capital_luanda
            "washington, d.c." -> R.string.capital_washington_dc
            "london" -> R.string.capital_london
            "new delhi" -> R.string.capital_new_delhi
            "ottawa" -> R.string.capital_ottawa
            "paris" -> R.string.capital_paris
            "berlin" -> R.string.capital_berlin
            "tokyo" -> R.string.capital_tokyo
            "dhaka" -> R.string.capital_dhaka
            "abu dhabi" -> R.string.capital_abu_dhabi
            "madrid" -> R.string.capital_madrid
            "rome" -> R.string.capital_rome
            "cairo" -> R.string.capital_cairo
            "beijing" -> R.string.capital_beijing
            else -> null
        }
    }
    
    val localizedCapital = localizedCapitalRes?.let { stringResource(it) } ?: country.capital ?: "N/A"

    val localizedCurrencyInfo = remember(country.currency, targetLocale) {
        buildString {
            try {
                if (country.currency != null) {
                    val curr = Currency.getInstance(country.currency)
                    append(curr.getDisplayName(targetLocale))
                    val symbol = curr.getSymbol(targetLocale)
                    if (symbol != country.currency) {
                        append(" ($symbol)")
                    }
                    append(" ${country.currency}")
                } else {
                    append(country.currencyName ?: "N/A")
                }
            } catch (_: Exception) {
                append(country.currencyName ?: country.currency ?: "N/A")
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = country.flag,
                    contentDescription = "Flag of $localizedCountryName",
                    modifier = Modifier
                        .size(if (isLandscape) 56.dp else 72.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = localizedCountryName,
                        style = if (isLandscape) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${stringResource(R.string.iso)}: ${country.iso2} / ${country.iso3}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = if (isLandscape) 8.dp else 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            DetailItem(
                label = stringResource(R.string.capital),
                value = localizedCapital,
                isLandscape = isLandscape
            )
            DetailItem(
                label = stringResource(R.string.population),
                value = "%,d".format(targetLocale, country.population ?: 0),
                isLandscape = isLandscape
            )
            DetailItem(
                label = stringResource(R.string.currency),
                value = localizedCurrencyInfo,
                isLandscape = isLandscape
            )
        }
    }
}

@Composable
fun ErrorSection(error: String, onRetryClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.failed_to_load, error),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 8.dp),
            style = MaterialTheme.typography.bodySmall
        )
        Button(onClick = onRetryClick) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.retry), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, isLandscape: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isLandscape) 2.dp else 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isLandscape) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = if (isLandscape) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CountryListItem(
    country: CountryNowItem, 
    targetLocale: Locale,
    onClick: () -> Unit
) {
    val localizedName = Locale.Builder().setRegion(country.iso2).build().getDisplayCountry(targetLocale)

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
            AsyncImage(
                model = country.flag,
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.LightGray, MaterialTheme.shapes.extraSmall)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = localizedName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
