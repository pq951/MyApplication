package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 进销存数据仓库：持有商品、入库、销售三类数据，
 * 使用 SharedPreferences + JSON 做本地持久化，无额外第三方依赖。
 */
object InventoryRepository {

    private const val PREFS_NAME = "inventory_data"
    private const val KEY_PRODUCTS = "products"
    private const val KEY_PURCHASES = "purchases"
    private const val KEY_SALES = "sales"

    private lateinit var prefs: SharedPreferences

    val products = mutableStateListOf<Product>()
    val purchases = mutableStateListOf<PurchaseRecord>()
    val sales = mutableStateListOf<SaleRecord>()

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        products.addAll(Product.listFromJson(prefs.getString(KEY_PRODUCTS, null)))
        purchases.addAll(PurchaseRecord.listFromJson(prefs.getString(KEY_PURCHASES, null)))
        sales.addAll(SaleRecord.listFromJson(prefs.getString(KEY_SALES, null)))
        if (products.isEmpty() && purchases.isEmpty() && sales.isEmpty()) {
            seedDemoData()
        }
    }

    fun productById(id: String): Product? = products.find { it.id == id }

    val lowStockProducts: List<Product>
        get() = products.filter { it.isLowStock }

    /** 新增商品，重名或名称为空时返回 false */
    fun addProduct(
        name: String,
        category: String,
        unit: String,
        purchasePrice: Double,
        salePrice: Double,
        stock: Int,
        minStock: Int
    ): Boolean {
        if (name.isBlank()) return false
        if (products.any { it.name == name.trim() }) return false
        products.add(
            Product(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                category = category.trim(),
                unit = unit.trim().ifBlank { "件" },
                purchasePrice = purchasePrice,
                salePrice = salePrice,
                stock = stock,
                minStock = minStock
            )
        )
        save()
        return true
    }

    fun updateProduct(product: Product) {
        val index = products.indexOfFirst { it.id == product.id }
        if (index >= 0) {
            products[index] = product
            save()
        }
    }

    fun deleteProduct(productId: String) {
        products.removeAll { it.id == productId }
        save()
    }

    /** 商品入库：增加库存并写入采购记录 */
    fun purchase(productId: String, quantity: Int, unitPrice: Double, supplier: String): Boolean {
        val index = products.indexOfFirst { it.id == productId }
        if (index < 0 || quantity <= 0 || unitPrice < 0) return false
        val product = products[index]
        products[index] = product.copy(stock = product.stock + quantity)
        purchases.add(
            0,
            PurchaseRecord(
                id = UUID.randomUUID().toString(),
                productId = productId,
                productName = product.name,
                quantity = quantity,
                unitPrice = unitPrice,
                total = unitPrice * quantity,
                supplier = supplier.trim(),
                timestamp = System.currentTimeMillis()
            )
        )
        save()
        return true
    }

    /** 收银结账：校验库存后扣减并写入销售记录 */
    fun checkout(cart: Map<String, Int>): Boolean {
        if (cart.isEmpty()) return false
        val items = cart.mapNotNull { (productId, quantity) ->
            productById(productId)?.let { product -> product to quantity }
        }
        if (items.isEmpty()) return false
        if (items.any { (_, quantity) -> quantity <= 0 }) return false
        if (items.any { (product, quantity) -> product.stock < quantity }) return false
        val now = System.currentTimeMillis()
        items.forEach { (product, quantity) ->
            val index = products.indexOfFirst { it.id == product.id }
            products[index] = product.copy(stock = product.stock - quantity)
            sales.add(
                0,
                SaleRecord(
                    id = UUID.randomUUID().toString(),
                    productId = product.id,
                    productName = product.name,
                    quantity = quantity,
                    unitPrice = product.salePrice,
                    costPrice = product.purchasePrice,
                    total = product.salePrice * quantity,
                    profit = (product.salePrice - product.purchasePrice) * quantity,
                    timestamp = now
                )
            )
        }
        save()
        return true
    }

    fun salesToday(): List<SaleRecord> = sales.filter { isToday(it.timestamp) }

    fun purchasesToday(): List<PurchaseRecord> = purchases.filter { isToday(it.timestamp) }

    private fun isToday(timestamp: Long): Boolean {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        return format.format(Date(timestamp)) == format.format(Date())
    }

    private fun save() {
        prefs.edit()
            .putString(KEY_PRODUCTS, Product.toJsonList(products))
            .putString(KEY_PURCHASES, PurchaseRecord.toJsonList(purchases))
            .putString(KEY_SALES, SaleRecord.toJsonList(sales))
            .apply()
    }

    /** 首次启动写入示例商品与流水，方便直接体验 */
    private fun seedDemoData() {
        val now = System.currentTimeMillis()
        val demoProducts = listOf(
            Product(UUID.randomUUID().toString(), "可口可乐 500ml", "饮料", "瓶", 2.2, 3.5, 120, 30),
            Product(UUID.randomUUID().toString(), "农夫山泉 550ml", "饮料", "瓶", 0.9, 2.0, 200, 50),
            Product(UUID.randomUUID().toString(), "乐事薯片 原味", "零食", "袋", 3.8, 6.5, 45, 15),
            Product(UUID.randomUUID().toString(), "康师傅红烧牛肉面", "方便食品", "桶", 2.8, 4.5, 60, 20),
            Product(UUID.randomUUID().toString(), "卫龙辣条 大面筋", "零食", "包", 1.5, 3.0, 8, 10),
            Product(UUID.randomUUID().toString(), "蓝月亮洗衣液 1kg", "日用百货", "瓶", 12.0, 19.9, 25, 8),
            Product(UUID.randomUUID().toString(), "心相印抽纸 3层", "日用百货", "包", 4.2, 7.5, 70, 20),
            Product(UUID.randomUUID().toString(), "正大鲜鸡蛋 10枚", "生鲜食品", "盒", 9.5, 15.8, 18, 10)
        )
        products.addAll(demoProducts)
        val cola = demoProducts[0]
        val water = demoProducts[1]
        val chips = demoProducts[2]
        purchases.add(
            PurchaseRecord(
                UUID.randomUUID().toString(), cola.id, cola.name, 100, cola.purchasePrice,
                cola.purchasePrice * 100, "中粮可口可乐经销商", now - 20 * 3_600_000L
            )
        )
        purchases.add(
            PurchaseRecord(
                UUID.randomUUID().toString(), water.id, water.name, 200, water.purchasePrice,
                water.purchasePrice * 200, "农夫山泉办事处", now - 5 * 3_600_000L
            )
        )
        sales.add(
            SaleRecord(
                UUID.randomUUID().toString(), chips.id, chips.name, 2, chips.salePrice, chips.purchasePrice,
                chips.salePrice * 2, (chips.salePrice - chips.purchasePrice) * 2, now - 3 * 3_600_000L
            )
        )
        sales.add(
            SaleRecord(
                UUID.randomUUID().toString(), water.id, water.name, 3, water.salePrice, water.purchasePrice,
                water.salePrice * 3, (water.salePrice - water.purchasePrice) * 3, now - 2 * 3_600_000L
            )
        )
        sales.add(
            SaleRecord(
                UUID.randomUUID().toString(), cola.id, cola.name, 4, cola.salePrice, cola.purchasePrice,
                cola.salePrice * 4, (cola.salePrice - cola.purchasePrice) * 4, now - 3_600_000L
            )
        )
        save()
    }
}
