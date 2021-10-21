package com.example.mapapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mapapp.databinding.ItemSearchBinding
import com.example.mapapp.models.Place

class PlacesAdapter(var list: List<Place>, var onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<PlacesAdapter.Vh>() {
            inner class Vh(var itemSearchBinding: ItemSearchBinding) :
        RecyclerView.ViewHolder(itemSearchBinding.root) {

        fun onBind(place: Place, position: Int) {
            itemSearchBinding.mainText.text = place.main_text
            itemSearchBinding.secondaryText.text = place.secondary_text

            itemSearchBinding.itemResult.setOnClickListener {
                onItemClickListener.onItemClick(place, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position], position)
    }

    override fun getItemCount(): Int = list.size

    interface OnItemClickListener {
        fun onItemClick(place: Place, position: Int)
    }
}