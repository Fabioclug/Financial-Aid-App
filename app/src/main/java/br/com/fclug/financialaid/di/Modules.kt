package br.com.fclug.financialaid.di

import br.com.fclug.financialaid.SessionManager
import br.com.fclug.financialaid.server.ServerApi
import org.koin.dsl.module.module

val networkModule = module {
    single { ServerApi() }
}

val preferencesModule = module {
    single { SessionManager(get()) }
}