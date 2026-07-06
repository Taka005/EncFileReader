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
            maxContentDiskCache = manager.contentCacheService.maxDiskCache,
            maxContentMemoryCache = manager.contentCacheService.maxMemoryCache,
            maxManifestDiskCache = manager.manifestCacheService.maxDiskCache,
            contentDiskCacheSize = manager.contentCacheService.diskCacheSize,
            contentMemoryCacheSize = manager.contentCacheService.memoryCacheSize,
            manifestDiskCacheSize = manager.manifestCacheService.diskCacheSize,
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

    fun clearContentCache(){
        manager.contentCacheService.clearAll()
        loadData()
    }

    fun clearManifestCache(){
        manager.manifestCacheService.clearAll()
        loadData()
    }
}