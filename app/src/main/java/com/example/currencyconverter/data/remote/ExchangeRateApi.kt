package com.example.currencyconverter.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateApi {
    @GET("v6/{apiKey}/latest/{baseCode}")
    suspend fun getLatestRates(
        @Path("apiKey") apiKey: String,
        @Path("baseCode") baseCode: String
    ): ExchangeRateResponse
}
