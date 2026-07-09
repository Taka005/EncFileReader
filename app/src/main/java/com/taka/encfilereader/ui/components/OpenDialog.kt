package com.taka.encfilereader.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenDialog(
    fileName: String,
    onContinue: () -> Unit,
    onBegin: () -> Unit,
    onCancel: () -> Unit
){
    AlertDialog(
        onDismissRequest = {
            onCancel()
        },
        title = { Text(fileName) },
        confirmButton = {
            TextButton(onClick = {
                onContinue()
            }) { Text("続きから") }
        },
        dismissButton = {
            TextButton(onClick = {
                onBegin()
            }) { Text("最初から") }
        }
    )
}