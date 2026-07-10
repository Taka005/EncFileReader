package com.taka.encfilereader.di

import org.koin.core.module.dsl.viewModel
import com.taka.encfilereader.manager.StorageManager
import com.taka.encfilereader.ui.views.FileListViewModel
import com.taka.encfilereader.ui.views.HistoryViewModel
import com.taka.encfilereader.ui.views.LoadViewModel
import com.taka.encfilereader.ui.views.ManifestListViewModel
import com.taka.encfilereader.ui.views.ReaderViewModel
import com.taka.encfilereader.ui.views.SettingViewModel
import com.taka.encfilereader.ui.views.SetupViewModel
import com.taka.encfilereader.ui.views.StartViewModel
import org.koin.dsl.module
import org.koin.dsl.onClose

val appModule = module {
    single {
        StorageManager(get())
    }onClose{ manager ->
        manager?.close()
    }

    viewModel { StartViewModel(get()) }
    viewModel { SetupViewModel(get()) }
    viewModel { LoadViewModel(get()) }
    viewModel { SettingViewModel(get()) }
    viewModel { ManifestListViewModel(get()) }
    viewModel { FileListViewModel(get()) }
    viewModel { ReaderViewModel(get()) }
    viewModel { HistoryViewModel(get()) }
}