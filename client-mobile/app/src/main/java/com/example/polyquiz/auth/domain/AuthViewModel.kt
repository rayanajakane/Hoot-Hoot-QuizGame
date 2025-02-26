package com.example.polyquiz.auth.domain

import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.polyquiz.constants.AuthErrorText
import com.example.vanillaprototype.socket.SocketHandler
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    private var user: FirebaseUser? = null
    private val database = Firebase.database

    private val TAG = "EmailAuthActivity"

    // Reactive programming in Kotlin :
    // https://codersee.com/reactive-programming-in-kotlin-a-step-by-step-guide/
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> get() = _email

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> get() = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> get() = _password

    private val _emailError = MutableStateFlow("")
    val emailError: StateFlow<String> get() = _emailError

    private val _usernameError = MutableStateFlow("")
    val usernameError: StateFlow<String> get() = _usernameError

    private val _passwordError = MutableStateFlow("")
    val passwordError: StateFlow<String> get() = _passwordError

    init {
        checkAuthStatus()
        if (authState.value == AuthState.Authenticated) {
            signOut()
        }
    }

    fun getUserDatabaseRef(uid: String): DatabaseReference {
        return database.getReference("users/${uid}")
    }

    fun getUsernameDatabaseRef(username: String) : DatabaseReference {
        return database.getReference("usernames/${username}")
    }

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
        validateEmail(newEmail)
    }

    fun updateUsername(newUsername : String) {
        _username.value = newUsername
        validateUsername(newUsername)
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        validatePassword(newPassword)
    }

    fun resetSignUpFields() {
        _email.value = ""
        _username.value = ""
        _password.value = ""
        _emailError.value = ""
        _usernameError.value = ""
        _passwordError.value = ""
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

    fun signIn(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error(AuthErrorText.EMPTY_USERNAME_PASSWORD.value)
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userRef = task.result.user?.let { this.getUserDatabaseRef(it.uid) }
                    userRef?.child("isOnline")?.get()?.addOnSuccessListener { dataSnapshot: DataSnapshot ->
                        val isOnline: Boolean = dataSnapshot.value as Boolean
                        if (isOnline) {
                            auth.signOut()
                            _authState.value = AuthState.Error(AuthErrorText.ALREADY_ONLINE.value)
                            return@addOnSuccessListener
                        }
                        userRef.child("isOnline").setValue(true)
                        userRef.child("isOnline").onDisconnect().setValue(false)
                        user = task.result.user
                        _authState.value = AuthState.Authenticated
                        SocketHandler.connect()
                        Log.d(TAG, "signInWithEmail:success")
                    }
                } else {
                    handleAuthError(task)
                }
            }
    }

     fun signUp(email: String, username: String, password: String) {
        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error(AuthErrorText.EMPTY_USERNAME_PASSWORD.value)
            return
        }
        // TODO : Add email input view!
        if (emailError.value.isNotEmpty() || passwordError.value.isNotEmpty() || usernameError.value.isNotEmpty()) {
            _authState.value = AuthState.Error(AuthErrorText.INVALID_USERNAME_PASSWORD.value)
            return
        }
         // TODO: Replace spaces? (or simply forbid them?)
         val usernameRef = getUsernameDatabaseRef(username.lowercase())
         usernameRef.get().addOnSuccessListener { databaseSnapshot: DataSnapshot ->
             if(databaseSnapshot.exists()) {
                 // TODO : Make new error text
                 _authState.value = AuthState.Error("Ce nom d'utilisateur est déjà pris.")
                 Log.e(TAG, "Nom d'utilisateur déjà pris.")
             } else {
                 _authState.value = AuthState.Loading
                 auth.createUserWithEmailAndPassword(email, password)
                     .addOnCompleteListener { task ->
                         if (task.isSuccessful) {
                             user = task.result.user
                             val displayNameUpdate = UserProfileChangeRequest.Builder()
                                 .setDisplayName(username)
                                 .build()
                             user?.updateProfile(displayNameUpdate)?.addOnCompleteListener { updateTask ->
                                 if (updateTask.isSuccessful) {
                                     val userRef = task.result.user?.let { this.getUserDatabaseRef(it.uid) }
                                     userRef?.child("isOnline")?.setValue(true)
                                     userRef?.child("isOnline")?.onDisconnect()?.setValue(false)
                                     usernameRef.setValue(username.lowercase())
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
         }
     }


    fun signOut() {
        if (user != null) {
            val userRef = this.getUserDatabaseRef(user!!.uid)
            userRef.child("isOnline").setValue(false)
        }
        auth.signOut()
        resetSignUpFields()
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
            e.message ?: AuthErrorText.OTHER_ERROR.value
        }
        _authState.value = AuthState.Error(errorMessage)
        Log.w(TAG, "createUserWithEmail:failure", task.exception)
    }

    private fun validatePassword(password: String) {
        if(password.length < 8) {
            _passwordError.value = AuthErrorText.PASSWORD_TOO_SHORT.value
        } else {
            _passwordError.value = ""
        }
    }

    private fun validateUsername(username: String) {
        if(username.matches(".*[^A-Za-z0-9_].*".toRegex())) {
            _usernameError.value = AuthErrorText.SPECIAL_CHAR_USERNAME.value
        } else if(username.length < 3) {
            _usernameError.value = AuthErrorText.SHORT_USERNAME.value
        } else if(username.length > 20) {
            _usernameError.value = AuthErrorText.LONG_USERNAME.value
        } else {
            _usernameError.value = ""
        }
    }

    private fun validateEmail(email: String) {
        if(email.isBlank() || !isValidEmail(email)) {
            _emailError.value = AuthErrorText.INVALID_EMAIL.value
        } else {
            _emailError.value = ""
        }
    }

    // https://developer.android.com/reference/android/util/Patterns#EMAIL_ADDRESS
    private fun isValidEmail(target: CharSequence) : Boolean {
        return if(TextUtils.isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

}

sealed class AuthState {
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
