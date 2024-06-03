package com.example.app_vpn.ui.menu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_vpn.R
import com.example.app_vpn.databinding.ActivityContactBinding
import com.example.app_vpn.ui.BaseActivity

class ContactActivity : BaseActivity() {

    private lateinit var binding: ActivityContactBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnContactBack.setOnClickListener {
            finish()
        }

        // Sử lí sự kiện khi click vào nút instagram
        binding.btnContactIns.setOnClickListener {
            // Mở trang instagram
            val uri = Uri.parse("https://www.instagram.com/")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        // Sử lí sự kiện khi click vào nút facebook
        binding.btnContactFB.setOnClickListener {
            // Mở trang facebook
            val uri = Uri.parse("https://www.facebook.com/")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        // Sử lí sự kiện khi click vào nút linkedin
        binding.btnContactLK.setOnClickListener {
            // Mở trang linkedin
            val uri = Uri.parse("https://www.linkedin.com/")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }
}