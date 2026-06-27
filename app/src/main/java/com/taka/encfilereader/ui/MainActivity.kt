package com.taka.encfilereader.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import androidx.compose.material3.ExperimentalMaterial3Api
import com.taka.encfilereader.ui.screens.MainScreen
import com.taka.encfilereader.ui.theme.EncFileReaderTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EncFileReaderTheme {
                MainScreen(modifier = Modifier)
            }
        }
    }
}