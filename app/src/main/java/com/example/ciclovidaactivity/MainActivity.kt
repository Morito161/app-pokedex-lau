package com.example.ciclovidaactivity

import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
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
import java.io.IOException

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

    // MediaPlayer para el sonido
    private var mediaPlayer: MediaPlayer? = null

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
                toolbar.navigationIcon = null
                // Detener y liberar el MediaPlayer al salir de la vista de detalle
                mediaPlayer?.release()
                mediaPlayer = null
            } else {
                // Mostrar Detalle
                displayPokemonDetail(detail)
                recyclerView.isVisible = false
                detailView.isVisible = true
                toolbarTitle.text = detail.name.replaceFirstChar { it.uppercase() }
                toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            }
        })
    }

    private fun displayPokemonDetail(detail: PokemonDetail) {
        // Obtener referencias a las vistas de detalle
        val nameDetail = findViewById<TextView>(R.id.detail_pokemon_name)
        val idDetail = findViewById<TextView>(R.id.detail_pokemon_id)
        val imageDetail = findViewById<ImageView>(R.id.detail_pokemon_image)
        val heightDetail = findViewById<TextView>(R.id.detail_height)
        val weightDetail = findViewById<TextView>(R.id.detail_weight)
        val descriptionDetail = findViewById<TextView>(R.id.detail_pokemon_description)
        val playCryButton = findViewById<ImageButton>(R.id.play_cry_button)
        val backButton = findViewById<Button>(R.id.back_button)

        // Asignar datos
        nameDetail.text = detail.name.replaceFirstChar { it.uppercase() }
        idDetail.text = "ID: #${detail.id}"
        heightDetail.text = "Altura: ${detail.height / 10.0} m"
        weightDetail.text = "Peso: ${detail.weight / 10.0} kg"
        descriptionDetail.text = detail.description

        // Cargar imagen con Coil
        imageDetail.load(detail.getImageUrl()) {
            placeholder(android.R.color.darker_gray)
            error(android.R.color.holo_red_dark)
            crossfade(true)
        }

        // Configurar el botón de sonido
        playCryButton.setOnClickListener {
            playPokemonCry(detail.cries.latest)
        }

        // Configurar el botón de regresar
        backButton.backgroundTintList = ColorStateList.valueOf(Color.RED)
        backButton.setOnClickListener {
            viewModel.clearSelection()
        }
    }

    private fun playPokemonCry(url: String) {
        // Detener cualquier reproducción anterior
        mediaPlayer?.release()
        mediaPlayer = null

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(url)
                prepareAsync() // Preparar de forma asíncrona para no bloquear el hilo principal
                setOnPreparedListener {
                    start()
                }
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(this@MainActivity, "No se pudo reproducir el sonido.", Toast.LENGTH_SHORT).show()
                    true
                }
            } catch (e: IOException) {
                Toast.makeText(this@MainActivity, "Error al cargar el sonido.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Liberar el MediaPlayer cuando la actividad ya no es visible
        mediaPlayer?.release()
        mediaPlayer = null
    }
}