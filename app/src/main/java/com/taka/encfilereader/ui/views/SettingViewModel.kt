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
        //TODO: maxになるように調整　contentとmanifestを分ける
        _uiState.value = SettingUiState(
            defaultDiskCache = manager.contentCacheService.maxDiskCache,
            defaultMemoryCache = manager.contentCacheService.maxMemoryCache,
            diskCacheSize = manager.contentCacheService.diskCacheSize,
            memoryCacheSize = manager.contentCacheService.memoryCacheSize,
            displayColumns = manager.displayColumns,
            maxRequests = manager.maxRequests
        )
    }

     fun setColumns(columns: Int){
        viewModelScope.launch {
            manager.updateDisplayColumns(columns)
        }

        loadData()
    }

    fun setMaxRequests(request: Int){
        viewModelScope.launch {
            manager.updateMaxRequests(request)
        }

        loadData()
    }

    fun clearCache(){
        manager.contentCacheService.clearAll()
        loadData()
    }
}