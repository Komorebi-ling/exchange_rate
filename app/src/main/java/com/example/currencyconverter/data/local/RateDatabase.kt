package com.example.currencyconverter.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ExchangeRateEntity::class, UpdateRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RateDatabase : RoomDatabase() {
    abstract fun exchangeRateDao(): ExchangeRateDao

    companion object {
        @Volatile
        private var instance: RateDatabase? = null

        fun getInstance(context: Context): RateDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RateDatabase::class.java,
                    "currency_converter.db"
                ).build().also { instance = it }
            }
    }
}
