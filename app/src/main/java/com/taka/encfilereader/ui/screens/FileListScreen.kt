package com.taka.encfilereader.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.taka.encfilereader.ui.components.FileItem
import com.taka.encfilereader.ui.components.OpenDialog
import com.taka.encfilereader.ui.states.FileUiState
import com.taka.encfilereader.ui.views.FileListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListScreen(
    viewModel: FileListViewModel,
    columns: Int,
    navController: NavController,
    index: Int
){
    val items by viewModel.uiState.collectAsState()
    val progress by viewModel.progressUiState.collectAsState()
    val title by viewModel.title.collectAsState()
    var isShowMenu by remember { mutableStateOf(false) }
    var selectedFileState by remember { mutableStateOf<FileUiState?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadFileList(index)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title ?: "") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(
                        onClick = {

                        }
                    ){
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                        IconButton(onClick = { isShowMenu = !isShowMenu }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = null
                            )
                        }

                        DropdownMenu(
                            expanded = isShowMenu,
                            onDismissRequest = { isShowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("設定") },
                                onClick = {
                                    isShowMenu = false

                                    navController.navigate("setting")
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${progress.current} / ${progress.total}件をロード済み",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(3.dp)
            ) {
                items(items) { item ->
                    FileItem(
                        item,
                        onClick = {
                            coroutineScope.launch {
                                if(item.positionHistory != null){
                                    val imageData = viewModel.getContentData(index,item.fileIndex,item.positionHistory)

                                    selectedFileState = item.copy(
                                        imageData = imageData
                                    )
                                }else{
                                    navController.navigate("reader/${index}/${items.indexOf(item)}")
                                }
                            }
                        }
                    )
                }
            }

            if (selectedFileState != null) {
                OpenDialog(
                    uiState = selectedFileState,
                    onContinue = {
                        selectedFileState?.let { data ->
                            selectedFileState = null
                            navController.navigate("reader/${index}/${data.fileIndex}")
                        }
                    },
                    onBegin = {
                        selectedFileState?.let { data ->
                            selectedFileState = null
                            coroutineScope.launch {
                                viewModel.resetHistory(index, data.fileIndex)
                            }
                            navController.navigate("reader/${index}/${data.fileIndex}")
                        }
                    },
                    onCancel = {
                        selectedFileState = null
                    }
                )
            }
        }
    }
}