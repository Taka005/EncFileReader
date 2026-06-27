package com.taka.encfilereader.ui.views

import androidx.lifecycle.ViewModel
import com.taka.encfilereader.manager.StorageManager
import com.taka.encfilereader.ui.states.SettingUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingViewModel(
    private val manager: StorageManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<SettingUiState>(SettingUiState())
    val uiState = _uiState.asStateFlow()

    fun loadData(){
        _uiState.value = SettingUiState(
            diskCacheSize = manager.cacheService.diskCacheSize,
            memoryCacheSize = manager.cacheService.memoryCacheSize
        )
    }

    fun clearCache(){
        manager.cacheService.clearAll()
        loadData()
    }
}