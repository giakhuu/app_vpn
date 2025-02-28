package com.example.app_vpn.ui.pay

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_vpn.R
import com.example.app_vpn.databinding.ActivityPaymentSuccessBinding
import com.example.app_vpn.databinding.ActivitySuccessBinding
import com.example.app_vpn.ui.BaseActivity
import com.example.app_vpn.ui.auth.login.LoginActivity
import com.example.app_vpn.util.startNewActivity

class PaymentSuccessActivity : BaseActivity() {
    private lateinit var binding: ActivityPaymentSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment_success)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityPaymentSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnOkay.setOnClickListener {
            startNewActivity(LoginActivity::class.java)
        }
    }
}