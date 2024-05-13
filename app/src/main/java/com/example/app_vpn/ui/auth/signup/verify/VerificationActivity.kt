package com.example.app_vpn.ui.auth.signup.verify

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.databinding.ActivityVerificationBinding
import com.example.app_vpn.ui.viewmodel.AuthViewModel
import com.example.app_vpn.util.handleApiError
import com.example.app_vpn.util.startNewActivity
import com.example.app_vpn.util.visible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class VerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationBinding

    private lateinit var bundle: Bundle

    private val authViewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        bundle = intent.extras!!

        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pbVerify.visible(false)

        binding.txtEmailVerify.text = bundle.getString("email")

        binding.btnResentCode.setOnClickListener {

        }

        binding.btnVerify.setOnClickListener {
            verifyEmail()
        }

        authViewModel.verifyResponse.observe(this, Observer {
            when (it) {
                is Resource.Success -> {
                    binding.pbVerify.visible(false)
                    register()
                }
                is Resource.Failure -> {
                    binding.pbVerify.visible(false)
                    handleApiError(it)
                }
                else -> {
                    binding.pbVerify.visible(true)
                }
            }
        })

        authViewModel.registerResponse.observe(this, Observer {
            when (it) {
                is Resource.Success -> {
                    startNewActivity(SuccessActivity::class.java)
                }
                is Resource.Failure -> {
                    handleApiError(it)
                }
                else -> {

                }
            }
        })

    }

    private fun register() {
        val username = bundle.getString("username")
        val email = bundle.getString("email")
        val password = bundle.getString("password")
        authViewModel.register(username!!, email!!, password!!)
    }

    private fun verifyEmail() {
        val code = binding.pvOTP.text.toString()
        val email = bundle.getString("email")
        authViewModel.verify(email!!, code)
    }
}