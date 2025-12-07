package com.example.inventory.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.ItemWithStock
import com.example.inventory.repository.InventoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val repository: InventoryRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val importProgress: StateFlow<Int> = repository.importProgress

    private val _searchResult = MutableStateFlow<ItemWithStock?>(null)
    val searchResult: StateFlow<ItemWithStock?> = _searchResult.asStateFlow()

    fun import(url: String) {
        viewModelScope.launch {
            repository.downloadAndImport(url)
        }
    }

    fun search(code: String) {
        viewModelScope.launch(ioDispatcher) {
            _searchResult.value = repository.searchByCode(code)
        }
    }
}
