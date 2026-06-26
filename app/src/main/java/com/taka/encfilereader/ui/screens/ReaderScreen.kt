package com.taka.encfilereader.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.taka.encfilereader.ui.components.Content
import com.taka.encfilereader.ui.views.ReaderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    manifestIndex: Int,
    fileIndex: Int
){
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadContent(manifestIndex, fileIndex, 0)
    }

    val pages = listOfNotNull(uiState.before, uiState.now, uiState.after)
    val pagerState = rememberPagerState(pageCount = { pages.size })

    val targetPage = if (uiState.before != null) 1 else 0

    LaunchedEffect(uiState.position) {
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != targetPage) {
            val nextPos = if (pagerState.currentPage > targetPage) {
                uiState.position + 1
            } else {
                uiState.position - 1
            }

            if (nextPos >= 0) {
                viewModel.loadContent(manifestIndex, fileIndex, nextPos)
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { pageIndex ->
                uiState.position + (pageIndex - if (uiState.before != null) 1 else 0)
            }
        ) { pageIndex ->
            val imageBytes = pages.getOrNull(pageIndex)
            if (imageBytes != null) {
                Content(imageBytes)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}