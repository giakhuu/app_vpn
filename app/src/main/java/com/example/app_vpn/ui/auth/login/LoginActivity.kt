package com.example.app_vpn.ui.auth.login

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.app_vpn.R
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.preferences.PreferenceManager
import com.example.app_vpn.databinding.ActivityLoginBinding
import com.example.app_vpn.ui.BaseActivity
import com.example.app_vpn.ui.MainActivity
import com.example.app_vpn.ui.auth.resetpw.ForgotPasswordActivity
import com.example.app_vpn.ui.auth.signup.SignUpActivity
import com.example.app_vpn.ui.viewmodel.AuthViewModel
import com.example.app_vpn.util.enable
import com.example.app_vpn.util.handleApiError
import com.example.app_vpn.util.hideKeyboard
import com.example.app_vpn.util.isValidPassword
import com.example.app_vpn.util.onDone
import com.example.app_vpn.util.onLoad
import com.example.app_vpn.util.setUp
import com.example.app_vpn.util.startNewActivity
import com.github.razir.progressbutton.bindProgressButton
import com.google.android.material.switchmaterial.SwitchMaterial
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale


const val TAG = "MYTAG_CHECK"

@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    private val authViewModel by viewModels<AuthViewModel>()

    private lateinit var binding : ActivityLoginBinding
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var btnSignIn : Button
    private lateinit var switchLang: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        updateUI(this)

        preferenceManager = PreferenceManager(this)

        switchLang = binding.switchLang
        switchLang.apply {
            isChecked = preferenceManager.getLanguage() == "vi"
            setOnCheckedChangeListener { _, isChecked ->
                val newLanguage = if (isChecked) "vi" else "en"
                preferenceManager.saveLanguage(newLanguage)
                recreate()
            }
        }


        btnSignIn = binding.btnSignIn
        bindProgressButton(btnSignIn)
        btnSignIn.setUp()

        //kiểm tra dữ liệu trả về có token hay chưa
        authViewModel.loginResponse.observe(this) {response ->
            when (response) {
                is Resource.Success -> {
                    btnSignIn.onDone(getString(R.string.sign_in))
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
                                    helperText = context.getString(R.string.user_doesn_t_exist)
                                    setHelperTextColor(ColorStateList.valueOf(resources.getColor(R.color.red)))
                                }
                            }
                            else if (loginResult.message.contains("password")) {
                                binding.txtInputPassword.apply {
                                    binding.txtInputPassword.isHelperTextEnabled = true
                                    helperText = context.getString(R.string.incorrect_password)
                                    setHelperTextColor(ColorStateList.valueOf(resources.getColor(R.color.red)))
                                }
                            }
                        }
                    }
                }

                is Resource.Failure -> {
                    btnSignIn.onDone(getString(R.string.sign_in))
                    handleApiError(response) { logIn() }
                }

                is Resource.Loading -> {
                    btnSignIn.onLoad()
                }
            }
        }

        binding.txtPassword.addTextChangedListener {
            val username = binding.txtUsername.text.toString().trim()
            binding.btnSignIn.enable(username.isNotEmpty() && it.toString().isValidPassword())
        }

        binding.btnSignIn.setOnClickListener {
            hideKeyboard(this, it)
            logIn()
        }

        binding.txtSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.txtForgotPw.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun updateUI(context: Context) {
        binding.txtTitle.text = context.getString(R.string.login_to_your_account)
        binding.txtUsername.hint = context.getString(R.string.enter_your_username)
    }

    private fun logIn() {
        val username = binding.txtUsername.text.toString().trim()
        val password = binding.txtPassword.text.toString().trim()
        authViewModel.login(username, password)
    }

    private fun updateLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}