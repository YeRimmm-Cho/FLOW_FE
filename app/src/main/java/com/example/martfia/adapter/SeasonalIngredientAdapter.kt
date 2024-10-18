package com.example.martfia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.martfia.R
import com.example.martfia.model.SeasonalIngredient

class SeasonalIngredientAdapter(private val ingredientList: List<SeasonalIngredient>) :
    RecyclerView.Adapter<SeasonalIngredientAdapter.SeasonalIngredientViewHolder>() {

    class SeasonalIngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredientImage: ImageView = itemView.findViewById(R.id.ingredientImage)
        val ingredientName: TextView = itemView.findViewById(R.id.ingredientName)
        val ingredientMonth: TextView = itemView.findViewById(R.id.ingredientMonth)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeasonalIngredientViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seasonal_ingredient, parent, false)
        return SeasonalIngredientViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SeasonalIngredientViewHolder, position: Int) {
        val currentIngredient = ingredientList[position]

        // Glide로 이미지 로드
        Glide.with(holder.itemView.context)
            .load(currentIngredient.imageUrl)
            .into(holder.ingredientImage)

        holder.ingredientName.text = currentIngredient.name
        holder.ingredientMonth.text = currentIngredient.month
    }

    override fun getItemCount() = ingredientList.size
}
