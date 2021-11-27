package kg.bakai.nsd

import android.app.Application
import kg.bakai.nsd.di.appModule
import kg.bakai.nsd.di.repoModule
import kg.bakai.nsd.di.vmModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(vmModule, repoModule, appModule)
        }
    }
}