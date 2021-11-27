package kg.bakai.nsd.di

import android.content.Context.MODE_PRIVATE
import kg.bakai.nsd.MainViewModel
import kg.bakai.nsd.repository.MainRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val vmModule = module {
    viewModel { MainViewModel(get()) }
}

val repoModule = module {
    single { MainRepository(get()) }
}

val appModule = module {
}