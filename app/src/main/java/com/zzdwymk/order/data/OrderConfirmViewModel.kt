package com.zzdwymk.order.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.zzdwymk.order.model.*

class OrderConfirmViewModel : ViewModel() {

    var orderData by mutableStateOf(OrderConfirmData())
        private set

    var showAddressPicker by mutableStateOf(false)
        private set

    var showCouponPicker by mutableStateOf(false)
        private set

    var showRemarkDialog by mutableStateOf(false)
        private set

    var isSubmitting by mutableStateOf(false)
        private set

    var scheduledDeliveryTime by mutableStateOf("尽快送达")
        private set

    fun updateDeliveryTime(time: String) {
        scheduledDeliveryTime = time
    }

    fun toggleDeliveryTimePicker() {
        showDeliveryTimePicker = !showDeliveryTimePicker
    }

    var showDeliveryTimePicker by mutableStateOf(false)
        private set

    fun initOrder(shopId: Int, shopName: String, shopPic: String, cartItems: List<CartEntry>, deliveryFee: Double = 5.0, packagingFee: Double = 2.0, defaultAddress: Address? = null) {
        val items = cartItems.map { entry ->
            CartItem(
                foodId = entry.food.foodId,
                foodName = entry.food.foodName,
                foodPic = entry.food.foodPic,
                price = entry.food.price,
                count = entry.count,
                taste = entry.food.taste
            )
        }

        val mockGifts = listOf(
            GiftItem(
                foodId = "gift_1",
                foodName = "店铺招牌小菜",
                foodPic = "",
                count = 1,
                originalPrice = 12.0
            )
        )

        val mockCoupons = listOf(
            Coupon(
                id = "coupon_1",
                name = "新用户专享券",
                discountAmount = 10.0,
                minSpend = 30.0,
                expireDate = "2026-06-30"
            ),
            Coupon(
                id = "coupon_2",
                name = "满减优惠券",
                discountAmount = 5.0,
                minSpend = 50.0,
                expireDate = "2026-05-31"
            )
        )

        orderData = OrderConfirmData(
            shopId = shopId,
            shopName = shopName,
            shopPic = shopPic,
            items = items,
            giftItems = mockGifts,
            address = defaultAddress,
            availableCoupons = mockCoupons,
            deliveryFee = deliveryFee,
            packagingFee = packagingFee
        )
    }

    fun updateAddress(address: Address) {
        orderData = orderData.copy(address = address)
        showAddressPicker = false
    }

    fun selectCoupon(coupon: Coupon?) {
        orderData = orderData.copy(selectedCoupon = coupon)
        showCouponPicker = false
    }

    fun updateRemark(remark: String) {
        orderData = orderData.copy(remark = remark)
        showRemarkDialog = false
    }

    fun toggleAddressPicker() {
        showAddressPicker = !showAddressPicker
    }

    fun toggleCouponPicker() {
        showCouponPicker = !showCouponPicker
    }

    fun toggleRemarkDialog() {
        showRemarkDialog = !showRemarkDialog
    }

    suspend fun submitOrder(): Boolean {
        if (orderData.address == null) return false
        isSubmitting = true
        try {
            kotlinx.coroutines.delay(1500)
            return true
        } catch (e: Exception) {
            return false
        } finally {
            isSubmitting = false
        }
    }

}
