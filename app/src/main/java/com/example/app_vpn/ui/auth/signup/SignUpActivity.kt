package com.example.app_vpn.ui.auth.signup

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.databinding.ActivitySignUpBinding
import com.example.app_vpn.ui.auth.login.LoginActivity
import com.example.app_vpn.ui.auth.signup.verify.VerificationActivity
import com.example.app_vpn.ui.viewmodel.AuthViewModel
import com.example.app_vpn.ui.viewmodel.MailViewModel
import com.example.app_vpn.util.enable
import com.example.app_vpn.util.handleApiError
import com.example.app_vpn.util.snackBar
import com.example.app_vpn.util.startNewActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val mailViewModel by viewModels<MailViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()

    //    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignUp.setOnClickListener {
            isValidUsernameEmail()
        }

        authViewModel.isValidUsernameEmailResponse.observe(this, Observer {response ->
            when (response) {
                is Resource.Success -> {
                    val isValidReponse = response.value
                    when (isValidReponse.isSuccess) {
                        true -> {
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

                            mailViewModel.sendVerifyCode(email)
                        }
                        false -> {
                            binding.root.snackBar(isValidReponse.message)
                        }
                    }
                }
                is Resource.Failure -> {
                    handleApiError(response)
                }
                else -> {

                }
            }
        })


        binding.txtSignIn.setOnClickListener {
            startNewActivity(LoginActivity::class.java)
        }

        binding.txtPassword.addTextChangedListener {
            val username = binding.txtUsername.text.toString().trim()
            val email = binding.txtEmail.text.toString().trim()
            binding.btnSignUp.enable(
                username.isNotEmpty() && email.isNotEmpty() && it.toString().trim().isNotEmpty()
            )
        }
    }

    private fun isValidUsernameEmail() {
        val username = binding.txtUsername.text.toString().trim()
        val email = binding.txtEmail.text.toString().trim()
        authViewModel.isValidUsernameEmailResponse(username, email)
    }
}