package com.taka.encfilereader.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.taka.encfilereader.ui.components.GridItem
import com.taka.encfilereader.ui.views.localContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManifestListScreen(columns: Int){
    val viewModel = localContext.current
    val storage = viewModel.storage ?: return

    val items = remember { List(storage.manifestCount) { it } }

    val imageCache = remember { mutableStateMapOf<Int, ByteArray>() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(3.dp)
    ) {
        items(items) { index ->
            if (!imageCache.containsKey(index)) {
                LaunchedEffect(index) {
                    val result = storage.getContentData(index, 0, 0)

                    result.onSuccess { data ->
                        imageCache[index] = data
                    }
                }
            }

            GridItem(
                imageData = imageCache[index],
                title = "アイテム $index",
                fileCount = 1
            )
        }
    }
}