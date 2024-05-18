package com.example.app_vpn.ui.auth.signup.verify

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.app_vpn.R
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.databinding.ActivityVerificationBinding
import com.example.app_vpn.ui.viewmodel.AuthViewModel
import com.example.app_vpn.ui.viewmodel.MailViewModel
import com.example.app_vpn.util.handleApiError
import com.example.app_vpn.util.snackBar
import com.example.app_vpn.util.startNewActivity
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class VerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationBinding

    private lateinit var bundle: Bundle

    private val authViewModel by viewModels<AuthViewModel>()
    private val mailViewModel by viewModels<MailViewModel>()
    private lateinit var btnVerify: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btnVerify = binding.btnVerify
        bindProgressButton(btnVerify)
        btnVerify.apply {
            attachTextChangeAnimator()
        }

        bundle = intent.extras!!

        binding.txtEmailVerify.text = bundle.getString("email")

        binding.btnResentCode.setOnClickListener {
            resendCode()
        }

        binding.btnVerify.setOnClickListener {
            verifyEmail()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        authViewModel.verifyResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    btnVerify.hideProgress(R.string.verify)
                    val verifyResponse = response.value
                    when (verifyResponse.isSuccess) {
                        true -> {
                            register()
                        }

                        false -> {
                            binding.root.snackBar(verifyResponse.message)
                        }
                    }
                }

                is Resource.Failure -> {
                    handleApiError(response)
                }

                else -> {
                    btnVerify.showProgress {
                        buttonTextRes = R.string.loading
                        progressColor = Color.WHITE
                    }
                }
            }
        }

        authViewModel.registerResponse.observe(this) {
            when (it) {
                is Resource.Success -> {
                    startNewActivity(SuccessActivity::class.java)
                }

                is Resource.Failure -> {
                    handleApiError(it)
                }

                else -> {
                    btnVerify.showProgress {
                        buttonTextRes = R.string.loading
                        progressColor = Color.WHITE
                    }
                }
            }
        }

        mailViewModel.resendCodeResponse.observe(this) {
            when (it) {
                is Resource.Success -> {
                    btnVerify.hideProgress(R.string.verify)
                }

                is Resource.Failure -> {
                    handleApiError(it)
                }

                else -> {
                    btnVerify.showProgress {
                        buttonTextRes = R.string.loading
                        progressColor = Color.WHITE
                    }
                }
            }
        }
    }

    private fun resendCode() {
        val email = bundle.getString("email")
        mailViewModel.resendCode(email!!)
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