package com.taka.encfilereader.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "setting",
        modifier = modifier
    ) {
        composable("setting") {
            InitSettingsScreen(onFinish = {
                navController.navigate("loadManifestList") {
                    popUpTo("setting") { inclusive = true }
                }
            })
        }
        composable("loadManifestList") {
            LoadManifestScreen(onFinish = {
                navController.navigate("manifestList") {
                    popUpTo("loadManifestList") { inclusive = true }
                }
            })
        }
        composable("manifestList") {
            ManifestListScreen(columns = 2,navController)
        }
        composable(
            "fileList/{index}",
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: 0

            FileListScreen(2,navController,index)
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

            ContentReaderScreen(manifestIndex,fileIndex)
        }
    }
}