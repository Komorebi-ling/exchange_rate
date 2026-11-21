package com.example.currencyconverter.domain

data class CurrencyInfo(
    val code: String,
    val name: String
)

object CurrencyCatalog {
    private val names = mapOf(
        "AUD" to "澳大利亚元",
        "BRL" to "巴西雷亚尔",
        "CAD" to "加拿大元",
        "CHF" to "瑞士法郎",
        "CNY" to "人民币",
        "EUR" to "欧元",
        "GBP" to "英镑",
        "HKD" to "港元",
        "INR" to "印度卢比",
        "JPY" to "日元",
        "KRW" to "韩元",
        "NZD" to "新西兰元",
        "SGD" to "新加坡元",
        "THB" to "泰铢",
        "USD" to "美元"
    )

    private val commonOrder = listOf("JPY", "USD", "CNY", "EUR", "GBP", "HKD", "KRW", "AUD", "CAD")

    fun fromCodes(codes: Collection<String>): List<CurrencyInfo> {
        val normalizedCodes = codes.map(String::uppercase).distinct()
        return normalizedCodes
            .sortedWith(compareBy({ commonOrder.indexOf(it).takeUnless { index -> index == -1 } ?: Int.MAX_VALUE }, { it }))
            .map { code -> CurrencyInfo(code, names[code] ?: code) }
    }

    fun search(codes: Collection<String>, query: String): List<CurrencyInfo> {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isEmpty()) return fromCodes(codes)
        return fromCodes(codes).filter {
            it.code.lowercase().contains(normalizedQuery) || it.name.contains(normalizedQuery)
        }
    }
}
