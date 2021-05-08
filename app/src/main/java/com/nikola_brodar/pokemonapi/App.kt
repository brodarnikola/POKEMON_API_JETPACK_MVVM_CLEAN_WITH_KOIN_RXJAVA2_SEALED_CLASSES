package com.nikola_brodar.pokemonapi

import android.app.Application
import com.nikola_brodar.pokemonapi.connectivity.network.ConnectivityChangedEvent
import com.nikola_brodar.pokemonapi.connectivity.network.ConnectivityMonitor
import com.nikola_brodar.pokemonapi.di.presentationModule
import com.vjezba.data.di.databaseModule
import com.vjezba.data.di.networkingModule
import com.vjezba.data.di.repositoryModule
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {

    init {
        ref = this
    }

    companion object {
        @JvmStatic
        lateinit var ref: App
    }

    //event bus initialization
    val eventBus: EventBus by lazy {
        EventBus.builder()
            .logNoSubscriberMessages(false)
            .sendNoSubscriberEvent(false)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        //instance = this

        val appModules = listOf(presentationModule)
        val dataModules = listOf( networkingModule, repositoryModule, databaseModule)

        startKoin {
            androidContext(this@App)
            if (BuildConfig.DEBUG) androidLogger(Level.ERROR)
            modules(appModules + dataModules )
        }

        ConnectivityMonitor.initialize(this) { available ->
            eventBus.post(
                ConnectivityChangedEvent(
                    available
                )
            )
        }

    }

}

