package com.example.currencyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.currencyconverter.ui.screen.ConverterScreen
import com.example.currencyconverter.ui.theme.CurrencyConverterTheme
import com.example.currencyconverter.viewmodel.ConverterViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CurrencyConverterTheme {
                val converterViewModel: ConverterViewModel = viewModel(
                    factory = ConverterViewModel.Factory(application as CurrencyConverterApplication)
                )
                ConverterScreen(converterViewModel)
            }
        }
    }
}
