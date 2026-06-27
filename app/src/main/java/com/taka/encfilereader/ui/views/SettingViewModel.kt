package com.taka.encfilereader.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taka.encfilereader.manager.StorageManager
import com.taka.encfilereader.ui.states.SettingUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingViewModel(
    private val manager: StorageManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<SettingUiState>(SettingUiState())
    val uiState = _uiState.asStateFlow()

    fun loadData(){
        _uiState.value = SettingUiState(
            defaultDiskCache = manager.cacheService.defaultDiskCache,
            defaultMemoryCache = manager.cacheService.defaultMemoryCache,
            diskCacheSize = manager.cacheService.diskCacheSize,
            memoryCacheSize = manager.cacheService.memoryCacheSize,
            displayColumns = manager.displayColumns
        )
    }

     fun setColumns(columns: Int){
        viewModelScope.launch {
            manager.updateDisplayColumns(columns)
        }

        loadData()
    }

    fun clearCache(){
        manager.cacheService.clearAll()
        loadData()
    }
}