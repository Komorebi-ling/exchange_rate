package com.example.currencyconverter.data.remote

import com.example.currencyconverter.BuildConfig
import java.io.IOException

class ExchangeRateRemoteDataSource(
    private val api: ExchangeRateApi = NetworkModule.exchangeRateApi
) {
    suspend fun fetchLatestRates(baseCode: String): ExchangeRateResponse {
        val apiKey = BuildConfig.EXCHANGE_RATE_API_KEY.trim()
        require(apiKey.isNotEmpty()) {
            "Exchange Rate API Key 未配置，请通过 Gradle 属性或环境变量 EXCHANGE_RATE_API_KEY 配置。"
        }

        val normalizedBaseCode = baseCode.trim().uppercase()
        require(normalizedBaseCode.length == 3) { "基础货币代码必须是 3 位字母。" }

        val response = try {
            api.getLatestRates(apiKey, normalizedBaseCode)
        } catch (exception: IOException) {
            throw ExchangeRateNetworkException("无法连接汇率服务，请检查网络后重试。", exception)
        } catch (exception: Exception) {
            throw ExchangeRateNetworkException("获取汇率时发生网络错误。", exception)
        }

        if (response.result != "success") {
            throw ExchangeRateApiException(
                message = "汇率服务返回错误：${response.errorType ?: "unknown-error"}",
                errorType = response.errorType
            )
        }

        validateResponse(response, normalizedBaseCode)
        return response
    }

    private fun validateResponse(response: ExchangeRateResponse, expectedBaseCode: String) {
        require(response.baseCode?.equals(expectedBaseCode, ignoreCase = true) == true) {
            "汇率服务返回的基础货币与请求不一致。"
        }
        require(response.conversionRates.isNotEmpty()) {
            "汇率服务返回了空的汇率数据。"
        }
        require(response.conversionRates.keys.all { it.length == 3 }) {
            "汇率服务返回了无效的货币代码。"
        }
        require(response.conversionRates.values.all { it.isFinite() && it > 0.0 }) {
            "汇率服务返回了无效的汇率数值。"
        }
        require(response.conversionRates[expectedBaseCode] == 1.0) {
            "汇率服务返回的基础货币汇率无效。"
        }
    }
}

class ExchangeRateNetworkException(message: String, cause: Throwable? = null) :
    IOException(message, cause)

class ExchangeRateApiException(
    message: String,
    val errorType: String?
) : IOException(message)
