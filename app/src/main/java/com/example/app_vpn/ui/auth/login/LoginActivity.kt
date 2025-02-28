package com.example.app_vpn.ui.auth.login

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import com.example.app_vpn.util.hideKeyboard
import com.example.app_vpn.util.isValidPassword
import com.example.app_vpn.util.onDone
import com.example.app_vpn.util.onLoad
import com.example.app_vpn.util.setUp
import com.example.app_vpn.util.showToast
import com.example.app_vpn.util.startNewActivity
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.launch


const val TAG = "MYTAG_CHECK"
@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    private val authViewModel by viewModels<AuthViewModel>()
    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() = with(binding) {
        enableEdgeToEdge()
        setupInsets()
        setupLanguageSwitch()
        setupSignInButton()
        setupInputListeners()
        setupNavigation()
    }

    private fun ActivityLoginBinding.setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun ActivityLoginBinding.setupLanguageSwitch() {
        switchLang.isChecked = preferenceManager.getLanguage() == "vi"
        switchLang.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.saveLanguage(if (isChecked) "vi" else "en")
            recreate()
        }
    }

    private fun ActivityLoginBinding.setupSignInButton() {
        bindProgressButton(btnSignIn)
        btnSignIn.setUp()
        btnSignIn.setOnClickListener {
            btnSignIn.showProgress()
            hideKeyboard(this@LoginActivity, it)
            logIn()
        }
    }

    private fun ActivityLoginBinding.setupInputListeners() {
        txtPassword.addTextChangedListener {
            btnSignIn.enable(txtUsername.text.toString().isNotBlank() && it.toString().isValidPassword())
        }
    }

    private fun ActivityLoginBinding.setupNavigation() {
        txtSignUp.setOnClickListener { startActivity(Intent(this@LoginActivity, SignUpActivity::class.java)) }
        txtForgotPw.setOnClickListener { startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java)) }
    }

    private fun logIn() {
        authViewModel.loginWithEmail(
            binding.txtUsername.text.toString().trim(),
            binding.txtPassword.text.toString().trim()
        )
    }

    private fun TextInputLayout.showError(message: String) {
        isHelperTextEnabled = true
        helperText = message
        setHelperTextColor(ColorStateList.valueOf(resources.getColor(R.color.red)))
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { authViewModel.loginResponse.collect { handleLoginResponse(it) } }
            }
        }
    }

    private fun handleLoginResponse(resource: Resource<UserInfo>) {
        when (resource) {
            is Resource.Loading -> {}
            is Resource.Success -> {
                handleLoginSuccess()
            }
            is Resource.Error ->{
                if (resource.error is AuthRestException) {
                    handleLoginFailed(resource.error) // Chỉ truyền nếu là AuthRestException
                } else {
                    showToast("Lỗi đăng nhập: ${resource.error.message ?: "Không xác định"}")
                }
            }
        }
    }

    private fun handleLoginSuccess() {
        startNewActivity(MainActivity::class.java)
    }

    private fun handleLoginFailed(error: AuthRestException) = with(binding) {
        Log.d(TAG, "handleLoginFailed: ${error.errorCode}")
        binding.txtInputPassword.isHelperTextEnabled = false
        btnSignIn.hideProgress(getString(R.string.sign_in))
        val errorMessage = when(error.errorCode.toString()) {
            "InvalidCredentials"  ->{
                binding.txtInputPassword.apply {
                    binding.txtInputPassword.isHelperTextEnabled = true
                    helperText = context.getString(R.string.email_login_failed)
                    setHelperTextColor(ColorStateList.valueOf(resources.getColor(R.color.red)))
                }
                getString(R.string.email_login_failed)
            }
            "EmailNotConfirmed" -> {
                binding.txtInputPassword.apply {
                    binding.txtInputPassword.isHelperTextEnabled = true
                    helperText = context.getString(R.string.email_not_confirmed)
                    setHelperTextColor(ColorStateList.valueOf(resources.getColor(R.color.red)))
                }
                getString(R.string.email_not_confirmed)
            }
            else -> error.errorCode.toString()
        }
        showToast(errorMessage)
    }
}

