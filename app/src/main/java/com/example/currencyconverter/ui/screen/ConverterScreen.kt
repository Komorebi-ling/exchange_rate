package com.example.currencyconverter.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.currencyconverter.data.local.ExchangeRateEntity
import com.example.currencyconverter.domain.CurrencyCalculator
import com.example.currencyconverter.domain.CurrencyCatalog
import com.example.currencyconverter.domain.CurrencyInfo
import com.example.currencyconverter.viewmodel.ConverterViewModel
import com.example.currencyconverter.viewmodel.UpdateStatus
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun ConverterScreen(viewModel: ConverterViewModel) {
    val rates by viewModel.rates.collectAsStateWithLifecycle()
    val updateRecord by viewModel.updateRecord.collectAsStateWithLifecycle()
    val updateStatus by viewModel.updateStatus.collectAsStateWithLifecycle()
    val rateMap = remember(rates) { rates.associate { it.currencyCode to it.rate } }
    val currencyCodes = remember(rates) { rates.map(ExchangeRateEntity::currencyCode) }

    var amountText by remember { mutableStateOf("") }
    var sourceCurrency by remember { mutableStateOf(ConverterViewModel.DEFAULT_BASE_CODE) }
    var targetCurrency by remember { mutableStateOf("CNY") }
    var pickerTarget by remember { mutableStateOf<PickerTarget?>(null) }

    LaunchedEffect(currencyCodes) {
        if (sourceCurrency !in currencyCodes && currencyCodes.isNotEmpty()) sourceCurrency = currencyCodes.first()
        if (targetCurrency !in currencyCodes && currencyCodes.isNotEmpty()) {
            targetCurrency = currencyCodes.firstOrNull { it != sourceCurrency } ?: currencyCodes.first()
        }
    }

    val result = remember(amountText, sourceCurrency, targetCurrency, rateMap) {
        CurrencyCalculator.calculate(amountText, sourceCurrency, targetCurrency, rateMap)
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("汇率换算", modifier = Modifier.padding(top = 20.dp), style = MaterialTheme.typography.headlineMedium)
                Text("离线使用本地汇率，输入金额即可换算", modifier = Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodyMedium)
            }
            item {
                Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("原币种", style = MaterialTheme.typography.labelLarge)
                        CurrencySelector(sourceCurrency, CurrencyCatalog.fromCodes(currencyCodes)) { pickerTarget = PickerTarget.Source }
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { newValue ->
                                if (newValue.length <= 18 && newValue.matches(Regex("^\\d*(\\.\\d*)?$"))) amountText = newValue
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            label = { Text("金额") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = {
                                if (amountText.isNotEmpty()) IconButton(onClick = { amountText = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "清空金额")
                                }
                            }
                        )
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    IconButton(onClick = {
                        val oldSource = sourceCurrency
                        sourceCurrency = targetCurrency
                        targetCurrency = oldSource
                    }) { Icon(Icons.Default.SwapVert, contentDescription = "交换币种") }
                }
            }
            item {
                Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("目标币种", style = MaterialTheme.typography.labelLarge)
                        CurrencySelector(targetCurrency, CurrencyCatalog.fromCodes(currencyCodes)) { pickerTarget = PickerTarget.Target }
                        Text(CurrencyCalculator.format(result, targetCurrency), modifier = Modifier.padding(top = 18.dp), style = MaterialTheme.typography.displaySmall)
                    }
                }
            }
            item { RateInfo(sourceCurrency, targetCurrency, rateMap, updateRecord?.dataDate) }
            item { UpdateStatusPanel(updateStatus, viewModel::refreshManually) }
        }
    }

    pickerTarget?.let { target ->
        CurrencyPickerDialog(
            currencies = CurrencyCatalog.fromCodes(currencyCodes),
            onDismiss = { pickerTarget = null },
            onSelected = { selected ->
                if (target == PickerTarget.Source) sourceCurrency = selected.code else targetCurrency = selected.code
                pickerTarget = null
            }
        )
    }
}

private enum class PickerTarget { Source, Target }

@Composable
private fun CurrencySelector(selectedCode: String, currencies: List<CurrencyInfo>, onClick: () -> Unit) {
    val selected = currencies.firstOrNull { it.code == selectedCode }
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(selected?.name ?: selectedCode, style = MaterialTheme.typography.titleMedium)
            Text(selectedCode, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun CurrencyPickerDialog(currencies: List<CurrencyInfo>, onDismiss: () -> Unit, onSelected: (CurrencyInfo) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filteredCurrencies = remember(query, currencies) { CurrencyCatalog.search(currencies.map { it.code }, query) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择币种") },
        text = {
            Column {
                OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), label = { Text("搜索代码或名称") }, singleLine = true)
                LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                    items(filteredCurrencies, key = { it.code }) { currency ->
                        TextButton(onClick = { onSelected(currency) }, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(currency.name)
                                Text(currency.code)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun RateInfo(sourceCurrency: String, targetCurrency: String, rates: Map<String, Double>, dataDate: String?) {
    val sourceRate = rates[sourceCurrency]
    val targetRate = rates[targetCurrency]
    val effectiveRate = if (sourceRate != null && targetRate != null && sourceRate > 0) {
        BigDecimal.valueOf(targetRate).divide(BigDecimal.valueOf(sourceRate), 8, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
    } else "—"
    Text("1 $sourceCurrency = $effectiveRate $targetCurrency\n数据日期：${dataDate ?: "暂无本地数据"}", style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun UpdateStatusPanel(status: UpdateStatus, onRefresh: () -> Unit) {
    val message = when (status) {
        UpdateStatus.Idle -> "正在准备本地汇率…"
        UpdateStatus.Loading -> "正在获取最新汇率…"
        is UpdateStatus.Success -> status.message
        is UpdateStatus.UsingLocal -> status.message
        is UpdateStatus.Failed -> status.message
        is UpdateStatus.NoLocalData -> status.message
    }
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (status == UpdateStatus.Loading) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
            Text(message, style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = onRefresh, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("手动更新汇率") }
        Text("数据来源：Exchange Rate API", modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.labelSmall)
    }
}
