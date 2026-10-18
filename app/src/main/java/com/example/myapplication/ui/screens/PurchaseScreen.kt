package com.example.myapplication.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.InventoryRepository
import com.example.myapplication.data.Product
import com.example.myapplication.data.PurchaseRecord
import com.example.myapplication.util.formatMoney
import com.example.myapplication.util.formatPrice
import com.example.myapplication.util.formatTime

@Composable
fun PurchaseScreen(modifier: Modifier = Modifier) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showPicker by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    val context = LocalContext.current

    val qty = quantity.toIntOrNull()
    val price = unitPrice.toDoubleOrNull()
    val canSubmit = selectedProduct != null && qty != null && qty > 0 && price != null && price >= 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = "商品入库", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { showPicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedProduct?.name ?: "点击选择商品")
        }
        selectedProduct?.let { product ->
            Text(
                text = "当前库存 ${product.stock} ${product.unit} · 默认进价 ${formatMoney(product.purchasePrice)}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("入库数量") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = unitPrice,
                onValueChange = { unitPrice = it },
                label = { Text("单价（元）") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = supplier,
            onValueChange = { supplier = it },
            label = { Text("供应商（选填）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (qty != null && qty > 0 && price != null) {
            Text(
                text = "预计金额：${formatMoney(price * qty)}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                val product = selectedProduct
                if (product != null && qty != null && price != null &&
                    InventoryRepository.purchase(product.id, qty, price, supplier)
                ) {
                    Toast.makeText(context, "入库成功：${product.name} ×$qty", Toast.LENGTH_SHORT).show()
                    selectedProduct = null
                    quantity = ""
                    unitPrice = ""
                    supplier = ""
                }
            },
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("确认入库")
        }
        Spacer(Modifier.height(20.dp))
        Text(text = "最近入库记录", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        val recentPurchases = InventoryRepository.purchases.take(20)
        if (recentPurchases.isEmpty()) {
            Text(
                text = "暂无入库记录",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            recentPurchases.forEach { record ->
                PurchaseRow(record)
            }
        }
        Spacer(Modifier.height(16.dp))
    }

    if (showPicker) {
        ProductPickerDialog(
            onDismiss = { showPicker = false },
            onSelect = { product ->
                selectedProduct = product
                if (unitPrice.isBlank()) {
                    unitPrice = formatPrice(product.purchasePrice)
                }
                showPicker = false
            }
        )
    }
}

@Composable
private fun PurchaseRow(record: PurchaseRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "${record.productName} ×${record.quantity}", fontSize = 15.sp)
            Text(
                text = listOf(record.supplier, formatTime(record.timestamp))
                    .filter { it.isNotBlank() }
                    .joinToString(" · "),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Text(
            text = formatMoney(record.total),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun ProductPickerDialog(onDismiss: () -> Unit, onSelect: (Product) -> Unit) {
    var searchText by remember { mutableStateOf("") }
    val filtered = InventoryRepository.products.filter {
        it.name.contains(searchText.trim(), ignoreCase = true)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择商品") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("搜索商品") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                if (filtered.isEmpty()) {
                    Text(
                        text = "未找到商品，可先到“商品”页添加",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(filtered, key = { it.id }) { product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(product) }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = product.name, fontSize = 15.sp)
                                    Text(
                                        text = "库存 ${product.stock} ${product.unit}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Text(text = formatMoney(product.purchasePrice), fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
