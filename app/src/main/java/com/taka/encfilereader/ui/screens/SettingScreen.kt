package com.taka.encfilereader.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.taka.encfilereader.R
import com.taka.encfilereader.ui.components.ColumnSelectDropdown
import com.taka.encfilereader.ui.components.MaxRequestDropDown
import com.taka.encfilereader.ui.views.SettingViewModel
import com.taka.encfilereader.util.formatBytes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: SettingViewModel,
    onReset: () -> Unit,
){
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("ストレージ設定", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            ListItem(
                headlineContent = { Text("コンテンツキャッシュ") },
                supportingContent = {
                    Column {
                        Text("メモリ: ${uiState.contentMemoryCacheSize.formatBytes()}/${uiState.maxContentMemoryCache.formatBytes()}")
                        Text("ディスク: ${uiState.contentDiskCacheSize.formatBytes()}/${uiState.maxContentDiskCache.formatBytes()}")
                    }
                },
                trailingContent = {
                    Button(
                        onClick = {
                            viewModel.clearContentCache()
                        }
                    ) {
                        Text("クリア")
                    }
                }
            )

            ListItem(
                headlineContent = { Text("マニフェストキャッシュ") },
                supportingContent = {
                    Text("ディスク: ${uiState.manifestDiskCacheSize.formatBytes()}/${uiState.maxManifestDiskCache.formatBytes()}")
                },
                trailingContent = {
                    Button(
                        onClick = {
                            viewModel.clearManifestCache()
                        }
                    ) {
                        Text("クリア")
                    }
                }
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            Text("表示設定", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            ListItem(
                headlineContent = { Text("カラム表示数") },
                trailingContent = {
                    ColumnSelectDropdown(
                        currentValue = uiState.displayColumns,
                        onValueSelected = { viewModel.setColumns(it) }
                    )
                }
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            Text("ネットワーク設定", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            ListItem(
                headlineContent = { Text("最大接続数") },
                trailingContent = {
                    MaxRequestDropDown(
                        currentValue = uiState.maxRequests,
                        onValueSelected = { viewModel.setMaxRequests(it) }
                    )
                }
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            Text("その他の設定", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            ListItem(
                headlineContent = { Text("初期化") },
                supportingContent = {
                    Text("全ての設定を削除します")
                },
                trailingContent = {
                    Button(
                        onClick = {
                            viewModel.clearContentCache()
                            viewModel.clearManifestCache()
                            onReset()
                        }
                    ) {
                        Text("リセット")
                    }
                }
            )
        }
    }
}