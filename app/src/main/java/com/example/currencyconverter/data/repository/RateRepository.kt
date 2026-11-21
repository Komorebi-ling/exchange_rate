package com.example.currencyconverter.data.repository

import com.example.currencyconverter.data.local.ExchangeRateDao
import com.example.currencyconverter.data.local.ExchangeRateEntity
import com.example.currencyconverter.data.local.UpdateRecordEntity
import com.example.currencyconverter.data.remote.ExchangeRateResponse
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

class RateRepository(
    private val dao: ExchangeRateDao,
    private val clockZone: ZoneId = ZoneId.systemDefault()
) {
    fun observeRates(baseCode: String): Flow<List<ExchangeRateEntity>> =
        dao.observeRates(baseCode.uppercase())

    fun observeUpdateRecord(baseCode: String): Flow<UpdateRecordEntity?> =
        dao.observeUpdateRecord(baseCode.uppercase())

    suspend fun getRates(baseCode: String): List<ExchangeRateEntity> =
        dao.getRates(baseCode.uppercase())

    suspend fun getUpdateRecord(baseCode: String): UpdateRecordEntity? =
        dao.getUpdateRecord(baseCode.uppercase())

    suspend fun saveSuccessfulResponse(response: ExchangeRateResponse) {
        val baseCode = requireNotNull(response.baseCode).uppercase()
        require(response.conversionRates.isNotEmpty()) { "不能保存空的汇率数据。" }

        val now = Instant.now()
        val today = LocalDate.now(clockZone)
        val rates = response.conversionRates.map { (currencyCode, rate) ->
            ExchangeRateEntity(
                baseCode = baseCode,
                currencyCode = currencyCode.uppercase(),
                rate = rate
            )
        }
        val record = UpdateRecordEntity(
            baseCode = baseCode,
            dataDate = response.timeLastUpdateUtc,
            apiLastUpdateUtc = response.timeLastUpdateUtc,
            apiNextUpdateUtc = response.timeNextUpdateUtc,
            lastSuccessfulUpdateDate = today.toString(),
            lastSuccessfulUpdateEpochMillis = now.toEpochMilli()
        )

        dao.replaceRatesAndUpdateRecord(baseCode, rates, record)
    }
}
