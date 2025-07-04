package com.example.app_vpn.data.repository.repoImpl

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.app_vpn.R
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.util.REDIRECT_URL
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient
) {

    suspend fun loginWithGoogle(context: Context): Resource<UserInfo> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val randNonce = UUID.randomUUID().toString()
            val hashNonce = randNonce.toByteArray().let {
                MessageDigest.getInstance("SHA-256").digest(it).joinToString("") { byte -> "%02x".format(byte) }
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.google_web_client_id))
                .setNonce(hashNonce)
                .build()

            val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Lấy thông tin từ CredentialManager
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )
                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                // Gọi đăng nhập bằng IDToken (Google)
                supabase.auth.signInWith(IDToken) {
                    idToken = googleIdToken
                    provider = Google
                    nonce = randNonce
                }
                val session = supabase.auth.currentSessionOrNull()
                if (session?.user != null) {
                    Resource.Success(session.user)
                } else {
                    Resource.Error(Exception("login_failed_session_not_found"))
                }
            } else {
                Resource.Error(Exception("Device version not supported"))
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }


    suspend fun registerWithEmailPassword(email: String, password: String): Resource<UserInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val response = supabase.auth.signUpWith(Email, redirectUrl = "${REDIRECT_URL}auth/login") {
                    this.email = email
                    this.password = password
                }
                // Nếu identities rỗng tức là email đã được sử dụng
                if (response?.identities != null && response.identities!!.isEmpty()) {
                    Resource.Error(Exception("email_already_taken"))
                } else {
                    Resource.Success(response)
                }
            } catch (e: Exception) {
                Log.d("registerWithEmailPassword", ": $e")
                Resource.Error(e)
            }
        }
    }

    /**
     * Đăng nhập với Email & Password
     */
    suspend fun loginWithEmail(email: String, password: String): Resource<UserInfo> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                val session = supabase.auth.currentSessionOrNull()
                if (session?.user != null) {
                    Resource.Success(session.user)
                } else {
                    Resource.Error(Exception("login_failed_session_not_found"))
                }
            } catch (e: AuthRestException) {
                Resource.Error(e)
            }
        }
    }

    /**
     * Reset mật khẩu bằng email
     */
    suspend fun resetPassword(email: String): Resource<Nothing> {
        return withContext(Dispatchers.IO) {
            try {
                val encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8.toString())
                val url = "${REDIRECT_URL}/auth/resetpassword?email=$encodedEmail"
                supabase.auth.resetPasswordForEmail(email, redirectUrl = url)
                Resource.Success(null)
            } catch (e: AuthRestException) {
                Resource.Error(e)
            }
        }
    }

    /**
     * Cập nhật mật khẩu của user.
     * Lưu ý: Ở đây ta import token, update user, sau đó signOut.
     */
    suspend fun updatePassword(nEmail: String?, newPassword: String, accessToken: String): Resource<Nothing> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.auth.importAuthToken(accessToken)
                supabase.auth.updateUser {
                    email = nEmail
                    password = newPassword
                }
                supabase.auth.signOut()
                Resource.Success(null)
            } catch (e: AuthRestException) {
                Resource.Error(e)
            }
        }
    }
}