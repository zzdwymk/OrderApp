package com.zzdwymk.order.data

import android.content.Context
import com.zzdwymk.order.model.Food
import com.zzdwymk.order.model.Shop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object ShopRepository {

    private const val BASE_URL = "http://192.168.194.77:8080/order"

    private var cachedShops: List<Shop>? = null

    suspend fun getShops(context: Context, source: DataSource = DataSource.LOCAL): List<Shop> {
        return when (source) {
            DataSource.LOCAL -> loadFromLocal(context)
            DataSource.NETWORK -> loadFromNetwork()
        }
    }

    private suspend fun loadFromLocal(context: Context): List<Shop> {
        cachedShops?.let { return it }
        return withContext(Dispatchers.IO) {
            val json = context.assets.open("shop_data.json").bufferedReader().use { it.readText() }
            val result = parseJson(json)
            cachedShops = result
            result
        }
    }

    private suspend fun loadFromNetwork(): List<Shop> = withContext(Dispatchers.IO) {
        val url = URL("$BASE_URL/shop_list_data.json")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 8_000
            connection.readTimeout = 8_000
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("服务器返回状态码: $responseCode")
            }

            val json = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { reader ->
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                sb.toString()
            }

            val result = parseJson(json)
            cachedShops = result
            result
        } finally {
            connection.disconnect()
        }
    }

    fun clearCache() {
        cachedShops = null
    }

    private fun parseJson(json: String): List<Shop> {
        val jsonArray = JSONArray(json)
        val shops = mutableListOf<Shop>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            shops.add(parseShop(obj))
        }
        return shops
    }

    private fun parseShop(obj: JSONObject): Shop {
        val foodArray = obj.getJSONArray("foodList")
        val foods = mutableListOf<Food>()
        for (i in 0 until foodArray.length()) {
            val foodObj = foodArray.getJSONObject(i)
            foods.add(
                Food(
                    foodId = foodObj.getString("foodId"),
                    foodName = foodObj.getString("foodName"),
                    taste = foodObj.getString("taste"),
                    saleNum = foodObj.getInt("saleNum"),
                    price = foodObj.getDouble("price"),
                    count = foodObj.getInt("count"),
                    foodPic = foodObj.getString("foodPic")
                )
            )
        }
        return Shop(
            id = obj.getInt("id"),
            shopName = obj.getString("shopName"),
            saleNum = obj.getInt("saleNum"),
            offerPrice = obj.getInt("offerPrice"),
            distributionCost = obj.getInt("distributionCost"),
            welfare = obj.getString("welfare"),
            time = obj.getString("time"),
            shopPic = obj.getString("shopPic"),
            shopNotice = obj.getString("shopNotice"),
            foodList = foods
        )
    }
}

enum class DataSource { LOCAL, NETWORK }
