package com.example.app_vpn.ui.auth.resetpw

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_vpn.R
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.databinding.ActivityResetPasswordBinding
import com.example.app_vpn.ui.BaseActivity
import com.example.app_vpn.ui.viewmodel.UserViewModel
import com.example.app_vpn.util.handleApiError
import com.example.app_vpn.util.isValid
import com.example.app_vpn.util.isValidPassword
import com.example.app_vpn.util.onDone
import com.example.app_vpn.util.onLoad
import com.example.app_vpn.util.setUp
import com.example.app_vpn.util.startNewActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var btnResetPassword: Button
    private lateinit var bundle: Bundle

    private val userViewModel by viewModels<UserViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bundle = intent.extras!!

        btnResetPassword = binding.btnResetPassword
        btnResetPassword.setUp()

        binding.btnResetBack.setOnClickListener {
            finish()
        }

        binding.iplyNewPassword.isValid(
            editText = binding.txtNewPassword,
            invalidHelperText = getString(R.string.passwordValidateError),
            validate = { this.isValidPassword() }
        )

        binding.iplyConfirmPassword.isValid(
            editText = binding.txtConfirmPassword,
            submitButton = btnResetPassword,
            invalidHelperText = getString(R.string.passwordConfirmValidateError),
            validate = {
                this.isPasswordConfirmMatch(
                    binding.txtNewPassword.text?.trim().toString()
                )
            }
        )

        btnResetPassword.setOnClickListener {
            resetPassword()
        }

        userViewModel.resetPasswordResponse.observe(this) {
            when (it) {
                is Resource.Success -> {
                    btnResetPassword.onDone(getString(R.string.reset_password))
                    startNewActivity(ResetPasswordSuccessActivity::class.java)
                }

                is Resource.Failure -> {
                    btnResetPassword.onDone(getString(R.string.reset_password))
                    handleApiError(it)
                }

                is Resource.Loading -> {
                    btnResetPassword.onLoad()
                }
            }
        }
    }

    private fun resetPassword() {
        val newPassword = binding.txtNewPassword.text?.trim().toString()
        val email = bundle.getString("email")
        userViewModel.resetPassword(email!!, newPassword)
    }

    private fun String.isPasswordConfirmMatch(password: String): Boolean {
        return this == password
    }
}