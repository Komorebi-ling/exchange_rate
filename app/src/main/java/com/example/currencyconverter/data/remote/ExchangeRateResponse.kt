package com.example.currencyconverter.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRateResponse(
    val result: String,
    @SerialName("base_code") val baseCode: String? = null,
    @SerialName("time_last_update_utc") val timeLastUpdateUtc: String? = null,
    @SerialName("time_next_update_utc") val timeNextUpdateUtc: String? = null,
    @SerialName("conversion_rates") val conversionRates: Map<String, Double> = emptyMap(),
    @SerialName("error-type") val errorType: String? = null
)
