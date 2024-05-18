package com.example.app_vpn.ui.auth.login

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.app_vpn.R
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.databinding.ActivityLoginBinding
import com.example.app_vpn.ui.MainActivity
import com.example.app_vpn.ui.auth.signup.SignUpActivity
import com.example.app_vpn.ui.viewmodel.AuthViewModel
import com.example.app_vpn.util.enable
import com.example.app_vpn.util.handleApiError
import com.example.app_vpn.util.hideKeyboard
import com.example.app_vpn.util.startNewActivity
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


const val TAG = "MYTAG_CHECK"

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val authViewModel by viewModels<AuthViewModel>()

    private lateinit var binding : ActivityLoginBinding

    private lateinit var btnSignIn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        btnSignIn = binding.btnSignIn
        bindProgressButton(btnSignIn)
        btnSignIn.apply {
            attachTextChangeAnimator()
            enable(false)
        }

        //kiểm tra dữ liệu trả về có token hay chưa
        authViewModel.loginResponse.observe(this) {response ->
            when (response) {
                is Resource.Success -> {
                    btnSignIn.hideProgress(R.string.sign_in)
                    val loginResult = response.value
                    when (loginResult.isSuccessful) {
                        true -> {
                            lifecycleScope.launch {
                                authViewModel.apply {
                                    saveAccessTokens(
                                        loginResult.data.accessToken!!,
                                        loginResult.data.refreshToken!!
                                    )
                                    savePremiumKey(loginResult.data.premiumKey!!)
                                }
                                startNewActivity(MainActivity::class.java)
                            }
                        }
                        false -> {
                            binding.txtInputUsername.isHelperTextEnabled = false
                            binding.txtInputPassword.isHelperTextEnabled = false
                            if (loginResult.message.contains("User")) {
                                binding.txtInputUsername.apply {
                                    binding.txtInputUsername.isHelperTextEnabled = true
                                    helperText = loginResult.message
                                    setHelperTextColor(ColorStateList.valueOf(resources.getColor(R.color.red)))
                                }
                            }
                            else if (loginResult.message.contains("password")) {
                                binding.txtInputPassword.apply {
                                    binding.txtInputPassword.isHelperTextEnabled = true
                                    helperText = loginResult.message
                                    setHelperTextColor(ColorStateList.valueOf(resources.getColor(R.color.red)))
                                }
                            }
                        }
                    }
                }

                is Resource.Failure -> {
                    btnSignIn.hideProgress(R.string.sign_in)
                    handleApiError(response) { logIn() }
                }

                is Resource.Loading -> {
                    btnSignIn.showProgress {
                        buttonTextRes = R.string.loading
                        progressColor = Color.WHITE
                    }
                }
            }
        }

        binding.txtPassword.addTextChangedListener {
            val username = binding.txtUsername.text.toString().trim()
            binding.btnSignIn.enable(username.isNotEmpty() && it.toString().length > 5 && it.toString().isNotEmpty())
        }

        binding.btnSignIn.setOnClickListener {
            hideKeyboard(this, it)
            logIn()
        }

        binding.txtSignUp.setOnClickListener {
            startNewActivity(SignUpActivity::class.java)
        }

//        binding.txtForgotPw.setOnClickListener {
//            val intent = Intent(this, ForgotPasswordActivity::class.java)
//            startActivity(intent)
//        }
    }

    private fun logIn() {
        val username = binding.txtUsername.text.toString().trim()
        val password = binding.txtPassword.text.toString().trim()
        authViewModel.login(username, password)
    }
}