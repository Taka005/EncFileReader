package com.taka.encfilereader.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import com.taka.encfilereader.R
import com.taka.encfilereader.ui.components.HistoryItem
import com.taka.encfilereader.ui.components.ManifestItem
import com.taka.encfilereader.ui.views.HistoryViewModel
import com.taka.encfilereader.ui.views.ManifestListViewModel
import com.taka.encfilereader.util.SortType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManifestListScreen(
    viewModel: ManifestListViewModel,
    columns: Int,
    historyViewModel: HistoryViewModel,
    onNavigate: (route: String, navOptions: NavOptions?)-> Unit
){
    val items by viewModel.uiState.collectAsState(initial = emptyList())
    var isShowMenu by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortType by viewModel.sortType.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val histories by historyViewModel.histories.collectAsState()

    BackHandler(isSearching) {
        isSearching = !isSearching
        viewModel.updateSearchQuery("")
    }

    LaunchedEffect(Unit) {
        viewModel.updateSearchQuery("")
        viewModel.loadManifestList()
        historyViewModel.loadHistories()
    }

    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) historyViewModel.loadHistories()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (isSearching) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { query ->
                                viewModel.updateSearchQuery(query)
                            },
                            placeholder = {
                                Text(
                                    text = "検索...",
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onPrimary
                            ),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Text(stringResource(R.string.app_name))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                if (drawerState.isOpen) {
                                    drawerState.close()
                                } else {
                                    drawerState.open()
                                }
                            }
                        }
                    ){
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearching = !isSearching

                        if (!isSearching) {
                            viewModel.updateSearchQuery("")
                        }
                    }) {
                        Icon(
                            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = null
                        )
                    }

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
                            if(histories.count() != 0) {
                                DropdownMenuItem(
                                    text = { Text("最新の履歴") },
                                    onClick = {
                                        isShowMenu = false

                                        val latestHistory = histories.last()

                                        onNavigate("manifestList",
                                            navOptions {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        )

                                        onNavigate("fileList/${latestHistory.manifestIndex}", null)

                                        onNavigate("reader/${latestHistory.manifestIndex}/${latestHistory.fileIndex}",null)
                                    }
                                )

                                HorizontalDivider()
                            }

                            DropdownMenuItem(
                                text = { Text(if (sortType == SortType.NONE) "✓ ソートなし" else "ソートなし") },
                                onClick = {
                                    viewModel.updateSortType(SortType.NONE)
                                    isShowMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (sortType == SortType.NAME_ASC) "✓ 名前順 (昇順)" else "名前順 (昇順)") },
                                onClick = {
                                    viewModel.updateSortType(SortType.NAME_ASC)
                                    isShowMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (sortType == SortType.NAME_DESC) "✓ 名前順 (降順)" else "名前順 (降順)") },
                                onClick = {
                                    viewModel.updateSortType(SortType.NAME_DESC)
                                    isShowMenu = false
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("設定") },
                                onClick = {
                                    isShowMenu = false

                                    onNavigate("setting",null)
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.7f)
                        .padding(innerPadding),
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ){
                    Text(
                        text = "履歴",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )

                    HorizontalDivider()

                    if (histories.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
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
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(2.dp)
                        ){
                            items(histories) { item ->
                                HistoryItem(
                                    item,
                                    {
                                        coroutineScope.launch { drawerState.close() }

                                        onNavigate("manifestList",
                                            navOptions {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        )

                                        onNavigate("fileList/${item.manifestIndex}", null)

                                        onNavigate("reader/${item.manifestIndex}/${item.fileIndex}",null)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        ) {
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
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
                        ManifestItem(
                            item,
                            onClick = {
                                onNavigate("fileList/${item.manifestIndex}",null)
                            }
                        )
                    }
                }
            }
        }
    }
}