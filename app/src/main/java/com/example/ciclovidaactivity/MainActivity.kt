package com.example.ciclovidaactivity


import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load

class MainActivity : ComponentActivity() {

    // Inicialización perezosa del ViewModel usando KTX (activity-ktx dependency)
    private val viewModel: PokemonViewModel by viewModels()

    // Componentes de la UI
    private lateinit var recyclerView: RecyclerView
    private lateinit var detailView: View // Referencia al ScrollView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Establece el layout principal (activity_main.xml)
        setContentView(R.layout.activity_main)

        // Inicializar vistas por ID (necesitas importar R.id.*)
        recyclerView = findViewById(R.id.pokemon_recycler_view)
        detailView = findViewById(R.id.pokemon_detail_container)
        loadingProgressBar = findViewById(R.id.loading_progress_bar)
        errorTextView = findViewById(R.id.error_text_view)
        toolbar = findViewById(R.id.toolbar)
        toolbarTitle = findViewById(R.id.toolbar_title)

        // Configuración inicial del RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Configuración de la barra de herramientas
        // Configura el listener para el botón de navegación (Atrás)
        toolbar.setNavigationOnClickListener {
            if (viewModel.selectedPokemon.value != null) {
                // Si estamos en detalle, limpiamos la selección para volver a la lista
                viewModel.clearSelection()
            }
        }

        // Iniciar la observación de los datos
        observeViewModel()
    }

    private fun observeViewModel() {
        // Observa la lista de Pokémon
        viewModel.pokemonList.observe(this, Observer { list ->
            // Creamos el adaptador y le pasamos la función de clic al ViewModel
            recyclerView.adapter = PokemonAdapter(list) { pokemonRef ->
                viewModel.selectPokemon(pokemonRef)
            }
        })

        // Observa el estado de carga (Mostrar/Ocultar el spinner)
        viewModel.isLoading.observe(this, Observer { isLoading ->
            loadingProgressBar.isVisible = isLoading
            if (isLoading) {
                // Ocultar contenido mientras carga
                errorTextView.isVisible = false
                // No ocultamos el recycler/detalle porque podríamos estar cargando el detalle de un solo ítem
            }
        })

        // Observa si hay un error
        viewModel.error.observe(this, Observer { errorMsg ->
            if (errorMsg != null) {
                errorTextView.text = errorMsg
                errorTextView.isVisible = true
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            } else {
                errorTextView.isVisible = false
            }
        })

        // Observa el Pokémon seleccionado para cambiar a la vista de detalle
        viewModel.selectedPokemon.observe(this, Observer { detail ->
            if (detail == null) {
                // Volver a la Lista
                recyclerView.isVisible = true
                detailView.isVisible = false
                toolbarTitle.text = getString(R.string.app_name)
                // El 'navigationIcon' debe ser nulo o un color transparente si no tienes ícono atrás
                toolbar.navigationIcon = null
            } else {
                // Mostrar Detalle
                displayPokemonDetail(detail)
                recyclerView.isVisible = false
                detailView.isVisible = true
                toolbarTitle.text = detail.name.replaceFirstChar { it.uppercase() }
                // Muestra un ícono de flecha atrás. NOTA: Debes tener este recurso en 'res/drawable'
                // Reemplaza 'R.drawable.ic_back' con un ícono real si el IDE no lo sugiere.
                toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            }
        })
    }

    private fun displayPokemonDetail(detail: PokemonDetail) {
        // Obtener referencias a las vistas de detalle (que están en activity_main.xml)
        val nameDetail = findViewById<TextView>(R.id.detail_pokemon_name)
        val idDetail = findViewById<TextView>(R.id.detail_pokemon_id)
        val imageDetail = findViewById<ImageView>(R.id.detail_pokemon_image)
        val heightDetail = findViewById<TextView>(R.id.detail_height)
        val weightDetail = findViewById<TextView>(R.id.detail_weight)

        // Asignar datos. Conversión: Decímetros -> Metros (dividir por 10), Hectogramos -> Kilogramos (dividir por 10)
        nameDetail.text = detail.name.replaceFirstChar { it.uppercase() }
        idDetail.text = "ID: #${detail.id}"
        heightDetail.text = "Altura: ${detail.height / 10.0} m"
        weightDetail.text = "Peso: ${detail.weight / 10.0} kg"

        // Cargar imagen con Coil.
        imageDetail.load(detail.getImageUrl()) {
            // Placeholder y Error son opcionales, pero recomendados para una buena UX
            // Usamos un color simple como fallback si no hay un drawable
            placeholder(android.R.color.darker_gray)
            error(android.R.color.holo_red_dark)
            crossfade(true)
        }
    }
}