package com.example.app_vpn.ui.auth.signup

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.app_vpn.R
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.databinding.ActivitySignUpBinding
import com.example.app_vpn.ui.auth.login.LoginActivity
import com.example.app_vpn.ui.auth.signup.verify.VerificationActivity
import com.example.app_vpn.ui.viewmodel.AuthViewModel
import com.example.app_vpn.ui.viewmodel.MailViewModel
import com.example.app_vpn.util.enable
import com.example.app_vpn.util.handleApiError
import com.example.app_vpn.util.hideKeyboard
import com.example.app_vpn.util.isValidEmail
import com.example.app_vpn.util.isValidUsername
import com.example.app_vpn.util.startNewActivity
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val mailViewModel by viewModels<MailViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()

    private lateinit var btnSignUp: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btnSignUp = binding.btnSignUp
        bindProgressButton(btnSignUp)
        btnSignUp.apply {
            enable(false)
            attachTextChangeAnimator()
        }

        binding.btnSignUp.setOnClickListener {
            val email = binding.txtEmail.text.toString().trim()
            val username = binding.txtUsername.text.toString().trim()
            if (!email.isValidEmail()) {
                binding.txtEmail.error = "Email is not valid"
            }
            if (!username.isValidUsername()) {
                Log.d("mytag", username)
                binding.txtUsername.error = getString(R.string.usernameValidateError)
            }
            if (email.isValidEmail() && username.length >= 6 && username.isValidUsername()) {
                isValidUsernameEmail()
            }
            hideKeyboard(this, it)
        }

        binding.txtPassword.addTextChangedListener {
            binding.btnSignUp.enable(
                binding.txtUsername.text.toString().trim()
                    .isNotEmpty() && binding.txtEmail.text.toString().trim()
                    .isNotEmpty() && it.toString().trim().length >= 6
            )
        }

        authViewModel.isValidUsernameEmailResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    btnSignUp.hideProgress(R.string.sign_up)
                    val isValidReponse = response.value
                    when (isValidReponse.isSuccess) {
                        true -> {
                            val email = binding.txtEmail.text.toString().trim()
                            mailViewModel.sendVerifyCode(email)
                        }

                        false -> {
                            if (isValidReponse.message == "Username is already exist") {
                                binding.txtUsername.error = "Username is already taken"
                            }
                            if (isValidReponse.message == "Email is already exist") {
                                binding.txtEmail.error = "Email is already taken"
                            }
                        }
                    }
                }

                is Resource.Failure -> {
                    btnSignUp.hideProgress(R.string.sign_up)
                    handleApiError(response)
                }

                else -> {
                    btnSignUp.showProgress {
                        buttonTextRes = R.string.loading
                        progressColor = Color.WHITE
                    }
                }
            }
        }

        mailViewModel.sendVerifyResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    btnSignUp.hideProgress(R.string.sign_up)
                    val username = binding.txtUsername.text.toString().trim()
                    val email = binding.txtEmail.text.toString().trim()
                    val password = binding.txtPassword.text.toString().trim()

                    val bundle = Bundle().apply {
                        putString("username", username)
                        putString("email", email)
                        putString("password", password)
                    }
                    val intent = Intent(this, VerificationActivity::class.java)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }

                is Resource.Failure -> {
                    btnSignUp.hideProgress(R.string.sign_up)
                    handleApiError(response)
                }

                else -> {
                    btnSignUp.showProgress {
                        buttonTextRes = R.string.loading
                        progressColor = Color.WHITE
                    }
                }
            }
        }

        binding.txtSignIn.setOnClickListener {
            startNewActivity(LoginActivity::class.java)
        }
    }

    private fun isValidUsernameEmail() {
        val username = binding.txtUsername.text.toString().trim()
        val email = binding.txtEmail.text.toString().trim()
        authViewModel.isValidUsernameEmailResponse(username, email)
    }
}