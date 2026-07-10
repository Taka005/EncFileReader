package com.taka.encfilereader.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.taka.encfilereader.ui.states.FileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenDialog(
    uiState: FileUiState?,
    onContinue: () -> Unit,
    onBegin: () -> Unit,
    onCancel: () -> Unit
){
    AlertDialog(
        onDismissRequest = {
            onCancel()
        },
        title = { Text(uiState?.fileName ?: "不明") },
        text = {
            SubcomposeAsyncImage(
                model = uiState?.imageData,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f/5f),
                contentScale = ContentScale.Crop,
                error = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                },
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                },
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onContinue()
            }) {
                Text(
                    text = "続きから",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onBegin()
            }) {
                Text(
                    text = "最初から",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    )
}