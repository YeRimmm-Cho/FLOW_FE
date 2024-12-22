package com.example.martfia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.martfia.R
import com.example.martfia.model.response.Instruction

class CookingStepAdapter(private val steps: List<Instruction>) :
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
        val instruction = steps[position]
        holder.stepNumberTextView.text = instruction.step
        holder.stepDescriptionTextView.text = instruction.description
    }

    override fun getItemCount(): Int = steps.size
}
