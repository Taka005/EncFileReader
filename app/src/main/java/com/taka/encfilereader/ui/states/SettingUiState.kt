package com.taka.encfilereader.ui.states

data class SettingUiState(
    var maxContentMemoryCache: Int = 0,
    var maxContentDiskCache: Int = 0,
    var maxManifestDiskCache: Int = 0,
    var contentMemoryCacheSize: Int = 0,
    var contentDiskCacheSize: Int = 0,
    var manifestDiskCacheSize: Int = 0,
    var displayColumns: Int = 0,
    var maxRequests: Int = 0
)