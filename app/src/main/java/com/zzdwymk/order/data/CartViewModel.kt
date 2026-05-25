package com.zzdwymk.order.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.zzdwymk.order.model.Food
import com.zzdwymk.order.model.Shop

data class CartEntry(
    val shopId: Int,
    val shopName: String,
    val shopPic: String,
    val food: Food,
    var count: Int
)

class CartViewModel : ViewModel() {

    var cartItems by mutableStateOf<List<CartEntry>>(emptyList())
        private set

    var showCartPopup by mutableStateOf(false)
        private set

    fun addFood(shop: Shop, food: Food) {
        val current = cartItems.toMutableList()
        val existing = current.find { it.shopId == shop.id && it.food.foodId == food.foodId }
        if (existing != null) {
            val idx = current.indexOf(existing)
            current[idx] = existing.copy(count = existing.count + 1)
        } else {
            current.add(CartEntry(shop.id, shop.shopName, shop.shopPic, food, 1))
        }
        cartItems = current
    }

    fun removeFood(shop: Shop, food: Food) {
        val current = cartItems.toMutableList()
        val existing = current.find { it.shopId == shop.id && it.food.foodId == food.foodId }
        if (existing != null) {
            if (existing.count <= 1) {
                current.remove(existing)
            } else {
                val idx = current.indexOf(existing)
                current[idx] = existing.copy(count = existing.count - 1)
            }
            cartItems = current
        }
    }

    fun getFoodCount(shopId: Int, foodId: String): Int {
        return cartItems.find { it.shopId == shopId && it.food.foodId == foodId }?.count ?: 0
    }

    fun getShopItems(shopId: Int): List<CartEntry> {
        return cartItems.filter { it.shopId == shopId }
    }

    fun getUniqueShopIds(): Set<Int> {
        return cartItems.map { it.shopId }.toSet()
    }

    fun getTotalFoodCount(shopId: Int): Int {
        return cartItems.filter { it.shopId == shopId }.sumOf { it.count }
    }

    fun getTotalPrice(shopId: Int): Double {
        return cartItems.filter { it.shopId == shopId }.sumOf { it.count * it.food.price }
    }

    fun clearAll() {
        cartItems = emptyList()
    }

    fun toggleCartPopup() {
        if (cartItems.isNotEmpty()) {
            showCartPopup = !showCartPopup
        }
    }

    fun hideCartPopup() {
        showCartPopup = false
    }
}
