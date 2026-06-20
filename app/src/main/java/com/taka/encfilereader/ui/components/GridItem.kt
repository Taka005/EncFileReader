package com.taka.encfilereader.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridItem(
    imageData: ByteArray?,
    title: String,
    fileCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ){
        Column{
            AsyncImage(
                model = imageData,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f/5f),
                contentScale = ContentScale.Crop,
                error = ColorPainter(MaterialTheme.colorScheme.surface),
                placeholder = ColorPainter(MaterialTheme.colorScheme.surface),
            )

            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 0.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis

            )

            Text(
                text = "${fileCount}ファイル",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 4.dp),
                textAlign = TextAlign.Right,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}