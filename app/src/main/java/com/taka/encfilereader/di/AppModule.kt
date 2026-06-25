package com.taka.encfilereader.di

import org.koin.core.module.dsl.viewModel
import com.taka.encfilereader.manager.StorageManager
import com.taka.encfilereader.ui.views.FileListViewModel
import com.taka.encfilereader.ui.views.LoadViewModel
import com.taka.encfilereader.ui.views.ManifestListViewModel
import com.taka.encfilereader.ui.views.ReaderViewModel
import com.taka.encfilereader.ui.views.SetupViewModel
import com.taka.encfilereader.ui.views.StartViewModel
import org.koin.dsl.module


val appModule = module {
    single { StorageManager(get()) }

    viewModel { StartViewModel(get()) }
    viewModel { SetupViewModel(get()) }
    viewModel { LoadViewModel(get()) }
    viewModel { ManifestListViewModel(get()) }
    viewModel { FileListViewModel(get()) }
    viewModel { ReaderViewModel(get()) }
}