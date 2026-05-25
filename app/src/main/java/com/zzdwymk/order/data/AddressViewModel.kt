package com.zzdwymk.order.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.zzdwymk.order.model.Address

class AddressViewModel : ViewModel() {

    var addresses by mutableStateOf<List<Address>>(emptyList())
        private set

    var defaultAddress by mutableStateOf<Address?>(null)
        private set

    init {
        // Initialize with some default addresses for demo
        addresses = listOf(
            Address(
                id = "addr_1",
                name = "张三",
                phone = "138****8888",
                detailAddress = "北京市朝阳区建国路88号SOHO现代城A座1205室",
                fullAddress = "北京市朝阳区建国路88号",
                latitude = 39.908,
                longitude = 116.408,
                isDefault = true
            ),
            Address(
                id = "addr_2",
                name = "李四",
                phone = "139****6666",
                detailAddress = "北京市海淀区中关村大街1号海龙大厦808室",
                fullAddress = "北京市海淀区中关村大街1号",
                latitude = 39.984,
                longitude = 116.312,
                isDefault = false
            )
        )
        defaultAddress = addresses.find { it.isDefault }
    }

    fun addAddress(address: Address) {
        val newAddress = if (address.isDefault) {
            // If setting as default, remove default from others
            addresses = addresses.map { it.copy(isDefault = false) }
            address.copy(id = generateAddressId())
        } else {
            address.copy(id = generateAddressId())
        }
        addresses = listOf(newAddress) + addresses
        if (newAddress.isDefault) {
            defaultAddress = newAddress
        }
    }

    fun updateAddress(address: Address) {
        addresses = addresses.map {
            if (it.id == address.id) address
            else if (address.isDefault && it.isDefault) it.copy(isDefault = false)
            else it
        }
        if (address.isDefault) {
            defaultAddress = address
        } else {
            defaultAddress = addresses.find { it.isDefault }
        }
    }

    fun deleteAddress(addressId: String) {
        val addressToDelete = addresses.find { it.id == addressId }
        addresses = addresses.filter { it.id != addressId }
        if (addressToDelete?.isDefault == true) {
            defaultAddress = addresses.firstOrNull()?.copy(isDefault = true)
            addresses = addresses.map {
                if (it.id == defaultAddress?.id) it.copy(isDefault = true) else it
            }
        }
    }

    fun setDefaultAddress(addressId: String) {
        addresses = addresses.map { address ->
            if (address.id == addressId) {
                address.copy(isDefault = true)
            } else if (address.isDefault) {
                address.copy(isDefault = false)
            } else {
                address
            }
        }
        defaultAddress = addresses.find { it.id == addressId }
    }

    fun getAddressById(addressId: String): Address? {
        return addresses.find { it.id == addressId }
    }

    private fun generateAddressId(): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(8)
        val random = (1000..9999).random()
        return "addr_$timestamp$random"
    }
}
