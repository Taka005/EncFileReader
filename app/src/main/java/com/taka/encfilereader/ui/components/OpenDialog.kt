package com.taka.encfilereader.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.taka.encfilereader.ui.states.FileUiState
import kotlin.math.round

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
        title = {
            Text(
                text = uiState?.fileName ?: "不明",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                SubcomposeAsyncImage(
                    model = uiState?.imageData,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    contentScale = ContentScale.Fit,
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                        }
                    },
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                        }
                    },
                )

                Spacer(modifier = Modifier.height(10.dp))

                val positionHistory = uiState?.positionHistory ?: 0
                val contentCount = uiState?.contentCount ?: 0

                Text(
                    text = "${positionHistory + 1}/${contentCount} ${round((positionHistory.toFloat()/contentCount.toFloat())*100).toInt()}％",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 0.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
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