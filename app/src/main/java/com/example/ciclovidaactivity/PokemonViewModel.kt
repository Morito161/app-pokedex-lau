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
    // Creamos una instancia de Retrofit y la interfaz PokeApiService.
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val apiService: PokeApiService = retrofit.create(PokeApiService::class.java)

    // 2. LiveData (Datos observables)
    // Estos objetos notifican a la UI cada vez que los datos cambian.
    val pokemonList = MutableLiveData<List<PokemonRef>>() // Lista para el RecyclerView
    val selectedPokemon = MutableLiveData<PokemonDetail?>() // Detalle del Pokémon seleccionado
    val isLoading = MutableLiveData<Boolean>() // Indica si hay una llamada de red activa
    val error = MutableLiveData<String?>() // Mensajes de error

    init {
        // Al crear el ViewModel, iniciamos la carga de la lista
        fetchPokemonList()
    }

    // Función para obtener la lista inicial de Pokémon
    private fun fetchPokemonList() {
        isLoading.value = true
        error.value = null

        // Ejecutamos la llamada en un Coroutine Scope asociado al ciclo de vida del ViewModel
        viewModelScope.launch {
            try {
                val response = apiService.getPokemonList()
                pokemonList.value = response.results // Actualiza LiveData con la lista
            } catch (e: Exception) {
                Log.e("PokeViewModel", "Error al obtener lista: ${e.message}")
                error.value = "Error al cargar la lista de Pokémon: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    // Función para obtener el detalle de un Pokémon al hacer clic
    fun selectPokemon(pokemonRef: PokemonRef) {
        val id = pokemonRef.getId()
        if (id == null) {
            error.value = "ID de Pokémon no válido."
            return
        }

        isLoading.value = true
        error.value = null
        selectedPokemon.value = null // Limpiar el detalle anterior

        viewModelScope.launch {
            try {
                val detail = apiService.getPokemonDetail(id)
                selectedPokemon.value = detail // Actualiza LiveData con el detalle
            } catch (e: Exception) {
                Log.e("PokeViewModel", "Error al obtener detalle: ${e.message}")
                error.value = "Error al cargar el detalle de ${pokemonRef.name}: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    // Función para volver a la lista (limpiar el detalle)
    fun clearSelection() {
        selectedPokemon.value = null
    }
}