package com.example.currencyconverter.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rate_update_records")
data class UpdateRecordEntity(
    @PrimaryKey val baseCode: String,
    val dataDate: String?,
    val apiLastUpdateUtc: String?,
    val apiNextUpdateUtc: String?,
    val lastSuccessfulUpdateDate: String,
    val lastSuccessfulUpdateEpochMillis: Long
)
