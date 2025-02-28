package com.example.app_vpn.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import com.example.app_vpn.R
import com.example.app_vpn.data.entities.VpnServer
import com.example.app_vpn.data.preferences.PreferenceManager
import com.example.app_vpn.util.enable
import com.squareup.picasso.Picasso

class CustomArrayVpnServerAdapter(
    val activity: Context,
    val list: List<VpnServer>,
    val isPremium: Boolean
) :
    ArrayAdapter<VpnServer>(activity, R.layout.item_country_layout) {

    private var checkedPosition = -1 // Khởi tạo checkedPosition là -1
    private lateinit var preferenceManager: PreferenceManager

    override fun getCount(): Int {
        return list.size
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(activity)
        val rowView = inflater.inflate(R.layout.item_country_layout, parent, false)

        val flagImg = rowView.findViewById<ImageView>(R.id.flagImg)
        val countryName = rowView.findViewById<TextView>(R.id.countryName)
        val pingConnection = rowView.findViewById<TextView>(R.id.pingConnection)
        val radioButton = rowView.findViewById<RadioButton>(R.id.radioButton)
        val lockIcon = rowView.findViewById<ImageView>(R.id.lockIcon)
        // set preferenceManager
        preferenceManager = PreferenceManager(activity)



        // Set hình ảnh của quốc gia
        Picasso.get().load(list[position].flag).into(flagImg)

        // Set tên quốc gia
        countryName.text = list[position].name

        // Set thời gian ping
        pingConnection.text = "50ms"

        // xử lí enable coutry premium
        // !(true && true)
        Log.d("vpnServer", "${list[position].name}: ${list[position].premium}")
        val isEnabled = if (isPremium) {
            // Người dùng premium → cho phép truy cập tất cả các server
            true
        } else {
            // Người dùng không phải premium → chỉ cho phép các server free
            !list[position].premium
        }

        lockIcon.visibility = if (isEnabled) View.GONE else View.VISIBLE
        rowView.enable(isEnabled)
        radioButton.enable(isEnabled)
        // Thiết lập sự kiện khi RadioButton được nhấn
        val onClickListener = View.OnClickListener {
            // Cập nhật checkedPosition khi RadioButton được chọn
            notifyDataSetChanged()

            // Kiểm tra nếu RadioButton được chọn ở vị trí hiện tại
            if (position != checkedPosition) {
                val countryNameText = countryName.text.toString()
                Toast.makeText(activity, countryNameText, Toast.LENGTH_SHORT).show()
                checkedPosition = position

                // lưu vào preference
                preferenceManager.saveVpnServer(list[position])
                val country = preferenceManager.getVpnServer()
            }
        }

        rowView.setOnClickListener(onClickListener)
        radioButton.setOnClickListener(onClickListener)

        // Thiết lập trạng thái của RadioButton dựa trên checkedPosition
        radioButton.isChecked = position == checkedPosition

        return rowView
    }
}
