package com.taka.encfilereader.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.taka.encfilereader.ui.views.SettingViewModel
import com.taka.encfilereader.util.formatBytes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: SettingViewModel
){
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("ストレージ設定", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        ListItem(
            headlineContent = { Text("キャッシュサイズ") },
            supportingContent = {
                Column {
                    Text("メモリ: ${uiState.memoryCacheSize.formatBytes()}/${uiState.defaultMemoryCache.formatBytes()}")
                    Text("ディスク: ${uiState.diskCacheSize.formatBytes()}/${uiState.defaultDiskCache.formatBytes()}")
                }
            },
            trailingContent = {
                Button(
                    onClick = {
                        viewModel.clearCache()
                    }
                ){
                    Text("クリア")
                }
            }
        )

        HorizontalDivider()
    }
}