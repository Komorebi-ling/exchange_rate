package com.example.currencyconverter.domain

import java.math.BigDecimal
import java.math.RoundingMode

object CurrencyCalculator {
    private const val CALCULATION_SCALE = 12
    private val zeroDecimalCurrencies = setOf("JPY", "KRW", "VND", "CLP", "ISK")

    /**
     * API 汇率均以同一基础货币表示：金额 × 目标汇率 ÷ 原汇率。
     * 返回 null 表示输入或汇率数据不可用。
     */
    fun calculate(
        amountText: String,
        sourceCurrency: String,
        targetCurrency: String,
        rates: Map<String, Double>
    ): BigDecimal? {
        val amount = amountText.trim().takeIf { it.isNotEmpty() }?.toBigDecimalOrNull()
            ?: return null
        if (amount.signum() < 0) return null

        val sourceRate = rates[sourceCurrency.uppercase()]?.toBigDecimalOrNull() ?: return null
        val targetRate = rates[targetCurrency.uppercase()]?.toBigDecimalOrNull() ?: return null
        if (sourceRate.signum() <= 0 || targetRate.signum() <= 0) return null

        return amount
            .multiply(targetRate)
            .divide(sourceRate, CALCULATION_SCALE, RoundingMode.HALF_UP)
            .stripTrailingZeros()
    }

    fun format(amount: BigDecimal?, currencyCode: String): String {
        if (amount == null) return "—"
        val scale = if (currencyCode.uppercase() in zeroDecimalCurrencies) 0 else 2
        return amount.setScale(scale, RoundingMode.HALF_UP).toPlainString()
    }

    private fun Double.toBigDecimalOrNull(): BigDecimal? =
        if (isFinite()) BigDecimal.valueOf(this) else null
}
