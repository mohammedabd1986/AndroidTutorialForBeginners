package com.example.inventory.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.entity.Item
import com.example.inventory.data.entity.WarehouseStock
import com.example.inventory.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _searchResult = MutableLiveData<Item?>()
    val searchResult: LiveData<Item?> = _searchResult

    private val _stocks = MutableLiveData<List<WarehouseStock>>()
    val stocks: LiveData<List<WarehouseStock>> = _stocks

    private val _importStatus = MutableLiveData<String>()
    val importStatus: LiveData<String> = _importStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun searchItem(code: String) {
        viewModelScope.launch {
            repository.getItemByCode(code).collect { item ->
                _searchResult.value = item
                if (item == null) {
                    _error.value = "Item not found"
                } else {
                    _error.value = null
                }
            }
        }
    }

    fun loadStocks(itemId: Long) {
        viewModelScope.launch {
            repository.getStocksByItemId(itemId).collect { list ->
                _stocks.value = list
            }
        }
    }

    fun importData(url: String) {
        if (url.isBlank()) {
            _error.value = "URL is empty"
            return
        }
        _isLoading.value = true
        _importStatus.value = "Starting..."
        viewModelScope.launch {
            try {
                repository.downloadAndProcessExcel(url) { status ->
                    _importStatus.postValue(status)
                }
                _importStatus.value = "Done"
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _importStatus.value = "Failed"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
