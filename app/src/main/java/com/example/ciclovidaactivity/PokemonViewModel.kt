package com.example.ciclovidaactivity

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PokemonViewModel : ViewModel() {
    private val BASE_URL = "https://pokeapi.co/api/v2/"

    // 1. Inicialización de Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val apiService: PokeApiService = retrofit.create(PokeApiService::class.java)

    // 2. LiveData
    val pokemonList = MutableLiveData<List<PokemonRef>>()
    val selectedPokemon = MutableLiveData<PokemonDetail?>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()

    init {
        fetchPokemonList()
    }

    private fun fetchPokemonList() {
        isLoading.value = true
        error.value = null
        viewModelScope.launch {
            try {
                val response = apiService.getPokemonList()
                pokemonList.value = response.results
            } catch (e: Exception) {
                Log.e("PokeViewModel", "Error al obtener lista: ${e.message}")
                error.value = "Error al cargar la lista de Pokémon: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun selectPokemon(pokemonRef: PokemonRef) {
        val id = pokemonRef.getId() ?: return

        isLoading.value = true
        error.value = null
        selectedPokemon.value = null

        viewModelScope.launch {
            try {
                // 1. Obtener detalles básicos
                val detail = apiService.getPokemonDetail(id)

                // 2. Obtener descripción (especie)
                val species = apiService.getPokemonSpecies(id)

                // 3. Buscar la descripción en español
                val spanishDescription = species.flavorTextEntries
                    .firstOrNull { it.language.name == "es" }?.flavorText

                // 4. Limpiar el texto (reemplazar saltos de línea)
                detail.description = spanishDescription?.replace('\n', ' ') ?: "No se encontró descripción en español."

                // 5. Actualizar el LiveData
                selectedPokemon.postValue(detail) // usamos postValue para seguridad entre hilos

            } catch (e: Exception) {
                Log.e("PokeViewModel", "Error al obtener detalle o especie: ${e.message}")
                error.value = "Error al cargar datos de ${pokemonRef.name}: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun clearSelection() {
        selectedPokemon.value = null
    }
}