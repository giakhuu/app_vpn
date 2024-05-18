package com.example.app_vpn.ui.custom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.app_vpn.R
import com.example.app_vpn.data.entities.Benefit
import com.example.app_vpn.data.entities.Country

class CustomBenefitAdapter(val activity: Context, val list: List<Benefit>) :
    ArrayAdapter<Country>(activity, R.layout.item_benefit) {

    // Số dòng sẽ vẽ nên
    override fun getCount(): Int {
        return list.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(activity)
        val rowView = inflater.inflate(R.layout.item_benefit, parent, false)

        val title = rowView.findViewById<TextView>(R.id.titleBenefit)
        val subtitle = rowView.findViewById<TextView>(R.id.subTitleBenefit)

        title.text = list[position].title
        subtitle.text = list[position].subTile

        return rowView
    }

}