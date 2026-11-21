package com.example.currencyconverter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.currencyconverter.data.local.ExchangeRateEntity
import com.example.currencyconverter.data.local.UpdateRecordEntity
import com.example.currencyconverter.data.remote.ExchangeRateRemoteDataSource
import com.example.currencyconverter.data.repository.RateRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ConverterViewModel(
    private val repository: RateRepository,
    private val remoteDataSource: ExchangeRateRemoteDataSource,
    private val baseCode: String = DEFAULT_BASE_CODE
) : ViewModel() {
    private val updateMutex = Mutex()
    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    private var lastManualRefreshMillis = 0L

    val updateStatus: StateFlow<UpdateStatus> = _updateStatus

    val rates: StateFlow<List<ExchangeRateEntity>> = repository
        .observeRates(baseCode)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val updateRecord: StateFlow<UpdateRecordEntity?> = repository
        .observeUpdateRecord(baseCode)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            refreshIfNeeded()
        }
    }

    fun refreshManually() {
        val now = System.currentTimeMillis()
        if (now - lastManualRefreshMillis < MANUAL_REFRESH_COOLDOWN_MILLIS) {
            _updateStatus.value = UpdateStatus.Failed("操作过于频繁，请稍后再试。")
            return
        }
        lastManualRefreshMillis = now
        viewModelScope.launch {
            refresh(force = true)
        }
    }

    private suspend fun refreshIfNeeded() {
        updateMutex.withLock {
            val record = repository.getUpdateRecord(baseCode)
            val localRates = repository.getRates(baseCode)
            val today = LocalDate.now().toString()

            if (record?.lastSuccessfulUpdateDate == today && localRates.isNotEmpty()) {
                _updateStatus.value = UpdateStatus.UsingLocal("今日已更新，正在使用本地汇率。")
                return
            }

            if (localRates.isNotEmpty()) {
                _updateStatus.value = UpdateStatus.UsingLocal(
                    "本地汇率日期为 ${record?.lastSuccessfulUpdateDate ?: "未知"}，正在获取最新数据。"
                )
            }
            refreshLocked()
        }
    }

    private suspend fun refresh(force: Boolean) {
        updateMutex.withLock {
            if (!force) {
                val record = repository.getUpdateRecord(baseCode)
                if (record?.lastSuccessfulUpdateDate == LocalDate.now().toString()) return
            }
            refreshLocked()
        }
    }

    private suspend fun refreshLocked() {
        _updateStatus.value = UpdateStatus.Loading
        try {
            val response = remoteDataSource.fetchLatestRates(baseCode)
            repository.saveSuccessfulResponse(response)
            _updateStatus.value = UpdateStatus.Success("汇率更新成功，已保存到本地。")
        } catch (exception: Exception) {
            val localRates = repository.getRates(baseCode)
            _updateStatus.value = if (localRates.isNotEmpty()) {
                UpdateStatus.Failed("更新失败，已保留并继续使用本地汇率：${exception.message ?: "未知错误"}")
            } else {
                UpdateStatus.NoLocalData("暂无本地汇率，请连接网络后重试：${exception.message ?: "未知错误"}")
            }
        }
    }

    companion object {
        const val DEFAULT_BASE_CODE = "JPY"
        private const val MANUAL_REFRESH_COOLDOWN_MILLIS = 5_000L

        class Factory(
            private val application: CurrencyConverterApplication
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ConverterViewModel::class.java)) {
                    return ConverterViewModel(
                        repository = application.rateRepository,
                        remoteDataSource = application.rateRemoteDataSource
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
