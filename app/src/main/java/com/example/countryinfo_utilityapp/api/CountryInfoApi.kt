package com.example.countryinfo_utilityapp.api

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// --- Flag endpoint models ---
// We still fetch flags list to get country names, but we'll build PNG URLs from ISO2 codes
data class FlagResponse(val error: Boolean, val data: List<FlagItem>)
data class FlagItem(val name: String, val flag: String)

// --- Capital endpoint models ---
data class CapitalResponse(val error: Boolean, val data: List<CapitalItem>)
data class CapitalItem(val name: String, val capital: String)

// --- Population endpoint models ---
data class PopulationResponse(val error: Boolean, val data: List<PopulationItem>)
data class PopulationItem(val country: String, val populationCounts: List<PopulationCount>)
data class PopulationCount(val year: Int, val value: Long)

// --- Currency endpoint models ---
data class CurrencyResponse(val error: Boolean, val data: List<CurrencyItem>)
data class CurrencyItem(
    val name: String,
    val currency: String,           // currency code e.g. BDT
    @SerializedName("currency_name")
    val currencyName: String? = null,    // full name e.g. Bangladeshi Taka
    @SerializedName("currency_symbol")
    val currencySymbol: String? = null   // symbol e.g. ৳
)

// --- ISO endpoint models ---
data class IsoResponse(val error: Boolean, val data: List<IsoItem>)
data class IsoItem(
    val name: String,
    @SerializedName("Iso2")
    val iso2: String,   // 2 letter code e.g. BD
    @SerializedName("Iso3")
    val iso3: String    // 3 letter code e.g. BGD
)

// --- Merged model used in UI ---
// This is the single model that holds all country data combined from all endpoints
data class CountryNowItem(
    val name: String,
    val flag: String = "",              // PNG flag URL built from iso2 via flagcdn.com
    val iso2: String = "",              // 2 letter ISO code
    val iso3: String = "",              // 3 letter ISO code
    val capital: String? = null,        // capital city
    val population: Long? = null,       // latest population count
    val currency: String? = null,       // currency code e.g. BDT
    val currencyName: String? = null,  // currency full name
    val currencySymbol: String? = null // currency symbol
)

interface CountryApi {
    // Fetch all country names + original SVG flags (we only use names from this)
    @GET("api/v0.1/countries/flag/images")
    suspend fun getFlags(): Response<FlagResponse>

    // Fetch capital cities for all countries
    @GET("api/v0.1/countries/capital")
    suspend fun getCapitals(): Response<CapitalResponse>

    // Fetch population data for all countries
    @GET("api/v0.1/countries/population")
    suspend fun getPopulations(): Response<PopulationResponse>

    // Fetch currency info for all countries
    @GET("api/v0.1/countries/currency")
    suspend fun getCurrencies(): Response<CurrencyResponse>

    // Fetch ISO2 and ISO3 codes for all countries
    @GET("api/v0.1/countries/iso")
    suspend fun getIsoCodes(): Response<IsoResponse>
}

object RetrofitInstance {
    // Logging interceptor to see API requests/responses in Logcat
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://countriesnow.space/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: CountryApi by lazy {
        retrofit.create(CountryApi::class.java)
    }
}