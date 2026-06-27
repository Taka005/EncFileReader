package com.taka.encfilereader.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.taka.encfilereader.R
import com.taka.encfilereader.ui.components.TopAppBar
import com.taka.encfilereader.ui.states.StartUiState
import com.taka.encfilereader.ui.views.FileListViewModel
import com.taka.encfilereader.ui.views.LoadViewModel
import com.taka.encfilereader.ui.views.ManifestListViewModel
import com.taka.encfilereader.ui.views.ReaderViewModel
import com.taka.encfilereader.ui.views.SettingViewModel
import com.taka.encfilereader.ui.views.SetupViewModel
import com.taka.encfilereader.ui.views.StartViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val defaultTitle = stringResource(id = R.string.app_name)

    var screenTitle: String? by remember { mutableStateOf(null) }

    LaunchedEffect(currentRoute) {
        if (
            currentRoute == "start" ||
            currentRoute == "setup" ||
            currentRoute == "load"  ||
            currentRoute == "manifestList"
        ) {
            screenTitle = defaultTitle
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navController,
                currentRoute,
                screenTitle ?: ""
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "start",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("start") {
                val viewModel: StartViewModel = koinViewModel()
                val state by viewModel.uiState.collectAsState()

                LaunchedEffect(state) {
                    if (state is StartUiState.Success) {
                        navController.navigate("load") {
                            popUpTo("start") { inclusive = true }
                        }
                    } else if (state is StartUiState.Error) {
                        navController.navigate("setup") {
                            popUpTo("start") { inclusive = true }
                        }
                    }
                }
            }
            composable("setup") {
                val viewModel: SetupViewModel = koinViewModel()

                SetupScreen(
                    viewModel,
                    onFinish = {
                        navController.navigate("load") {
                            popUpTo("setup") { inclusive = true }
                        }
                    }
                )
            }
            composable("load") {
                val viewModel: LoadViewModel = koinViewModel()

                LoadScreen(
                    viewModel,
                    onFinish = {
                        navController.navigate("manifestList") {
                            popUpTo("load") { inclusive = true }
                        }
                    },
                    onError = {
                        navController.navigate("setup") {
                            popUpTo("load") { inclusive = true }
                        }
                    }
                )
            }
            composable("setting") {
                val viewModel: SettingViewModel = koinViewModel()

                SettingScreen(
                    viewModel,
                    onFinish = {
                        navController.popBackStack()
                    }
                )
            }
            composable("manifestList") {
                val viewModel: ManifestListViewModel = koinViewModel()

                ManifestListScreen(viewModel, columns = 2, navController)
            }
            composable(
                "fileList/{index}",
                arguments = listOf(navArgument("index") { type = NavType.IntType })
            ) { backStackEntry ->
                val index = backStackEntry.arguments?.getInt("index") ?: 0

                val viewModel: FileListViewModel = koinViewModel()
                val title by viewModel.title.collectAsState()

                LaunchedEffect(title) {
                    screenTitle = title
                }

                FileListScreen(viewModel, 2, navController, index)
            }
            composable(
                "reader/{manifestIndex}/{fileIndex}",
                arguments = listOf(
                    navArgument("manifestIndex") { type = NavType.IntType },
                    navArgument("fileIndex") { type = NavType.IntType },
                )
            ) { backStackEntry ->
                val manifestIndex = backStackEntry.arguments?.getInt("manifestIndex") ?: 0
                val fileIndex = backStackEntry.arguments?.getInt("fileIndex") ?: 0

                val viewModel: ReaderViewModel = koinViewModel()
                val title by viewModel.title.collectAsState()

                LaunchedEffect(title) {
                    screenTitle = title
                }

                ReaderScreen(viewModel, manifestIndex, fileIndex)
            }
        }
    }
}