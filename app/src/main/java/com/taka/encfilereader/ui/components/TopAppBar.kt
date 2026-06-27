package com.taka.encfilereader.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    navController: NavController,
    currentRoute: String?,
    title: String
){
    var isShowMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if(currentRoute == "setting"){
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ){
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = null
                    )
                }
            }else if(
                currentRoute != "setup"&&
                currentRoute != "load"
            ){
                IconButton(
                    onClick = {

                    }
                ){
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            if(
                currentRoute != "setup"&&
                currentRoute != "load"&&
                currentRoute != "setting"
            ) {
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

                                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                    navController.navigate("setting")
                                }
                            }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}