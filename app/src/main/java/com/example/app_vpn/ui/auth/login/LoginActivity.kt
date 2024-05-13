package com.example.app_vpn.ui.auth.login

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.app_vpn.R
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.databinding.ActivityLoginBinding
import com.example.app_vpn.ui.MainActivity
import com.example.app_vpn.ui.auth.signup.SignUpActivity
import com.example.app_vpn.ui.viewmodel.AuthViewModel
import com.example.app_vpn.util.enable
import com.example.app_vpn.util.handleApiError
import com.example.app_vpn.util.snackBar
import com.example.app_vpn.util.startNewActivity
import com.example.app_vpn.util.visible
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


const val TAG = "MYTAG_CHECK"

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val authViewModel by viewModels<AuthViewModel>()

    private lateinit var binding : ActivityLoginBinding

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.progressBarLogin.visible(false)
        binding.btnSignIn.enable(false)

        //kiểm tra dữ liệu trả về có token hay chưa
        authViewModel.loginResponse.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    val loginResult = response.value
                    when (loginResult.isSuccessful) {
                        true -> {
                            lifecycleScope.launch {
                                authViewModel.saveAccessTokens(
                                    loginResult.accessToken!!,
                                    loginResult.refreshToken!!
                                )
                                startNewActivity(MainActivity::class.java)
                            }
                        }
                        false -> {
                            binding.root.snackBar(loginResult.message)
                        }
                    }
                }
                is Resource.Failure -> {
                    Log.d(TAG, response.toString())
                    handleApiError(response) { logIn() }
                }
                is Resource.Loading -> {
                    binding.progressBarLogin.visible(true)
                }
            }
        })

        binding.txtPassword.addTextChangedListener {
            val username = binding.txtUsername.text.toString().trim()
            binding.btnSignIn.enable(username.isNotEmpty() && it.toString().length > 5 && it.toString().isNotEmpty())
        }

        binding.btnSignIn.setOnClickListener {
            logIn()
        }

        binding.txtSignUp.setOnClickListener {
            startNewActivity(SignUpActivity::class.java)
        }
    }


    private fun googleLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    private fun logIn() {
        val TAG_LOGIN = "MYTAG_LOGIN"
        val username = binding.txtUsername.text.toString().trim()
        val password = binding.txtPassword.text.toString().trim()
        Log.d(TAG_LOGIN, username + password)
        authViewModel.login(username, password)
    }
}