package com.example.howviii.firebase

import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signInAnonymously(onResult: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser

        // Se já estiver logado anonimamente, não precisa logar de novo.
        if (currentUser != null) {
            onResult(true, currentUser.uid)
            return
        }

        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                onResult(true, result.user?.uid)
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.message)
            }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}
