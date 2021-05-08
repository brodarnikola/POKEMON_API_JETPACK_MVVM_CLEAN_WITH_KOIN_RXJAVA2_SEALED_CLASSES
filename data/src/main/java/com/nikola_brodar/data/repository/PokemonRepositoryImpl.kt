package com.nikola_brodar.data.repository

import com.nikola_brodar.data.database.PokemonDatabase
import com.nikola_brodar.data.database.mapper.DbMapper
import com.nikola_brodar.data.networking.PokemonRepositoryApi
import com.nikola_brodar.domain.ResultState
import com.nikola_brodar.domain.model.*
import com.nikola_brodar.domain.repository.PokemonRepository
import com.vjezba.data.lego.api.BaseDataSource
import java.util.*

import io.reactivex.Observable
/**
 * RepositoryResponseApi module for handling data operations.
 */

class PokemonRepositoryImpl constructor(
    private val database: PokemonDatabase,
    private val service: PokemonRepositoryApi,
    private val dbMapper: DbMapper?
) : PokemonRepository, BaseDataSource() {

    override fun getAllPokemons(limit: Int, offset: Int): Observable<ResultState<AllPokemons>> {
        val result = service.getAllPokemons(limit, offset)
        val correctResult = result.map { dbMapper?.mapAllPokemonToDomainAllPokemon(it)!! }
        return correctResult
    }

    override fun getRandomSelectedPokemon(id: Int): Observable<ResultState<MainPokemon>> { // MainPokemon {
        val result =  service.getRandomSelectedPokemon(id)
        val correctResult = result.map { dbMapper?.mapApiPokemonToDomainPokemon(it)!! }
        return correctResult
    }

}
