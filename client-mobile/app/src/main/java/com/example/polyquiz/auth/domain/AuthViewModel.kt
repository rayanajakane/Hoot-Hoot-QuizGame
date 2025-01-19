package com.example.polyquiz.auth.domain

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    private var user: FirebaseUser? = null

    private val TAG = "EmailAuthActivity"
    // TODO (Move this value into translator)
    private val emptyEmailPassword: String = "Email or password can't be empty"

    init {
        checkAuthStatus()
    }

    fun getUsername(): String {
        return auth.currentUser?.displayName ?: ""
    }

    private fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun signIn(username: String, password: String) {
        if (username.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error(emptyEmailPassword)
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword("$username@polyQuiz.com", password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user = task.result.user
                    _authState.value = AuthState.Authenticated
                    Log.d(TAG, "signInWithEmail:success")
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                }
            }
    }

    fun signUp(username: String, password: String) {
        if (username.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error(emptyEmailPassword)
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword("$username@polyQuiz.com", password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user = task.result.user
                    val displayNameUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                    user?.updateProfile(displayNameUpdate)?.addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            _authState.value = AuthState.Authenticated
                        }
                        Log.d(TAG, "createUserWithEmail:success")
                    }
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

}

sealed class AuthState {
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
