package com.example.martfia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.martfia.R
import com.example.martfia.model.Ingredient

class IngredientAdapter(private val ingredientList: List<Ingredient>) : RecyclerView.Adapter<IngredientAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ingredientTextView: TextView = view.findViewById(R.id.ingredientTextView)
        val ingredientImageView: ImageView = view.findViewById(R.id.ingredientImageView)
        val dividerView: View = view.findViewById(R.id.dividerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredientList[position]

        // 재료 이름 설정
        holder.ingredientTextView.text = ingredient.name

        // 이미지 로드
        Glide.with(holder.itemView.context)
            .load(ingredient.image_url) // API에서 받은 이미지 URL
            .circleCrop()
            .placeholder(R.drawable.ic_vege) // 기본 이미지
            .error(R.drawable.ic_vege) // 로드 실패 시 이미지
            .into(holder.ingredientImageView)

        // 마지막 항목인지 확인
        if (position == ingredientList.size - 1) {
            holder.dividerView.visibility = View.GONE // 마지막 항목은 구분선 숨김
        } else {
            holder.dividerView.visibility = View.VISIBLE // 나머지 항목은 구분선 표시
        }
    }

    override fun getItemCount() = ingredientList.size
}
