package com.example.myapplication.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.InventoryRepository
import com.example.myapplication.data.Product
import com.example.myapplication.util.formatMoney

@Composable
fun SalesScreen(modifier: Modifier = Modifier) {
    var searchText by remember { mutableStateOf("") }
    var showCartDialog by remember { mutableStateOf(false) }
    val cart = remember { mutableStateMapOf<String, Int>() }
    val context = LocalContext.current

    val products = InventoryRepository.products.filter {
        it.name.contains(searchText.trim(), ignoreCase = true) ||
            it.category.contains(searchText.trim(), ignoreCase = true)
    }
    val cartCount = cart.values.sum()
    val cartTotal = cart.entries.sumOf { (productId, quantity) ->
        (InventoryRepository.productById(productId)?.salePrice ?: 0.0) * quantity
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "收银结算",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (cartCount > 0) {
                Text(
                    text = "购物车 $cartCount 件",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
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
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(products, key = { it.id }) { product ->
                SaleProductCard(
                    product = product,
                    cartQuantity = cart[product.id] ?: 0,
                    onAdd = {
                        val current = cart[product.id] ?: 0
                        if (current < product.stock) {
                            cart[product.id] = current + 1
                        }
                    }
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
        if (cart.isNotEmpty()) {
            Surface(shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "合计：${formatMoney(cartTotal)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "共 $cartCount 件商品",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Button(onClick = { showCartDialog = true }) {
                        Text("去结账")
                    }
                }
            }
        }
    }

    if (showCartDialog) {
        CartDialog(
            cart = cart.toMap(),
            onChange = { productId, newQuantity ->
                if (newQuantity <= 0) cart.remove(productId) else cart[productId] = newQuantity
            },
            onDismiss = { showCartDialog = false },
            onCheckout = {
                if (InventoryRepository.checkout(cart.toMap())) {
                    cart.clear()
                    showCartDialog = false
                    Toast.makeText(context, "结账成功，欢迎下次光临", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "结账失败，请检查商品库存", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
private fun SaleProductCard(product: Product, cartQuantity: Int, onAdd: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${formatMoney(product.salePrice)} / ${product.unit}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "库存 ${product.stock} ${product.unit}",
                    fontSize = 12.sp,
                    color = if (product.stock <= 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            }
            if (cartQuantity > 0) {
                Text(
                    text = "已选 $cartQuantity",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            OutlinedButton(onClick = onAdd, enabled = product.stock > 0) {
                Text(if (product.stock > 0) "加入" else "缺货")
            }
        }
    }
}

@Composable
private fun CartDialog(
    cart: Map<String, Int>,
    onChange: (String, Int) -> Unit,
    onDismiss: () -> Unit,
    onCheckout: () -> Unit
) {
    val items = cart.mapNotNull { (productId, quantity) ->
        InventoryRepository.productById(productId)?.let { product -> product to quantity }
    }
    val total = items.sumOf { (product, quantity) -> product.salePrice * quantity }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("购物车") },
        text = {
            Column {
                if (items.isEmpty()) {
                    Text(
                        text = "购物车为空",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        items.forEach { (product, quantity) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = product.name, fontSize = 15.sp)
                                    Text(
                                        text = "${formatMoney(product.salePrice)} / ${product.unit} · 库存 ${product.stock}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                IconButton(
                                    onClick = { onChange(product.id, quantity - 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text(text = "−", fontSize = 20.sp, textAlign = TextAlign.Center)
                                }
                                Text(
                                    text = "$quantity",
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(28.dp)
                                )
                                IconButton(
                                    onClick = {
                                        if (quantity < product.stock) {
                                            onChange(product.id, quantity + 1)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "增加")
                                }
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "合计：${formatMoney(total)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCheckout, enabled = items.isNotEmpty()) { Text("确认结账") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
