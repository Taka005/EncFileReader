package com.taka.encfilereader.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.taka.encfilereader.ui.views.FileListViewModel
import com.taka.encfilereader.ui.views.LoadViewModel
import com.taka.encfilereader.ui.views.ManifestListViewModel
import com.taka.encfilereader.ui.views.ReaderViewModel
import com.taka.encfilereader.ui.views.SetupViewModel

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "setup",
        modifier = modifier
    ) {
        composable("setup") {
            val viewModel: SetupViewModel = hiltViewModel()

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
            val viewModel: LoadViewModel = hiltViewModel()

            LoadScreen(
                viewModel,
                onFinish = {
                    navController.navigate("manifestList") {
                        popUpTo("load") { inclusive = true }
                    }
                }
            )
        }
        composable("manifestList") {
            val viewModel: ManifestListViewModel = hiltViewModel()

            ManifestListScreen(viewModel,columns = 2,navController)
        }
        composable(
            "fileList/{index}",
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: 0

            val viewModel: FileListViewModel = hiltViewModel()

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

            val viewModel: ReaderViewModel = hiltViewModel()

            ReaderScreen(viewModel,manifestIndex,fileIndex)
        }
    }
}