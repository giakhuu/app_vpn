package com.example.app_vpn.ui.auth.resetpw

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_vpn.R
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.databinding.ActivityForgotPasswordBinding
import com.example.app_vpn.ui.auth.verify.VerificationActivity
import com.example.app_vpn.ui.viewmodel.AuthViewModel
import com.example.app_vpn.ui.viewmodel.MailViewModel
import com.example.app_vpn.util.handleApiError
import com.example.app_vpn.util.hideKeyboard
import com.example.app_vpn.util.isValid
import com.example.app_vpn.util.isValidEmail
import com.example.app_vpn.util.onDone
import com.example.app_vpn.util.onLoad
import com.example.app_vpn.util.setUp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private val mailViewModel by viewModels<MailViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()

    private lateinit var btnSubmit : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnSubmit = binding.btnSubmit
        btnSubmit.setUp()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.iplyForgotEmail.isValid(
            binding.txtForgotEmail,
            binding.btnSubmit,
            invalidHelperText = "Invalid Email",
            validate = {this.isValidEmail()}
        )

        btnSubmit.setOnClickListener {
            isEmailExist()
            hideKeyboard(this, it)
        }

        authViewModel.isEmailExist.observe(this) {
            when (it) {
                is Resource.Success -> {
                    btnSubmit.onDone(getString(R.string.verify_email))
                    val responseValue = it.value
                    if (responseValue.isSuccess) {
                        sendVerify()
                    } else {
                        binding.iplyForgotEmail.apply {
                            helperText = "Email does not exist"
                            setHelperTextColor(ColorStateList.valueOf(resources.getColor(R.color.red)))
                        }
                    }
                }

                is Resource.Failure -> {
                    btnSubmit.onDone(getString(R.string.verify_email))
                    handleApiError(it)
                }

                is Resource.Loading -> {
                    btnSubmit.onLoad()
                }
            }
        }

        mailViewModel.sendVerifyResponse.observe(this) {
            when (it) {
                is Resource.Success -> {
                    btnSubmit.onDone(getString(R.string.verify_email))
                    onSendMailSuccess()
                }
                is Resource.Failure -> {
                    btnSubmit.onDone(getString(R.string.verify_email))
                    handleApiError(it)
                }
                is Resource.Loading -> {
                    btnSubmit.onLoad()
                }
            }
        }
    }

    private fun onSendMailSuccess() {
        val bundle = Bundle()
        bundle.putString("email", binding.txtForgotEmail.text.toString())
        val intent = Intent(this, VerificationActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    private fun sendVerify() {
        val email = binding.txtForgotEmail.text?.trim().toString()
        mailViewModel.sendVerifyCode(email)
    }

    private fun isEmailExist() {
        authViewModel.isEmailExist(binding.txtForgotEmail.text?.trim().toString())
    }
}