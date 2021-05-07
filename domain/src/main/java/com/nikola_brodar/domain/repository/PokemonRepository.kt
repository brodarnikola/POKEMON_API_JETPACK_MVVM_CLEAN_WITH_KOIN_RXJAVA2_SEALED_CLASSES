/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nikola_brodar.domain.repository

import com.nikola_brodar.domain.ResultState
import com.nikola_brodar.domain.model.*

import io.reactivex.Observable

interface PokemonRepository {

    fun getAllPokemons(limit: Int, offset: Int) : Observable<ResultState<AllPokemons>> //AllPokemons

    fun getRandomSelectedPokemon(id: Int) : Observable<ResultState<MainPokemon>> //MainPokemon


}
