package com.taka.encfilereader.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.taka.encfilereader.ui.components.Content
import com.taka.encfilereader.ui.views.localContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentReaderScreen(
    manifestIndex: Int,
    fileIndex: Int
) {
    val viewModel = localContext.current
    val uiState by viewModel.readerUiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadReaderContent(manifestIndex, fileIndex)
    }

    val pages = listOfNotNull(uiState.before, uiState.now, uiState.after)
    val pagerState = rememberPagerState(pageCount = { pages.size })

    LaunchedEffect(uiState.position) {
        val targetPage = if (uiState.before != null) 1 else 0
        pagerState.scrollToPage(targetPage)
    }

    LaunchedEffect(pagerState.currentPage) {
        val move = when (pagerState.currentPage) {
            0 -> if (uiState.before != null) -1 else 0
            2 -> if (uiState.after != null) 1 else 0
            else -> 0
        }

        if (move != 0) {
            val nextPos = uiState.position + move
            if (nextPos >= 0) {
                viewModel.loadReaderContent(manifestIndex, fileIndex, nextPos)
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { pageIndex ->
        val imageBytes = pages.getOrNull(pageIndex)
        if (imageBytes != null) {
            Content(imageBytes)
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        }
    }
}