package com.zzdwymk.order.model

data class Address(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val detailAddress: String = "",
    val fullAddress: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isDefault: Boolean = false
)

data class Coupon(
    val id: String = "",
    val name: String = "",
    val discountAmount: Double = 0.0,
    val minSpend: Double = 0.0,
    val expireDate: String = "",
    val isUsed: Boolean = false,
    val isSelected: Boolean = false
)

data class GiftItem(
    val foodId: String = "",
    val foodName: String = "",
    val foodPic: String = "",
    val count: Int = 1,
    val originalPrice: Double = 0.0
)

data class OrderConfirmData(
    val shopId: Int = 0,
    val shopName: String = "",
    val shopPic: String = "",
    val items: List<CartItem> = emptyList(),
    val giftItems: List<GiftItem> = emptyList(),
    val address: Address? = null,
    val selectedCoupon: Coupon? = null,
    val availableCoupons: List<Coupon> = emptyList(),
    val deliveryFee: Double = 0.0,
    val packagingFee: Double = 0.0,
    val remark: String = ""
) {
    val subtotal: Double get() = items.sumOf { it.price * it.count }
    val couponDiscount: Double get() = selectedCoupon?.discountAmount ?: 0.0
    val giftValue: Double get() = giftItems.sumOf { it.originalPrice * it.count }
    val totalAmount: Double get() = (subtotal - couponDiscount + deliveryFee + packagingFee).coerceAtLeast(0.0)
}

data class CartItem(
    val foodId: String = "",
    val foodName: String = "",
    val foodPic: String = "",
    val price: Double = 0.0,
    val count: Int = 1,
    val taste: String = ""
)
