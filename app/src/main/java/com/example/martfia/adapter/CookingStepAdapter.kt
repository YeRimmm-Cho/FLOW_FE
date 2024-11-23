package com.example.martfia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.martfia.R
import com.example.martfia.model.CookingStep

class CookingStepAdapter(private val stepList: List<CookingStep>) :
    RecyclerView.Adapter<CookingStepAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stepNumberTextView: TextView = view.findViewById(R.id.stepNumberTextView)
        val stepDescriptionTextView: TextView = view.findViewById(R.id.stepDescriptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_method, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val step = stepList[position]
        holder.stepNumberTextView.text = step.stepNumber.toString()
        holder.stepDescriptionTextView.text = step.description
    }

    override fun getItemCount(): Int = stepList.size
}
