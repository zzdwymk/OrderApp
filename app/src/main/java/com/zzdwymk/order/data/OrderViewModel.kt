package com.zzdwymk.order.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.zzdwymk.order.model.OrderConfirmData
import com.zzdwymk.order.model.Address
import com.zzdwymk.order.model.CartItem

data class Order(
    val id: String,
    val shopId: Int,
    val shopName: String,
    val shopPic: String,
    val items: List<CartItem>,
    val address: Address?,
    val totalAmount: Double,
    val status: OrderStatus,
    val createTime: Long = System.currentTimeMillis(),
    val remark: String = ""
)


enum class OrderStatus(val displayName: String) {
    PENDING_PAYMENT("待支付"),
    DELIVERING("配送中"),
    COMPLETED("已完成"),
    PENDING_REVIEW("待评价")
}

class OrderViewModel : ViewModel() {

    var orders by mutableStateOf<List<Order>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var currentOrderId by mutableStateOf<String?>(null)
        private set

    fun addOrder(orderData: OrderConfirmData, remark: String = "") {
        val newOrder = Order(
            id = generateOrderId(),
            shopId = orderData.shopId,
            shopName = orderData.shopName,
            shopPic = orderData.shopPic,
            items = orderData.items,
            address = orderData.address,
            totalAmount = orderData.totalAmount,
            status = OrderStatus.PENDING_PAYMENT,
            remark = remark
        )
        orders = listOf(newOrder) + orders
        currentOrderId = newOrder.id
    }

    fun payOrder(orderId: String) {
        orders = orders.map { order ->
            if (order.id == orderId) {
                order.copy(status = OrderStatus.DELIVERING)
            } else {
                order
            }
        }
    }

    fun cancelOrder(orderId: String) {
        orders = orders.filter { it.id != orderId }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        orders = orders.map { order ->
            if (order.id == orderId) {
                order.copy(status = newStatus)
            } else {
                order
            }
        }
    }

    fun removeOrder(orderId: String) {
        orders = orders.filter { it.id != orderId }
    }

    fun getOrderById(orderId: String): Order? {
        return orders.find { it.id == orderId }
    }

    fun getCurrentOrder(): Order? {
        return currentOrderId?.let { getOrderById(it) }
    }

    private fun generateOrderId(): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(10)
        val random = (1000..9999).random()
        return "OD$timestamp$random"
    }
}