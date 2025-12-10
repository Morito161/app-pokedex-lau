package com.example.ciclovidaactivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

// Interfaz para manejar el clic en un elemento de la lista
class PokemonAdapter(
    private val pokemonList: List<PokemonRef>,
    private val onPokemonClick: (PokemonRef) -> Unit
) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    // Clase que contiene las referencias a las vistas de un solo item (el layout item_pokemon.xml)
    class PokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.pokemon_name_text)
        val cardView: CardView = view.findViewById(R.id.pokemon_card_view)
        val idTextView: TextView = view.findViewById(R.id.pokemon_id_text)
    }

    // Infla el layout del ítem para crear un ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        // Inflamos el layout item_pokemon.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    // Enlaza los datos del Pokémon en una posición con las vistas del ViewHolder
    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemonList[position]

        // Muestra el nombre con la primera letra en mayúscula
        holder.nameTextView.text = pokemon.name.replaceFirstChar { it.uppercase() }
        holder.idTextView.text = "#${pokemon.getId() ?: "???"}" // Muestra el ID

        // Configura el evento de clic en la tarjeta
        holder.cardView.setOnClickListener {
            // Cuando se hace clic, se llama a la función lambda definida en MainActivity
            onPokemonClick(pokemon)
        }
    }

    // Devuelve el número total de elementos en la lista
    override fun getItemCount() = pokemonList.size
}