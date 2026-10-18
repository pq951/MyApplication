package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.InventoryRepository
import com.example.myapplication.util.formatMoney
import com.example.myapplication.util.formatTime

private data class RecordItem(
    val time: Long,
    val name: String,
    val quantity: Int,
    val amount: Double,
    val isSale: Boolean
)

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    var filter by remember { mutableIntStateOf(0) }

    val salesToday = InventoryRepository.salesToday()
    val purchasesToday = InventoryRepository.purchasesToday()
    val lowStock = InventoryRepository.lowStockProducts

    val records = InventoryRepository.sales.map {
        RecordItem(it.timestamp, it.productName, it.quantity, it.total, true)
    } + InventoryRepository.purchases.map {
        RecordItem(it.timestamp, it.productName, it.quantity, it.total, false)
    }
    val filteredRecords = when (filter) {
        1 -> records.filter { it.isSale }
        2 -> records.filter { !it.isSale }
        else -> records
    }.sortedByDescending { it.time }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "经营概览",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "今日销售额",
                    value = formatMoney(salesToday.sumOf { it.total }),
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    label = "今日利润",
                    value = formatMoney(salesToday.sumOf { it.profit }),
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "今日入库额",
                    value = formatMoney(purchasesToday.sumOf { it.total }),
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.secondary
                )
                StatCard(
                    label = "库存预警",
                    value = "${lowStock.size} 件",
                    modifier = Modifier.weight(1f),
                    valueColor = if (lowStock.isEmpty()) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }
        item {
            Text(
                text = "库存预警",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
            )
        }
        if (lowStock.isEmpty()) {
            item {
                Text(
                    text = "暂无库存预警商品",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            items(lowStock, key = { "low_" + it.id }) { product ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = product.name, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Text(
                        text = "库存 ${product.stock} ${product.unit} / 预警线 ${product.minStock}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        item {
            Text(
                text = "进出流水",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = filter == 0, onClick = { filter = 0 }, label = { Text("全部") })
                FilterChip(selected = filter == 1, onClick = { filter = 1 }, label = { Text("销售") })
                FilterChip(selected = filter == 2, onClick = { filter = 2 }, label = { Text("入库") })
            }
        }
        items(filteredRecords, key = { it.time.toString() + it.name + it.isSale }) { record ->
            RecordRow(record)
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(6.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
private fun RecordRow(record: RecordItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (record.isSale) "销售" else "入库",
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = if (record.isSale) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(text = record.name, fontSize = 15.sp)
            }
            Text(
                text = formatTime(record.time),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = (if (record.isSale) "+" else "-") + formatMoney(record.amount),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (record.isSale) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
            Text(
                text = "${record.quantity} 件",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
