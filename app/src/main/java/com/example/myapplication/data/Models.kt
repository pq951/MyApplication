package com.example.myapplication.data

import org.json.JSONArray
import org.json.JSONObject

/** 商品资料：进价、售价、当前库存与预警线 */
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val unit: String,
    val purchasePrice: Double,
    val salePrice: Double,
    val stock: Int,
    val minStock: Int
) {
    val isLowStock: Boolean get() = stock <= minStock

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("category", category)
        put("unit", unit)
        put("purchasePrice", purchasePrice)
        put("salePrice", salePrice)
        put("stock", stock)
        put("minStock", minStock)
    }

    companion object {
        fun fromJson(obj: JSONObject): Product = Product(
            id = obj.getString("id"),
            name = obj.getString("name"),
            category = obj.optString("category"),
            unit = obj.optString("unit", "件"),
            purchasePrice = obj.getDouble("purchasePrice"),
            salePrice = obj.getDouble("salePrice"),
            stock = obj.getInt("stock"),
            minStock = obj.optInt("minStock")
        )

        fun listFromJson(text: String?): List<Product> {
            if (text.isNullOrBlank()) return emptyList()
            return try {
                val array = JSONArray(text)
                List(array.length()) { index -> fromJson(array.getJSONObject(index)) }
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun toJsonList(list: List<Product>): String {
            val array = JSONArray()
            list.forEach { array.put(it.toJson()) }
            return array.toString()
        }
    }
}

/** 入库记录（采购） */
data class PurchaseRecord(
    val id: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val total: Double,
    val supplier: String,
    val timestamp: Long
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("productId", productId)
        put("productName", productName)
        put("quantity", quantity)
        put("unitPrice", unitPrice)
        put("total", total)
        put("supplier", supplier)
        put("timestamp", timestamp)
    }

    companion object {
        fun fromJson(obj: JSONObject): PurchaseRecord = PurchaseRecord(
            id = obj.getString("id"),
            productId = obj.getString("productId"),
            productName = obj.getString("productName"),
            quantity = obj.getInt("quantity"),
            unitPrice = obj.getDouble("unitPrice"),
            total = obj.getDouble("total"),
            supplier = obj.optString("supplier"),
            timestamp = obj.getLong("timestamp")
        )

        fun listFromJson(text: String?): List<PurchaseRecord> {
            if (text.isNullOrBlank()) return emptyList()
            return try {
                val array = JSONArray(text)
                List(array.length()) { index -> fromJson(array.getJSONObject(index)) }
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun toJsonList(list: List<PurchaseRecord>): String {
            val array = JSONArray()
            list.forEach { array.put(it.toJson()) }
            return array.toString()
        }
    }
}

/** 销售记录（含成本与利润，便于统计） */
data class SaleRecord(
    val id: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val costPrice: Double,
    val total: Double,
    val profit: Double,
    val timestamp: Long
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("productId", productId)
        put("productName", productName)
        put("quantity", quantity)
        put("unitPrice", unitPrice)
        put("costPrice", costPrice)
        put("total", total)
        put("profit", profit)
        put("timestamp", timestamp)
    }

    companion object {
        fun fromJson(obj: JSONObject): SaleRecord {
            val total = obj.getDouble("total")
            val costPrice = obj.optDouble("costPrice", 0.0)
            return SaleRecord(
                id = obj.getString("id"),
                productId = obj.getString("productId"),
                productName = obj.getString("productName"),
                quantity = obj.getInt("quantity"),
                unitPrice = obj.getDouble("unitPrice"),
                costPrice = costPrice,
                total = total,
                profit = obj.optDouble("profit", total - costPrice * obj.getInt("quantity")),
                timestamp = obj.getLong("timestamp")
            )
        }

        fun listFromJson(text: String?): List<SaleRecord> {
            if (text.isNullOrBlank()) return emptyList()
            return try {
                val array = JSONArray(text)
                List(array.length()) { index -> fromJson(array.getJSONObject(index)) }
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun toJsonList(list: List<SaleRecord>): String {
            val array = JSONArray()
            list.forEach { array.put(it.toJson()) }
            return array.toString()
        }
    }
}
