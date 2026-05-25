package com.zzdwymk.order.data

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zzdwymk.order.model.Shop
import kotlinx.coroutines.launch

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    var shops by mutableStateOf<List<Shop>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadShopsWithFallback()
    }

    private fun loadShopsWithFallback() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                shops = ShopRepository.getShops(getApplication(), DataSource.NETWORK)
            } catch (e: Exception) {
                try {
                    shops = ShopRepository.getShops(getApplication(), DataSource.LOCAL)
                    errorMessage = "网络不可用，已加载本地缓存"
                } catch (localError: Exception) {
                    errorMessage = localError.message ?: "加载失败"
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun loadShops(source: DataSource = DataSource.LOCAL) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                shops = ShopRepository.getShops(getApplication(), source)
            } catch (e: Exception) {
                errorMessage = e.message ?: "加载失败"
                if (shops.isEmpty()) {
                    ShopRepository.clearCache()
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun refreshFromNetwork() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                ShopRepository.clearCache()
                shops = ShopRepository.getShops(getApplication(), DataSource.NETWORK)
            } catch (e: Exception) {
                if (shops.isEmpty()) {
                    try {
                        shops = ShopRepository.getShops(getApplication(), DataSource.LOCAL)
                        errorMessage = "刷新失败，显示本地数据"
                    } catch (localError: Exception) {
                        errorMessage = localError.message ?: "加载失败"
                    }
                } else {
                    errorMessage = "刷新失败：${e.message}"
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}
