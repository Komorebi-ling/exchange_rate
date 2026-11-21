package com.example.currencyconverter.data.local

import androidx.room.Entity

@Entity(
    tableName = "exchange_rates",
    primaryKeys = ["baseCode", "currencyCode"]
)
data class ExchangeRateEntity(
    val baseCode: String,
    val currencyCode: String,
    val rate: Double
)
