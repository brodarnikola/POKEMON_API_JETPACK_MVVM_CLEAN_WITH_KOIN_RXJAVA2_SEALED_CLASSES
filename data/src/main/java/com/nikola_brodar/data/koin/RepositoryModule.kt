package com.vjezba.data.di

import com.nikola_brodar.data.database.mapper.DbMapper
import com.nikola_brodar.data.database.mapper.DbMapperImpl
import com.nikola_brodar.data.repository.PokemonRepositoryImpl
import com.nikola_brodar.domain.repository.PokemonRepository
import org.koin.dsl.module

val repositoryModule = module {

  factory<DbMapper> { DbMapperImpl() }
  factory<PokemonRepository> { PokemonRepositoryImpl(get(), get(), get()) }
}