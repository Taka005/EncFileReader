package com.taka.encfilereader.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.taka.encfilereader.ui.states.UiState
import com.taka.encfilereader.ui.views.FileListViewModel
import com.taka.encfilereader.ui.views.LoadViewModel
import com.taka.encfilereader.ui.views.ManifestListViewModel
import com.taka.encfilereader.ui.views.ReaderViewModel
import com.taka.encfilereader.ui.views.SetupViewModel
import com.taka.encfilereader.ui.views.StartViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "start",
        modifier = modifier
    ) {
        composable("start") {
            val viewModel: StartViewModel = koinViewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(state) {
                if(state is UiState.Success){
                    navController.navigate("load") {
                        popUpTo("start") { inclusive = true }
                    }
                }else if(state is UiState.Error){
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
        composable("manifestList") {
            val viewModel: ManifestListViewModel = koinViewModel()

            ManifestListScreen(viewModel,columns = 2,navController)
        }
        composable(
            "fileList/{index}",
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: 0

            val viewModel: FileListViewModel = koinViewModel()

            FileListScreen(viewModel,2,navController,index)
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

            ReaderScreen(viewModel,manifestIndex,fileIndex)
        }
    }
}