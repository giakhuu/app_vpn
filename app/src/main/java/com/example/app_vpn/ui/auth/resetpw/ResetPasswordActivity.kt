package com.example.app_vpn.ui.auth.resetpw

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.app_vpn.R
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.databinding.ActivityResetPasswordBinding
import com.example.app_vpn.ui.BaseActivity
import com.example.app_vpn.ui.auth.login.TAG
import com.example.app_vpn.ui.viewmodel.AuthViewModel
import com.example.app_vpn.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResetPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private val authViewModel by viewModels<AuthViewModel>()
    private var email: String? = null
    private var accessToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupInsets()
        getEmailFromIntent()
        initViews()
        observeViewModel()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun getEmailFromIntent() {
        email = intent.data?.getQueryParameter("email")
        accessToken =
            intent.data?.toString()?.substringAfter("#access_token=", "")?.substringBefore("&")
        if (email.isNullOrEmpty()) {
            showToast(getString(R.string.enter_your_email))
            finish()
            return
        }
    }

    private fun initViews() {
        with(binding) {
            btnResetPassword.setUp()
            btnResetBack.setOnClickListener { finish() }

            iplyNewPassword.isValid(
                editText = txtNewPassword,
                invalidHelperText = getString(R.string.passwordValidateError),
                validate = { this.isValidPassword() }
            )

            iplyConfirmPassword.isValid(
                editText = txtConfirmPassword,
                submitButton = btnResetPassword,
                invalidHelperText = getString(R.string.passwordConfirmValidateError),
                validate = { txtConfirmPassword.text.toString() == txtNewPassword.text.toString() }
            )

            btnResetPassword.setOnClickListener { resetPassword() }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            authViewModel.updatePasswordResponse.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        binding.btnResetPassword.onDone(getString(R.string.reset_password))
                        showToast(getString(R.string.reset_password_success))
                        startActivity(Intent(this@ResetPasswordActivity, ResetPasswordSuccessActivity::class.java))
                    }
                    is Resource.Error -> {
                        binding.btnResetPassword.onDone(getString(R.string.reset_password))
                        showToast(resource.error.message.toString())
                    }
                    is Resource.Loading -> binding.btnResetPassword.onLoad()
                }
            }
        }
    }

    private fun resetPassword() {
        val newPassword = binding.txtNewPassword.text?.trim().toString()
        if (!email.isNullOrEmpty()) {
            authViewModel.updatePassword(email!!, newPassword, accessToken?: "")
        } else {
            showToast(getString(R.string.enter_your_email))
        }
    }
}
