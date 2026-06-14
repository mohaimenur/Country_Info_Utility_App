package com.example.countryinfo_utilityapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class CountryResponse(
    val name: Name,
    val flags: Flags,
    val cca2: String,
    val capital: List<String>? = null,
    val population: Long? = null
)

data class Name(
    val common: String
)

data class Flags(
    val png: String
)

interface CountryApi {
    @GET("v3.1/all")
    suspend fun getAllCountries(): List<CountryResponse>
}

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://restcountries.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: CountryApi by lazy {
        retrofit.create(CountryApi::class.java)
    }
}
