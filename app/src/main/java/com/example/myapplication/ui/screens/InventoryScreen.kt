package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.InventoryRepository
import com.example.myapplication.data.Product
import com.example.myapplication.util.formatMoney

@Composable
fun InventoryScreen(modifier: Modifier = Modifier) {
    var searchText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    val filteredProducts = InventoryRepository.products.filter {
        it.name.contains(searchText.trim(), ignoreCase = true) ||
            it.category.contains(searchText.trim(), ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "商品库存",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("添加商品")
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索商品名称或分类") },
            singleLine = true,
            trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredProducts, key = { it.id }) { product ->
                ProductCard(product = product, onClick = { editingProduct = product })
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    if (showAddDialog) {
        ProductEditDialog(initial = null, onDismiss = { showAddDialog = false })
    }
    editingProduct?.let { product ->
        ProductEditDialog(initial = product, onDismiss = { editingProduct = null })
    }
}

@Composable
private fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (product.category.isNotBlank()) {
                    Text(
                        text = product.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "进价 ${formatMoney(product.purchasePrice)} · 售价 ${formatMoney(product.salePrice)}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (product.isLowStock) {
                        "库存 ${product.stock} ${product.unit}（偏低）"
                    } else {
                        "库存 ${product.stock} ${product.unit}"
                    },
                    fontSize = 13.sp,
                    color = if (product.isLowStock) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}

@Composable
private fun ProductEditDialog(initial: Product?, onDismiss: () -> Unit) {
    var name by remember(initial) { mutableStateOf(initial?.name ?: "") }
    var category by remember(initial) { mutableStateOf(initial?.category ?: "") }
    var unit by remember(initial) { mutableStateOf(initial?.unit ?: "件") }
    var purchasePrice by remember(initial) { mutableStateOf(initial?.purchasePrice?.toString() ?: "") }
    var salePrice by remember(initial) { mutableStateOf(initial?.salePrice?.toString() ?: "") }
    var stock by remember(initial) { mutableStateOf(initial?.stock?.toString() ?: "") }
    var minStock by remember(initial) { mutableStateOf(initial?.minStock?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "添加商品" else "编辑商品") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("商品名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("分类") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("单位（件/瓶/袋）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchasePrice,
                        onValueChange = { purchasePrice = it },
                        label = { Text("进价（元）") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = salePrice,
                        onValueChange = { salePrice = it },
                        label = { Text("售价（元）") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text(if (initial == null) "初始库存" else "当前库存") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minStock,
                        onValueChange = { minStock = it },
                        label = { Text("预警值") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val pp = purchasePrice.toDoubleOrNull()
                    val sp = salePrice.toDoubleOrNull()
                    val st = stock.toIntOrNull()
                    val ms = minStock.toIntOrNull() ?: 0
                    if (name.isBlank() || pp == null || sp == null || st == null || st < 0) {
                        return@TextButton
                    }
                    if (initial == null) {
                        InventoryRepository.addProduct(name, category, unit, pp, sp, st, ms)
                    } else {
                        InventoryRepository.updateProduct(
                            initial.copy(
                                name = name.trim(),
                                category = category.trim(),
                                unit = unit.trim().ifBlank { "件" },
                                purchasePrice = pp,
                                salePrice = sp,
                                stock = st,
                                minStock = ms
                            )
                        )
                    }
                    onDismiss()
                }
            ) { Text("保存") }
        },
        dismissButton = {
            if (initial != null) {
                TextButton(
                    onClick = {
                        InventoryRepository.deleteProduct(initial.id)
                        onDismiss()
                    }
                ) { Text("删除", color = MaterialTheme.colorScheme.error) }
            } else {
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )
}
