package com.example.martfia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.martfia.R
import com.example.martfia.model.RecommendedRecipe

class RecommendedRecipeAdapter(
    private val recipeList: List<RecommendedRecipe>,
    private val onRecipeClick: (RecommendedRecipe) -> Unit // 레시피 클릭 시 처리할 콜백 함수
) : RecyclerView.Adapter<RecommendedRecipeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recipeImage: ImageView = view.findViewById(R.id.recipeImage)
        val recipeName: TextView = view.findViewById(R.id.recipeName)
        val recipeTime: TextView = view.findViewById(R.id.recipeTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommended_recipe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = recipeList[position]

        // 이미지 설정 (Glide 라이브러리를 이용해 URL로부터 이미지 로드)
        Glide.with(holder.itemView.context)
            .load(recipe.imageUrl)
            .into(holder.recipeImage)

        // 레시피 이름 및 소요 시간 설정
        holder.recipeName.text = recipe.name
        holder.recipeTime.text = recipe.time

        // 레시피 클릭 시 콜백 호출
        holder.itemView.setOnClickListener {
            onRecipeClick(recipe)
        }
    }

    override fun getItemCount() = recipeList.size
}
