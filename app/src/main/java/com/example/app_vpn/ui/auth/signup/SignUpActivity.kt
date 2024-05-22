package com.example.app_vpn.ui.auth.signup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.app_vpn.R
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.databinding.ActivitySignUpBinding
import com.example.app_vpn.ui.auth.login.LoginActivity
import com.example.app_vpn.ui.auth.verify.VerificationActivity
import com.example.app_vpn.ui.viewmodel.AuthViewModel
import com.example.app_vpn.ui.viewmodel.MailViewModel
import com.example.app_vpn.util.enable
import com.example.app_vpn.util.handleApiError
import com.example.app_vpn.util.hideKeyboard
import com.example.app_vpn.util.isValid
import com.example.app_vpn.util.isValidEmail
import com.example.app_vpn.util.isValidPassword
import com.example.app_vpn.util.isValidUsername
import com.example.app_vpn.util.onDone
import com.example.app_vpn.util.onLoad
import com.example.app_vpn.util.setUp
import com.example.app_vpn.util.startNewActivity
import com.github.razir.progressbutton.bindProgressButton
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

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnSignUp = binding.btnSignUp
        bindProgressButton(btnSignUp)
        btnSignUp.setUp()

        binding.iplyUsername.isValid(
            editText = binding.txtUsername,
            invalidHelperText = getString(R.string.usernameValidateError),
            validate = {this.isValidUsername()}
        )

        binding.iplyEmail.isValid(
            editText = binding.txtEmail,
            invalidHelperText = getString(R.string.emailValidateError),
            validate = {this.isValidEmail()}
        )

        binding.iplyPassword.isValid(
            editText = binding.txtPassword,
            submitButton = binding.btnSignUp,
            invalidHelperText = getString(R.string.passwordValidateError),
            validate = {this.isValidPassword()}
        )

        binding.btnSignUp.setOnClickListener {
            val email = binding.txtEmail.text.toString().trim()
            val username = binding.txtUsername.text.toString().trim()
            if (!email.isValidEmail()) {
                binding.txtEmail.error = getString(R.string.emailValidateError)
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
                binding.txtUsername.text.toString().isValidUsername()
                        && binding.txtEmail.text.toString().isValidEmail()
                        && it.toString().isValidPassword()
            )
        }

        authViewModel.isUsernameEmailExist.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    btnSignUp.onDone(getString(R.string.sign_up))
                    val isValidResponse = response.value
                    when (isValidResponse.isSuccess) {
                        true -> {
                            val email = binding.txtEmail.text.toString().trim()
                            mailViewModel.sendVerifyCode(email)
                        }
                        false -> {
                            if (isValidResponse.message.contains("Username")) {
                                binding.txtUsername.error = "Username is already taken"
                            }
                            if (isValidResponse.message.contains("Email")) {
                                binding.txtEmail.error = "Email is already taken"
                            }
                        }
                    }
                }

                is Resource.Failure -> {
                    btnSignUp.onDone(getString(R.string.sign_up))
                    handleApiError(response)
                }

                else -> {
                    btnSignUp.onLoad()
                }
            }
        }

        mailViewModel.sendVerifyResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    btnSignUp.onDone(getString(R.string.sign_up))
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
                    btnSignUp.onDone(getString(R.string.sign_up))
                    handleApiError(response)
                }

                else -> {
                    btnSignUp.onLoad()
                }
            }
        }

        binding.txtSignIn.setOnClickListener {
            startNewActivity(LoginActivity::class.java)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun isValidUsernameEmail() {
        val username = binding.txtUsername.text.toString().trim()
        val email = binding.txtEmail.text.toString().trim()
        authViewModel.isUsernameEmailExist(username, email)
    }
}