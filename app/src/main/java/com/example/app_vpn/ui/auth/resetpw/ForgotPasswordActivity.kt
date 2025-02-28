package com.example.app_vpn.ui.auth.resetpw

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_vpn.R
import com.example.app_vpn.databinding.ActivityForgotPasswordBinding
import com.example.app_vpn.ui.BaseActivity
import com.example.app_vpn.ui.viewmodel.AuthViewModel
import com.example.app_vpn.util.hideKeyboard
import com.example.app_vpn.util.isValid
import com.example.app_vpn.util.isValidEmail
import com.example.app_vpn.util.setUp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private val authViewModel by viewModels<AuthViewModel>()

    private lateinit var btnSubmit : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUi()

    }
    private fun setupUi() = with(binding) {
        enableEdgeToEdge()
        setupInsets()
        setupSubmitButton()
        setupEmailInput()
        setupBackButton()
    }
    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupSubmitButton() {
        btnSubmit = binding.btnSubmit
        btnSubmit.setUp()

        btnSubmit.setOnClickListener {
            authViewModel.resetPassword(binding.txtForgotEmail.text.toString())
            hideKeyboard(this, it)
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupEmailInput() {
        binding.iplyForgotEmail.isValid(
            binding.txtForgotEmail,
            binding.btnSubmit,
            invalidHelperText = getString(R.string.emailValidateError),
            validate = {this.isValidEmail()}
        )
    }
}