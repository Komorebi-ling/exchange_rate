package com.example.currencyconverter

import android.app.Application
import com.example.currencyconverter.data.local.RateDatabase
import com.example.currencyconverter.data.remote.ExchangeRateRemoteDataSource
import com.example.currencyconverter.data.repository.RateRepository

class CurrencyConverterApplication : Application() {
    val rateRepository: RateRepository by lazy {
        RateRepository(
            dao = RateDatabase.getInstance(this).exchangeRateDao()
        )
    }

    val rateRemoteDataSource: ExchangeRateRemoteDataSource by lazy {
        ExchangeRateRemoteDataSource()
    }
}
