package com.taka.encfilereader.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.taka.encfilereader.ui.ImageGridScreen

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
            ImageGridScreen(columns = 2)
        }
    }
}