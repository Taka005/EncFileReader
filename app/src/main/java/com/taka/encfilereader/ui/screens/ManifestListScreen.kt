package com.taka.encfilereader.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.taka.encfilereader.ui.components.ManifestItem
import com.taka.encfilereader.ui.views.localContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManifestListScreen(
    columns: Int,
    navController: NavController
){
    val viewModel = localContext.current
    val items by viewModel.manifestUiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadManifestList()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(3.dp)
    ) {
        items(items) { item ->
            ManifestItem(
                item,
                onClick = {
                    navController.navigate("fileList/${items.indexOf(item)}")
                }
            )
        }
    }
}