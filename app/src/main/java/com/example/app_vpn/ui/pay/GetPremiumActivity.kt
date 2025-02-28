package com.example.app_vpn.ui.pay

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_vpn.R
import com.example.app_vpn.data.entities.Subscription
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.databinding.ActivityGetPremiumBinding
import com.example.app_vpn.ui.BaseActivity
import com.example.app_vpn.ui.custom.CustomBenefitAdapter
import com.example.app_vpn.ui.custom.CustomSubscriptionAdapter
import com.example.app_vpn.ui.viewmodel.PaymentViewModel
import com.example.app_vpn.ui.viewmodel.UserViewModel
import com.example.app_vpn.util.PAYPAL_CLIENT_ID
import com.example.app_vpn.util.PAYPAL_SECRECT_ID
import com.example.app_vpn.util.REDIRECT_URL
import com.example.app_vpn.util.enable
import com.example.app_vpn.util.listBenefit
import com.example.app_vpn.util.listSubscriptions
import com.example.app_vpn.util.showToast
import com.example.app_vpn.util.startNewActivity
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.Environment
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutListener
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutRequest
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
@AndroidEntryPoint
class GetPremiumActivity : BaseActivity() {

    private lateinit var binding: ActivityGetPremiumBinding
    private val paymentViewModel by viewModels<PaymentViewModel>()
    private val userViewModel by viewModels<UserViewModel>()

    private lateinit var customBenefitAdapter: CustomBenefitAdapter
    private lateinit var customSubscriptionAdapter: CustomSubscriptionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetPremiumBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupUI()
        fetchInitialData()
        observerViewmodel()
    }

    private fun setupUI() {
        applyWindowInsets()
        setupBenefitList()
        setupSubscriptionList(listSubscriptions)
        setupListeners()
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun fetchInitialData() {
        paymentViewModel.getAccessToken()
        paymentViewModel.getAllSubsciption()
    }

    private fun setupBenefitList() {
        customBenefitAdapter = CustomBenefitAdapter(this, listBenefit)
        binding.listViewBenefits.adapter = customBenefitAdapter
    }

    private fun setupSubscriptionList(listSubscriptions: List<Subscription>) {
        customSubscriptionAdapter = CustomSubscriptionAdapter(listSubscriptions) { position ->
            paymentViewModel.selectedChoice.value = position
            binding.btnSubcription.isEnabled = true
        }
        binding.listViewSubcription.layoutManager = LinearLayoutManager(this)
        binding.listViewSubcription.adapter = customSubscriptionAdapter
    }

    private fun setupListeners() {
        binding.btnexit.setOnClickListener { finish() }
        binding.btnSubcription.setOnClickListener {
            paymentViewModel.startOrder()
        }
    }

    private fun observerViewmodel() {
        lifecycleScope.launch {
            paymentViewModel.orderResponse.collect { data ->
                if(data != null) {
                    val jsonString = data.string()
                    val jsonObject = JSONObject(jsonString)
                    val orderId = jsonObject.getString("id")
                    Log.d("payment", "observerViewmodel: $orderId")
                    handlerOrderID(orderId)
                }
            }
        }

        lifecycleScope.launch {
            paymentViewModel.subscription.collect {data ->
                if(data != null) {
                    setupSubscriptionList(data)
                }
            }
        }
        lifecycleScope.launch {
            paymentViewModel.toastMessage.collect { message ->
                showToast(message)
            }
        }

        lifecycleScope.launch {
            paymentViewModel.captureResponse.collect { data ->
                if(data != null) {
                    startNewActivity(PaymentSuccessActivity::class.java)
                }
            }
        }
    }



    private fun handlerOrderID(orderID: String) {
        val config = CoreConfig(PAYPAL_CLIENT_ID, environment = Environment.SANDBOX)
        val payPalWebCheckoutClient = PayPalWebCheckoutClient(this@GetPremiumActivity, config, "${REDIRECT_URL}/payment/return")
        payPalWebCheckoutClient.listener = object : PayPalWebCheckoutListener {
            override fun onPayPalWebSuccess(result: PayPalWebCheckoutResult) {
                Log.d("payment", "onPayPalWebSuccess: $result")
            }

            override fun onPayPalWebFailure(error: PayPalSDKError) {
                Log.d("payment", "onPayPalWebFailure: $error")
            }

            override fun onPayPalWebCanceled() {
                Log.d("payment", "onPayPalWebCanceled: ")
            }
        }

        paymentViewModel.orderID.value = orderID
        val payPalWebCheckoutRequest =
            PayPalWebCheckoutRequest(orderID, fundingSource = PayPalWebCheckoutFundingSource.PAYPAL)
        payPalWebCheckoutClient.start(payPalWebCheckoutRequest)
    }


    override fun onNewIntent(intent: Intent)  {
        super.onNewIntent(intent)
        Log.d("payment", "onNewIntent: $intent")
        if (intent?.data!!.getQueryParameter("opType") == "payment") {
            paymentViewModel.accessToken.value?.let { paymentViewModel.captureOrder(paymentViewModel.orderID.value ?: "", it.accessToken) }
        } else if (intent?.data!!.getQueryParameter("opType") == "cancel") {
            Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}