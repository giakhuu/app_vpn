package com.example.app_vpn.data.network

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseHandler @Inject constructor() {
    // firebase file
    fun downloadFileFromFirebase(
        fileUrl: String,
        localFile: File,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storage = Firebase.storage
        val fileRef = storage.getReferenceFromUrl(fileUrl)

        fileRef.getFile(localFile).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    fun signIn(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    task.exception?.let { onFailure(it) }
                }
            }
    }

    fun isUserLoggedIn(): Boolean {
        val auth = FirebaseAuth.getInstance()
        return auth.currentUser != null
    }
}
