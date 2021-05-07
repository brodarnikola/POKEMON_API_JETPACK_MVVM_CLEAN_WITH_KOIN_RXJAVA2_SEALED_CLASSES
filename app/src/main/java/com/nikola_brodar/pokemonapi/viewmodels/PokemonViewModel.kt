/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nikola_brodar.pokemonapi.viewmodels

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikola_brodar.data.database.PokemonDatabase
import com.nikola_brodar.data.database.mapper.DbMapper
import com.nikola_brodar.data.database.model.DBMainPokemon
import com.nikola_brodar.data.di_dagger2.PokemonNetwork
import com.nikola_brodar.domain.ResultState
import com.nikola_brodar.domain.model.AllPokemons
import com.nikola_brodar.domain.model.MainPokemon
import com.nikola_brodar.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.Exception
import javax.inject.Inject


@HiltViewModel
class PokemonViewModel @Inject constructor(
    @PokemonNetwork private val pokemonRepository: PokemonRepository,
    private val dbPokemon: PokemonDatabase,
    private val dbMapper: DbMapper?
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _pokemonMutableLiveData: MutableLiveData<ResultState<*>> = MutableLiveData()

    val mainPokemonData: LiveData<ResultState<*>> = _pokemonMutableLiveData

    private val _pokemonMovesMutableLiveData: MutableLiveData<ResultState<*>> = MutableLiveData()

    val pokemonMovesData: LiveData<ResultState<*>> = _pokemonMovesMutableLiveData

    fun getPokemonMovesFromLocalStorage() {
        viewModelScope.launch {
            val loading = ResultState.Loading
            _pokemonMovesMutableLiveData.value = loading
            val listPokemonMove = getPokemonMovesFromDB()
            _pokemonMovesMutableLiveData.value = listPokemonMove
        }
    }

    private suspend fun getPokemonMovesFromDB(): ResultState<*> {

        val pokemonsMovesList = dbPokemon.pokemonDAO().getSelectedMovesPokemonData()
        if( pokemonsMovesList.isNotEmpty() )
            return ResultState.Success(pokemonsMovesList)
        return ResultState.Error("Something went wrong when reading data from database", null)
    }

    fun getAllPokemonDataFromLocalStorage() {
        viewModelScope.launch {
            val mainPokemonData = getAllPokemonDataFromRoom()
            val successPokemonData = ResultState.Success(mainPokemonData)
            _pokemonMutableLiveData.value = successPokemonData
        }
    }

    private suspend fun getAllPokemonDataFromRoom(): MainPokemon {
        val pokemonMain = dbPokemon.pokemonDAO().getSelectedMainPokemonData()
        val pokemonStats = dbPokemon.pokemonDAO().getSelectedStatsPokemonData()
        val pokemonMoves = dbPokemon.pokemonDAO().getSelectedMovesPokemonData()

        val correctPokemonMain = MainPokemon()
        correctPokemonMain.name = pokemonMain.name
        correctPokemonMain.sprites.backDefault = pokemonMain.backDefault
        correctPokemonMain.sprites.frontDefault = pokemonMain.frontDefault

        correctPokemonMain.stats = dbMapper?.mapDbPokemonStatsToDbPokemonStats(pokemonStats) ?: listOf()
        correctPokemonMain.moves = dbMapper?.mapDbPokemonMovesToDbPokemonMoves(pokemonMoves) ?: listOf()

        return correctPokemonMain
    }

    fun getPokemonData() {

        pokemonRepository.getAllPokemons(100, 0)
                .flatMap(object : io.reactivex.functions.Function<ResultState<AllPokemons>,
                        Observable<ResultState<MainPokemon>>> {
                    //@Throws
                    override fun apply(posts: ResultState<AllPokemons>): Observable<ResultState<MainPokemon>> {

                        when (posts) {
                            is ResultState.Success -> {

                                val pokemonID = getRandomSelectedPokemonId(posts)
                                return pokemonRepository.getRandomSelectedPokemon(pokemonID)
                                    .subscribeOn(Schedulers.io())
                            }
                            is ResultState.Error -> {
                                _pokemonMutableLiveData.value = posts
                                return Observable.fromIterable(mutableListOf<ResultState<MainPokemon>>() )
                                    .subscribeOn(Schedulers.io())
                            }
                            else -> {
                                return Observable.fromIterable(mutableListOf<ResultState<MainPokemon>>() )
                                    .subscribeOn(Schedulers.io())
                            }
                        }
                    }
                })
                .map { randomSelectedPokemon ->

                    when (randomSelectedPokemon) {
                        is ResultState.Success -> {
                            deleteAllPokemonData()
                            insertPokemonIntoDatabase(randomSelectedPokemon.data)

                        }
                        is ResultState.Error -> {
                            _pokemonMutableLiveData.value = randomSelectedPokemon
                        }
                    }
                    randomSelectedPokemon
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : io.reactivex.Observer<ResultState<*>> {


                    override fun onComplete() {}


                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable.add(d)
                    }

                    override fun onNext(post: ResultState<*>) {

                        _pokemonMutableLiveData.value = post
                    }

                    override fun onError(e: Throwable) {
                        Log.e(ContentValues.TAG, "onError received: ", e)
                    }
                })
    }

    private fun getRandomSelectedPokemonId(allPokemons: ResultState.Success<*>) : Int {
        val randomPokemonUrl = allPokemons.data as AllPokemons
        val separateString = randomPokemonUrl.results.random().url.split("/")
        val pokemonId = separateString.get(separateString.size - 2)
        Log.d(
            ContentValues.TAG,
            "Id is: ${pokemonId.toInt()}"
        )
        return pokemonId.toInt()
    }

    private fun deleteAllPokemonData() {
        dbPokemon.pokemonDAO().clearMainPokemonData()
        dbPokemon.pokemonDAO().clearPokemonStatsData()
        dbPokemon.pokemonDAO().clearMPokemonMovesData()
    }

    private fun insertPokemonIntoDatabase(pokemonData: MainPokemon) {

        val pokemonMain =
            dbMapper?.mapDomainMainPokemonToDBMainPokemon(pokemonData) ?: DBMainPokemon()
        dbPokemon.pokemonDAO().insertMainPokemonData(pokemonMain)

        val pokemonStats =
            dbMapper?.mapDomainPokemonStatsToDbPokemonStats(pokemonData.stats) ?: listOf()
        dbPokemon.pokemonDAO().insertStatsPokemonData(pokemonStats)

        val pokemonMoves =
            dbMapper?.mapDomainPokemonMovesToDbPokemonMoves(pokemonData.moves) ?: listOf()
        dbPokemon.pokemonDAO().insertMovesPokemonData(pokemonMoves)
    }

    override fun onCleared() {
        super.onCleared()
        if (!compositeDisposable.isDisposed)
            compositeDisposable.dispose()
    }

}

