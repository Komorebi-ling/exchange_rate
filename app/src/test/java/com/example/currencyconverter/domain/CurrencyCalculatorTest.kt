package com.example.currencyconverter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CurrencyCalculatorTest {
    private val rates = mapOf(
        "JPY" to 1.0,
        "CNY" to 0.046,
        "USD" to 0.0065
    )

    @Test
    fun convertsFromBaseCurrency() {
        val result = CurrencyCalculator.calculate("10000", "JPY", "CNY", rates)

        assertEquals("460.00", CurrencyCalculator.format(result, "CNY"))
    }

    @Test
    fun convertsToBaseCurrency() {
        val result = CurrencyCalculator.calculate("100", "CNY", "JPY", rates)

        assertEquals("2174", CurrencyCalculator.format(result, "JPY"))
    }

    @Test
    fun rejectsInvalidInput() {
        assertNull(CurrencyCalculator.calculate("-1", "JPY", "CNY", rates))
        assertNull(CurrencyCalculator.calculate("abc", "JPY", "CNY", rates))
        assertNull(CurrencyCalculator.calculate("1", "JPY", "EUR", rates))
    }

    @Test
    fun searchesCurrencyByCodeOrName() {
        val result = CurrencyCatalog.search(listOf("JPY", "USD", "CNY"), "美元")

        assertEquals(listOf("USD"), result.map { it.code })
    }
}
