package com.taka.encfilereader.ui.states

data class SettingUiState(
    var defaultMemoryCache: Int = 0,
    var defaultDiskCache: Int = 0,
    var memoryCacheSize: Int = 0,
    var diskCacheSize: Int = 0,
    var displayColumns: Int = 0
)