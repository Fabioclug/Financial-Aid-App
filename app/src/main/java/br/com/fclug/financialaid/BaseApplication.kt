package br.com.fclug.financialaid

import android.app.Application
import br.com.fclug.financialaid.di.networkModule
import org.koin.android.ext.android.startKoin

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(networkModule))
    }
}