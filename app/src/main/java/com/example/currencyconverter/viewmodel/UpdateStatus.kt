package com.example.currencyconverter.viewmodel

sealed interface UpdateStatus {
    data object Idle : UpdateStatus
    data object Loading : UpdateStatus
    data class Success(val message: String) : UpdateStatus
    data class UsingLocal(val message: String) : UpdateStatus
    data class Failed(val message: String) : UpdateStatus
    data class NoLocalData(val message: String) : UpdateStatus
}
