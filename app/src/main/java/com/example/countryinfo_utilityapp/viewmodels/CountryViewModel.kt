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

    private val _isLoading = MutableStateFlow(value = false)
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
                val flagsDef = async { try { RetrofitInstance.api.getFlags() } catch (_: Exception) { null } }
                val capitalsDef = async { try { RetrofitInstance.api.getCapitals() } catch (_: Exception) { null } }
                val populationsDef = async { try { RetrofitInstance.api.getPopulations() } catch (_: Exception) { null } }
                val currenciesDef = async { try { RetrofitInstance.api.getCurrencies() } catch (_: Exception) { null } }
                val isoDef = async { try { RetrofitInstance.api.getIsoCodes() } catch (_: Exception) { null } }

                val flags = flagsDef.await()?.body()?.data ?: emptyList()
                val capitals = capitalsDef.await()?.body()?.data ?: emptyList()
                val populations = populationsDef.await()?.body()?.data ?: emptyList()
                val currencies = currenciesDef.await()?.body()?.data ?: emptyList()
                val isoCodes = isoDef.await()?.body()?.data ?: emptyList()

                if (flags.isEmpty()) {
                    _error.value = "Unable to fetch country list. Please check your connection."
                    return@launch
                }

                val capitalMap = capitals.associateBy({ it.name }) { it.capital }
                val currencyMap = currencies.associateBy({ it.name }) {
                    Triple(it.currency, it.currencyName, it.currencySymbol)
                }

                // Get the latest population by picking the highest year entry
                val populationMap = populations.associateBy({ it.country }) { it.populationCounts.maxByOrNull { pc -> pc.year }?.value }
                val isoMap = isoCodes.associateBy({ it.name }) { Pair(it.iso2, it.iso3) }

                val merged = flags.asSequence().map { flag ->
                    val iso2 = isoMap[flag.name]?.first ?: ""
                    CountryNowItem(
                        name = flag.name,
                        flag = if (iso2.isNotEmpty()) "https://flagcdn.com/w160/${iso2.lowercase()}.png" else "",
                        iso2 = iso2,
                        iso3 = isoMap[flag.name]?.second ?: "",
                        capital = capitalMap[flag.name],
                        population = populationMap[flag.name],
                        currency = currencyMap[flag.name]?.first,
                        currencyName = currencyMap[flag.name]?.second,
                        currencySymbol = currencyMap[flag.name]?.third,
                    )
                }.toList().sortedBy { it.name }

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
