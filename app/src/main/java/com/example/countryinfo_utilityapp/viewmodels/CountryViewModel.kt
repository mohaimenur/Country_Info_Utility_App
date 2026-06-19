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
    private val _countries = MutableStateFlow<List<CountryNowItem>>(emptyList())
    val countries: StateFlow<List<CountryNowItem>> = _countries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedCountry = MutableStateFlow<CountryNowItem?>(null)
    val selectedCountry: StateFlow<CountryNowItem?> = _selectedCountry.asStateFlow()

    private val _confirmedCountry = MutableStateFlow<CountryNowItem?>(null)
    val confirmedCountry: StateFlow<CountryNowItem?> = _confirmedCountry.asStateFlow()

    init {
        fetchCountries()
    }

    fun selectCountry(country: CountryNowItem?) {
        _selectedCountry.value = country
        // We don't reset confirmed country here to avoid flicker if user is just browsing the selection list
    }

    fun confirmCountry() {
        _confirmedCountry.value = _selectedCountry.value
    }

    fun fetchCountries() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Parallel fetching with error handling for each
                val flagsDef = async { try { RetrofitInstance.api.getFlags() } catch (e: Exception) { null } }
                val capitalsDef = async { try { RetrofitInstance.api.getCapitals() } catch (e: Exception) { null } }
                val populationsDef = async { try { RetrofitInstance.api.getPopulations() } catch (e: Exception) { null } }
                val currenciesDef = async { try { RetrofitInstance.api.getCurrencies() } catch (e: Exception) { null } }
                val isoDef = async { try { RetrofitInstance.api.getIsoCodes() } catch (e: Exception) { null } }

                val flags = flagsDef.await()?.body()?.data ?: emptyList()
                val capitals = capitalsDef.await()?.body()?.data ?: emptyList()
                val populations = populationsDef.await()?.body()?.data ?: emptyList()
                val currencies = currenciesDef.await()?.body()?.data ?: emptyList()
                val isoCodes = isoDef.await()?.body()?.data ?: emptyList()

                if (flags.isEmpty()) {
                    _error.value = "Unable to fetch country list. Please check your connection."
                    return@launch
                }

                val capitalMap = capitals.associate { it.name to it.capital }
                val currencyMap = currencies.associate { it.name to Triple(it.currency, it.currency_name, it.currency_symbol) }
                val populationMap = populations.associate { it.country to (it.populationCounts.maxByOrNull { pc -> pc.year }?.value) }
                val isoMap = isoCodes.associate { it.name to Pair(it.Iso2, it.Iso3) }

                val merged = flags.map { flag ->
                    val iso2 = isoMap[flag.name]?.first ?: ""
                    CountryNowItem(
                        name = flag.name,
                        flag = if (iso2.isNotEmpty()) "https://flagcdn.com/w160/${iso2.lowercase()}.png" else "",
                        iso2 = iso2,
                        iso3 = isoMap[flag.name]?.second ?: "",
                        capital = capitalMap[flag.name],
                        population = populationMap[flag.name],
                        currency = currencyMap[flag.name]?.first,
                        currency_name = currencyMap[flag.name]?.second,
                        currency_symbol = currencyMap[flag.name]?.third
                    )
                }.sortedBy { it.name }

                _countries.value = merged
                _error.value = null
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Connection error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
