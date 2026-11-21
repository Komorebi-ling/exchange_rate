package com.example.currencyconverter.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ExchangeRateDao {
    @Query("SELECT * FROM exchange_rates WHERE baseCode = :baseCode ORDER BY currencyCode")
    abstract fun observeRates(baseCode: String): Flow<List<ExchangeRateEntity>>

    @Query("SELECT * FROM exchange_rates WHERE baseCode = :baseCode ORDER BY currencyCode")
    abstract suspend fun getRates(baseCode: String): List<ExchangeRateEntity>

    @Query("SELECT * FROM rate_update_records WHERE baseCode = :baseCode LIMIT 1")
    abstract fun observeUpdateRecord(baseCode: String): Flow<UpdateRecordEntity?>

    @Query("SELECT * FROM rate_update_records WHERE baseCode = :baseCode LIMIT 1")
    abstract suspend fun getUpdateRecord(baseCode: String): UpdateRecordEntity?

    @Query("DELETE FROM exchange_rates WHERE baseCode = :baseCode")
    protected abstract suspend fun deleteRates(baseCode: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertRates(rates: List<ExchangeRateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertUpdateRecord(record: UpdateRecordEntity)

    @Transaction
    open suspend fun replaceRatesAndUpdateRecord(
        baseCode: String,
        rates: List<ExchangeRateEntity>,
        record: UpdateRecordEntity
    ) {
        deleteRates(baseCode)
        insertRates(rates)
        insertUpdateRecord(record)
    }
}
