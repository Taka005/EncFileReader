package com.taka.encfilereader.ui.states

data class TopBarState(
    val title: String = "",
    val showNavigationIcon: Boolean = false,
    val showMenuIcon: Boolean = false,
    val onNavigationClick: () -> Unit = {}
)