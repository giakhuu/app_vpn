package com.example.app_vpn

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.example.app_vpn.data.UserPreferences
import com.example.app_vpn.ui.MainActivity
import com.example.app_vpn.ui.auth.AuthActivity
import com.example.app_vpn.util.startNewActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        val userPreferences = UserPreferences(this)

//        userPreferences.accessToken.asLiveData().observe(this, Observer {
//            val activity = if (it == null) AuthActivity::class.java else MainActivity::class.java
//            startNewActivity(activity)
//        })
        startNewActivity(MainActivity::class.java)
    }
}