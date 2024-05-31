package com.example.app_vpn.ui.auth.verify

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.app_vpn.R
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.databinding.ActivityVerificationBinding
import com.example.app_vpn.ui.auth.resetpw.ResetPasswordActivity
import com.example.app_vpn.ui.viewmodel.AuthViewModel
import com.example.app_vpn.ui.viewmodel.MailViewModel
import com.example.app_vpn.util.enable
import com.example.app_vpn.util.handleApiError
import com.example.app_vpn.util.onDone
import com.example.app_vpn.util.onLoad
import com.example.app_vpn.util.setUp
import com.example.app_vpn.util.startNewActivity
import com.example.app_vpn.util.visible
import com.github.razir.progressbutton.bindProgressButton
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
        btnVerify.setUp()

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

        binding.pvOTP.addTextChangedListener {
            binding.btnVerify.enable(
                it.toString().length == 6
            )
        }

        authViewModel.verifyResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    btnVerify.onDone(getString(R.string.verify))
                    val verifyResponse = response.value
                    when (verifyResponse.isSuccess) {
                        true -> {
                            if (bundle.containsKey("username") && bundle.containsKey("password")) {
                                register()
                            } else {
                                val intent = Intent(this, ResetPasswordActivity::class.java)
                                intent.putExtras(bundle)
                                startActivity(intent)
                            }
                        }
                        false -> {
                            binding.txtVerifyError.apply {
                                if (verifyResponse.message.contains("expired")) {
                                    text = context.getString(R.string.verrify_error_1)
                                } else if (verifyResponse.message.contains("Invalid")) {
                                    text = context.getString(R.string.verify_error_2)
                                }
                                visible(true)
                            }
                        }
                    }
                }

                is Resource.Failure -> {
                    btnVerify.onDone(getString(R.string.verify))
                    handleApiError(response)
                }

                else -> {
                    btnVerify.onLoad()
                }
            }
        }

        authViewModel.registerResponse.observe(this) {
            when (it) {
                is Resource.Success -> {
                    startNewActivity(SuccessActivity::class.java)
                }

                is Resource.Failure -> {
                    btnVerify.onDone(getString(R.string.verify))
                    handleApiError(it)
                }

                else -> {
                    btnVerify.onLoad()
                }
            }
        }

        mailViewModel.resendCodeResponse.observe(this) {
            when (it) {
                is Resource.Success -> {
                    btnVerify.onDone(getString(R.string.verify))
                }

                is Resource.Failure -> {
                    btnVerify.onDone(getString(R.string.verify))
                    handleApiError(it)
                }

                else -> {
                    binding.txtVerifyError.visible(false)
                    btnVerify.onLoad()
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