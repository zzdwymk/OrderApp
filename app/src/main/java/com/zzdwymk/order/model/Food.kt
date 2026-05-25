package com.zzdwymk.order.model

data class Food(
    val foodId: String,
    val foodName: String,
    val taste: String,
    val saleNum: Int,
    val price: Double,
    val count: Int,
    val foodPic: String
)
