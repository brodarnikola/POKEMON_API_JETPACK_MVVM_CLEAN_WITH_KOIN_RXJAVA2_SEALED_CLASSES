package com.vjezba.data.di

import com.nikola_brodar.data.networking.PokemonRepositoryApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://pokeapi.co/api/v2/"


val networkingModule = module {

    //it creates a singleton that can be used across the app as a singular instance.
    // it has all the time the same value, and it point to the same reference in memory
    single { GsonConverterFactory.create() as Converter.Factory }
    single { HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY) as Interceptor }
    single {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()
    }

    single<Retrofit>(named("retrofitPokemon")) {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get())
            .addConverterFactory(get())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }
    single { get<Retrofit>(named("retrofitPokemon")).create(PokemonRepositoryApi::class.java) }
}
