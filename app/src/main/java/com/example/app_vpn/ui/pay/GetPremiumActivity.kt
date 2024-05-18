package com.example.app_vpn.ui.pay

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_vpn.data.entities.Subscription
import com.example.app_vpn.databinding.ActivityGetPremiumBinding
import com.example.app_vpn.ui.custom.CustomBenefitAdapter
import com.example.app_vpn.ui.custom.CustomSubscriptionAdapter
import com.example.app_vpn.util.enable
import com.example.app_vpn.util.listBenefit
import com.example.app_vpn.util.listSubscriptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GetPremiumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGetPremiumBinding

    private lateinit var customBenefitAdapter: CustomBenefitAdapter
    private lateinit var customSubscriptionAdapter: CustomSubscriptionAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGetPremiumBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)


        binding.btnSubcription.enable(false)

        // Quyền lợi
        customBenefitAdapter = CustomBenefitAdapter(this, listBenefit)

        // Thiết lập ListView để sử dụng CustomBenefitAdapter
        binding.listViewBenefits.adapter = customBenefitAdapter

        // Tắt activity
        binding.btnexit.setOnClickListener {
            finish()
        }

        // Chọn gói
        customSubscriptionAdapter = CustomSubscriptionAdapter(listSubscriptions) { position ->
            binding.btnSubcription.enable(true)

            binding.btnSubcription.setOnClickListener {
                subscriptionPremium(listSubscriptions[position])
            }
        }

        binding.listViewSubcription.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.listViewSubcription.adapter = customSubscriptionAdapter
    }

    private fun subscriptionPremium(subscription: Subscription) {
        val bundle = Bundle().apply {
            putString("amount", subscription.price)
        }
        val intent = Intent(this, PaymentVipActivity::class.java).apply {
            putExtras(bundle)
        }
        startActivity(intent)
    }
}