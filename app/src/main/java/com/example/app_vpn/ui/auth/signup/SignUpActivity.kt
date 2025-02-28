package com.example.app_vpn.ui.auth.signup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
import com.example.app_vpn.databinding.ActivitySignUpBinding
import com.example.app_vpn.ui.BaseActivity
import com.example.app_vpn.ui.MainActivity
import com.example.app_vpn.ui.auth.login.LoginActivity
import com.example.app_vpn.ui.auth.login.TAG
import com.example.app_vpn.ui.auth.verify.VerificationActivity
import com.example.app_vpn.ui.viewmodel.AuthViewModel
import com.example.app_vpn.util.*
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpActivity : BaseActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val authViewModel by viewModels<AuthViewModel>()
    private lateinit var btnSignUp: Button
    private lateinit var btnSignUpWithGoogle: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUi()
        observeViewModel()
    }

    private fun setupUi() = with(binding) {
        enableEdgeToEdge()
        setupInsets()
        setupValidation()
        setupInputListener()
        setupNavigation()
        setupSignupButton()
        setupSignUpWithGoogleButton()
    }




    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupValidation() {
        binding.iplyEmail.isValid(
            editText = binding.txtEmail,
            invalidHelperText = getString(R.string.emailValidateError),
            validate = { this.isValidEmail() }
        )

        binding.iplyPassword.isValid(
            editText = binding.txtPassword,
            submitButton = binding.btnSignUp,
            invalidHelperText = getString(R.string.passwordValidateError),
            validate = { this.isValidPassword() }
        )
    }

    private fun setupInputListener() {
        binding.txtPassword.addTextChangedListener {
            binding.btnSignUp.enable(
                binding.txtEmail.text.toString().isValidEmail() && it.toString().isValidPassword()
            )
        }
    }

    private fun setupNavigation() {
        binding.txtSignIn.setOnClickListener { startNewActivity(LoginActivity::class.java) }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupSignupButton() {
        btnSignUp = binding.btnSignUp
        bindProgressButton(btnSignUp)
        btnSignUp.setUp()

        btnSignUp.setOnClickListener {
            registerUser()
            btnSignUp.showProgress()
            hideKeyboard(this, it)
        }
    }

    private fun registerUser() {
        val email = binding.txtEmail.text.toString().trim()
        val password = binding.txtPassword.text.toString().trim()

        if (email.isValidEmail() && password.isValidPassword()) {
            authViewModel.registerWithEmailPassword(email, password)
        }
    }

    private fun setupSignUpWithGoogleButton() {
        btnSignUpWithGoogle = binding.btnSignUpWithGoogle

        btnSignUpWithGoogle.setOnClickListener {
            authViewModel.loginWithGoogle(this)
        }
    }

    private fun navigateToVerification() {
        val intent = Intent(this, VerificationActivity::class.java).apply {
            putExtra("email", binding.txtEmail.text.toString().trim())
            putExtra("password", binding.txtPassword.text.toString().trim())
        }
        startActivity(intent)
    }




    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { authViewModel.registerResponse.collect { handleRegisterResponse(it) } }
                launch { authViewModel.loginResponse.collect { handleLoginResponse(it) } }
            }
        }
    }



    private fun handleRegisterResponse(resource: Resource<UserInfo>) {
        when (resource) {
            is Resource.Loading -> {}
            is Resource.Success -> {
                showToast(this.getString(R.string.login_success))
                btnSignUp.hideProgress()
                btnSignUp.text = this.getString(R.string.sign_up)
                handleRegisterSuccess()
            }
            is Resource.Error -> {
                btnSignUp.hideProgress()
                if(resource.error.message == "email_already_taken") {
                    binding.txtEmail.error = getString(R.string.email_already_taken)
                }
                showToast(resource.error.message ?: getString(R.string.unknow_error))
            }
        }
    }

    private fun handleLoginResponse(resource: Resource<UserInfo>) {
        when (resource) {
            is Resource.Loading -> {}
            is Resource.Success -> {
                showToast("Đăng nhập thành công: ${resource.data}")
                startNewActivity(MainActivity::class.java)
            }
            is Resource.Error -> showToast("Lỗi đăng nhập: ${resource.error.message}")
        }
    }

    private fun handleRegisterSuccess() {
        startActivity(Intent(this, VerificationActivity::class.java))
    }

}
