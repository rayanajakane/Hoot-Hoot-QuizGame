package com.example.polyquiz.auth.domain

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.polyquiz.constants.AuthErrorText
import com.example.vanillaprototype.socket.SocketHandler
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    private var user: FirebaseUser? = null

    private val TAG = "EmailAuthActivity"

    init {
        checkAuthStatus()
        if (authState.value == AuthState.Authenticated) {
            SocketHandler.connect()
        }
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
            _authState.value = AuthState.Error(AuthErrorText.EMPTY_USERNAME_PASSWORD.value)
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword("$username@polyQuiz.com", password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user = task.result.user
                    _authState.value = AuthState.Authenticated
                    SocketHandler.connect()
                    Log.d(TAG, "signInWithEmail:success")
                } else {
                    handleAuthError(task)
                }
            }
    }

    fun signUp(username: String, password: String) {
        // TODO: Replace spaces? (or simply forbid them?)
        if (username.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error(AuthErrorText.EMPTY_USERNAME_PASSWORD.value)
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
                            SocketHandler.connect()
                        }
                        Log.d(TAG, "createUserWithEmail:success")
                    }
                } else {
                    handleAuthError(task)
                }
            }
    }

    fun signOut() {
        auth.signOut()
        resetAuthState()
        SocketHandler.disconnect()
    }

    fun resetAuthState() {
        // This is to avoid the bug where an error state transfers from login to signup page.
        _authState.value = AuthState.Unauthenticated
    }

    private fun handleAuthError(task: Task<AuthResult>) {
        val errorMessage = try {
            throw task.exception!!
        } catch(e: FirebaseAuthUserCollisionException) {
            AuthErrorText.USER_ALREADY_EXISTS.value
        } catch(e: FirebaseAuthWeakPasswordException) {
            AuthErrorText.PASSWORD_TOO_SHORT.value
        } catch(e: FirebaseAuthInvalidCredentialsException) {
           AuthErrorText.INVALID_USERNAME_PASSWORD.value
        } catch (e: Exception) {
            e.message ?: AuthErrorText.OTHER_ERROR.value;
        }
        _authState.value = AuthState.Error(errorMessage)
        Log.w(TAG, "createUserWithEmail:failure", task.exception)
    }

}

sealed class AuthState {
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
