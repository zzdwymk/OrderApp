package com.zzdwymk.order.model

data class Shop(
    val id: Int,
    val shopName: String,
    val saleNum: Int,
    val offerPrice: Int,
    val distributionCost: Int,
    val welfare: String,
    val time: String,
    val shopPic: String,
    val shopNotice: String,
    val foodList: List<Food>
)
