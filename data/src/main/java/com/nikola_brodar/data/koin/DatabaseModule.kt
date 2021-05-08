package com.vjezba.data.di

import com.nikola_brodar.data.database.PokemonDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val databaseModule = module {
  
  single { PokemonDatabase.getInstance(androidContext()) }

  factory { get<PokemonDatabase>().pokemonDAO() }
}