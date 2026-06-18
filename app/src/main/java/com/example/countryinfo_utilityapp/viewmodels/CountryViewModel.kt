package com.example.countryinfo_utilityapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.countryinfo_utilityapp.api.CountryNowItem
import com.example.countryinfo_utilityapp.api.RetrofitInstance
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CountryViewModel : ViewModel() {
    // Holds the merged list of all countries
    private val _countries = MutableStateFlow<List<CountryNowItem>>(emptyList())
    val countries: StateFlow<List<CountryNowItem>> = _countries.asStateFlow()

    // Tracks loading state to show spinner in UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Holds error message if any API call fails
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Holds the country currently chosen in the selection dialog (survives rotation)
    private val _selectedCountry = MutableStateFlow<CountryNowItem?>(null)
    val selectedCountry: StateFlow<CountryNowItem?> = _selectedCountry.asStateFlow()

    // Survives rotation - holds the country shown in the info card (new for rotation handling)
    private val _confirmedCountry = MutableStateFlow<CountryNowItem?>(null)
    val confirmedCountry: StateFlow<CountryNowItem?> = _confirmedCountry.asStateFlow()


    init {
        // Fetch countries when ViewModel is first created
        fetchCountries()
    }


    // Called when user taps a country in the dialog (new for rotation handling)
    fun selectCountry(country: CountryNowItem?) {
        _selectedCountry.value = country
        _confirmedCountry.value = null // reset info card when new country is selected
    }

    // Called when user taps Get Info button (new for rotation handling)
    fun confirmCountry() {
        _confirmedCountry.value = _selectedCountry.value
    }


    fun fetchCountries() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Launch all 5 API calls in parallel to save time
                val flagsDeferred = async { RetrofitInstance.api.getFlags() }
                val capitalsDeferred = async { RetrofitInstance.api.getCapitals() }
                val populationsDeferred = async { RetrofitInstance.api.getPopulations() }
                val currenciesDeferred = async { RetrofitInstance.api.getCurrencies() }
                val isoDeferred = async { RetrofitInstance.api.getIsoCodes() }

                // Wait for all responses and extract data lists
                val flags = flagsDeferred.await().body()?.data ?: emptyList()
                val capitals = capitalsDeferred.await().body()?.data ?: emptyList()
                val populations = populationsDeferred.await().body()?.data ?: emptyList()
                val currencies = currenciesDeferred.await().body()?.data ?: emptyList()
                val isoCodes = isoDeferred.await().body()?.data ?: emptyList()

                // Build lookup maps by country name for quick access
                val capitalMap = capitals.associate { it.name to it.capital }

                // Triple holds (currencyCode, currencyName, currencySymbol)
                val currencyMap = currencies.associate {
                    it.name to Triple(it.currency, it.currency_name, it.currency_symbol)
                }

                // Get the latest population by picking the highest year entry
                val populationMap = populations.associate { pop ->
                    pop.country to (pop.populationCounts.maxByOrNull { it.year }?.value)
                }

                // Pair holds (iso2, iso3)
                val isoMap = isoCodes.associate { it.name to Pair(it.Iso2, it.Iso3) }

                // Merge all data using flags list as base (has all country names)
                val merged = flags.map { flag ->
                    val iso2 = isoMap[flag.name]?.first ?: ""

                    CountryNowItem(
                        name = flag.name,
                        // Build reliable PNG flag URL from iso2 code using flagcdn.com
                        // e.g. iso2 = "bd" → https://flagcdn.com/w160/bd.png
                        flag = if (iso2.isNotEmpty())
                            "https://flagcdn.com/w160/${iso2.lowercase()}.png"
                        else "",
                        iso2 = iso2,
                        iso3 = isoMap[flag.name]?.second ?: "",
                        capital = capitalMap[flag.name],
                        population = populationMap[flag.name],
                        currency = currencyMap[flag.name]?.first,
                        currency_name = currencyMap[flag.name]?.second,
                        currency_symbol = currencyMap[flag.name]?.third
                    )
                }.sortedBy { it.name } // Sort alphabetically by country name

                _countries.value = merged

            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = e.localizedMessage ?: "Connection failed"
            } finally {
                _isLoading.value = false
            }
        }
    }
}