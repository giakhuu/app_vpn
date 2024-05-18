package com.example.app_vpn.ui.custom

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.app_vpn.R
import com.example.app_vpn.data.entities.Subscription
import com.google.android.material.card.MaterialCardView

class CustomSubscriptionAdapter(
    private val list: List<Subscription>, private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<CustomSubscriptionAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val _number: TextView = itemView.findViewById(R.id.number)
        private val _duration: TextView = itemView.findViewById(R.id.duration)
        private val _price: TextView = itemView.findViewById(R.id.price)
        private val _radioButtonPricing: RadioButton = itemView.findViewById(R.id.radioButtonPricing)
        private val _materialCardView: MaterialCardView = itemView as MaterialCardView
        fun bind(subscription: Subscription) {
            with(subscription) {
                _number.text = number.toString()
                _price.text = price
                _duration.text = duration
                _radioButtonPricing.isChecked = selected
                setBorderColor(selected)
            }
        }
        private fun setBorderColor(selected: Boolean) {
            val color = if (selected) android.R.color.holo_orange_light else android.R.color.white
            _materialCardView.strokeColor = ContextCompat.getColor(itemView.context, color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pricing, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subscription = list[position]
        holder.bind(subscription)

        holder.itemView.setOnClickListener {
            list.forEach { it.selected = false }
            subscription.selected = true
            onClick(holder.adapterPosition)
            notifyDataSetChanged()
        }
    }
}