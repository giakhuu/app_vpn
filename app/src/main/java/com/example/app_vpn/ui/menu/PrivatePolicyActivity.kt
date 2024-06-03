package com.example.app_vpn.ui.menu

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_vpn.R
import com.example.app_vpn.databinding.ActivityPrivatePolicyBinding
import com.example.app_vpn.ui.BaseActivity

class PrivatePolicyActivity : BaseActivity() {
    private lateinit var binding: ActivityPrivatePolicyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivatePolicyBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnPolicyBack.setOnClickListener {
            finish()
        }
    }
}