package com.paging

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber


/**
 * Created by Amit Singh on 28/03/20.
 * Tila
 * asingh@tila.com
 */

class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        startKoin {
            androidLogger()
            androidContext(this@AppApplication)
            modules(listOf(module))
        }
    }
}