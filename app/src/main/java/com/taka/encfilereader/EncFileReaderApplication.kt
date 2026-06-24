package com.taka.encfilereader

import android.app.Application
import com.taka.encfilereader.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class EncFileReaderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@EncFileReaderApplication)
            modules(appModule)
        }
    }
}