package com.taka.encfilereader.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taka.encfilereader.manager.StorageManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class ReaderViewModel(
    private val manager: StorageManager
) : ViewModel() {
    private val _loadImages = MutableStateFlow<Map<Int, ByteArray>>(emptyMap())
    val loadImages = _loadImages.asStateFlow()
    private val _title: MutableStateFlow<String?> = MutableStateFlow(null)
    val title = _title.asStateFlow()
    private val _position = MutableStateFlow(0)
    val position = _position.asStateFlow()
    private val _pageCount = MutableStateFlow(0)
    val pageCount = _pageCount.asStateFlow()

    private val loadingJobs = mutableMapOf<Int, Job>()

    fun initialize(manifestIndex: Int, fileIndex: Int) {
        val currentStorage = manager.storage ?: return
        val manifest = currentStorage.getManifest(manifestIndex).getOrNull() ?: return
        val file = manifest.getFileMetaData(fileIndex).getOrNull() ?: return

        _pageCount.value = file.contentCount
        _title.value = file.originalFileName

        loadPage(manifestIndex, fileIndex, 0)
    }

    fun setPosition(newPosition: Int){
        _position.value = newPosition
    }

    fun loadPage(manifestIndex: Int,fileIndex: Int, contentIndex: Int){
        if (contentIndex < 0 || contentIndex >= _pageCount.value) return
        if (_loadImages.value.containsKey(contentIndex)) return

        loadingJobs[contentIndex] = viewModelScope.launch {
            manager.getContentData(manifestIndex, fileIndex, contentIndex, false).getOrNull()?.let { data ->
                _loadImages.value += (contentIndex to data)

                if (_loadImages.value.size > 50) {
                    val keyToRemove = _loadImages.value.keys.minByOrNull { abs(it - contentIndex) }
                    if (keyToRemove != null) _loadImages.value -= keyToRemove
                }
            }

            loadingJobs.remove(contentIndex)
        }
    }
}