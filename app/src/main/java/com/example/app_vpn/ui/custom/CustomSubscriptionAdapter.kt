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
    private val list: List<Subscription>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<CustomSubscriptionAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val numberTextView: TextView = itemView.findViewById(R.id.number)
        private val durationTextView: TextView = itemView.findViewById(R.id.duration)
        private val priceTextView: TextView = itemView.findViewById(R.id.price)
        private val radioButtonPricing: RadioButton = itemView.findViewById(R.id.radioButtonPricing)
        private val materialCardView: MaterialCardView = itemView as MaterialCardView

        fun bind(subscription: Subscription) {
            with(subscription) {
                numberTextView.text = number.toString()
                priceTextView.text = price
                durationTextView.text = duration
                radioButtonPricing.isChecked = selected
                setBorderColor(selected)

                itemView.setOnClickListener { handleItemClick(this) }
                radioButtonPricing.setOnClickListener { handleItemClick(this) }
            }
        }

        private fun setBorderColor(selected: Boolean) {
            val colorResId = if (selected) android.R.color.holo_orange_light else android.R.color.white
            materialCardView.strokeColor = ContextCompat.getColor(itemView.context, colorResId)
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun handleItemClick(subscription: Subscription) {
            list.forEach { it.selected = false }
            subscription.selected = true
            onClick(adapterPosition)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pricing, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}
