package com.example.polyquiz.auth.domain

import StringValue
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.polyquiz.R
import com.example.polyquiz.SnackbarController
import com.example.polyquiz.SnackbarEvent
import com.example.polyquiz.constants.FriendsEvents
import com.example.polyquiz.constants.GameEvents
import com.example.polyquiz.constants.PresetAvatar
import com.example.polyquiz.core.storage.ImageStorage
import com.example.polyquiz.ui.theme.Theme
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
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

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

    private val _avatarURL = MutableStateFlow(PresetAvatar.DEFAULT.value)
    val avatarURL: StateFlow<String> get() = _avatarURL

    private val _theme = MutableStateFlow(Theme.LIGHT)
    val theme: StateFlow<Theme> get() = _theme

    private val _profileUpdated = MutableStateFlow(false)
    val profileUpdated: StateFlow<Boolean> get() = _profileUpdated

    fun setProfileUpdated(updated: Boolean) {
        _profileUpdated.value = updated
        if(updated) {
            sendUpdateProfileSnackbar()
            _profileUpdated.value = false
        }
    }

    fun setTheme(theme: Theme) {
        _theme.value = theme
    }

    fun getTheme(): Theme {
        return _theme.value
    }

    init {
        checkAuthStatus()
        if (authState.value == AuthState.Authenticated) {
            signOut()
        }
    }

    private fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun getUserDatabaseRef(uid: String): DatabaseReference {
        return database.getReference("users/${uid}")
    }

    fun getUserConfigsDatabaseRef(): DatabaseReference {
        return database.getReference("users/${user?.uid}/configs")
    }

    fun getUsernameDatabaseRef(username: String): DatabaseReference {
        return database.getReference("usernames/${username}")
    }

    fun getUserDatabaseRef(): DatabaseReference {
        val userId = getUserId()
        return database.getReference("users/$userId")
    }

    fun getPurchasedAvatarsRef(): DatabaseReference {
        return getUserDatabaseRef().child("purchasedAvatars")
    }

    fun getPurchasedWallpapersRef(): DatabaseReference {
        return getUserDatabaseRef().child("purchasedWallpapers")
    }

    fun getPurchasedThemesRef(): DatabaseReference {
        return getUserDatabaseRef().child("purchasedThemes")
    }

    fun getCurrentWallpaperRef(): DatabaseReference {
        return getUserDatabaseRef().child("currentWallpaper")
    }

    fun updateEmail(newEmail: String, context: Context) {
        _email.value = newEmail
        validateEmail(newEmail, context)
    }

    fun setAndUpdateUsername(newUsername: String, context: Context) {
        _username.value = newUsername
        updateUsername(newUsername, context)
    }

    fun updateUsername(newUsername: String, context: Context) {
        validateUsername(newUsername, context)
    }

    fun updatePassword(newPassword: String, context: Context) {
        _password.value = newPassword
        validatePassword(newPassword, context)
    }

    fun getAvatarURL(): String {
        return _avatarURL.value
    }


    fun setAvatarToFirebaseAvatar() : String {
        val url = user!!.photoUrl.toString()
        _avatarURL.value = url
        return url
    }

    fun setAvatarUrl(url: String) {
        _avatarURL.value = url
        Log.d("Set avatar", "Set avatar url to $url")
    }

    fun resetSignUpFields() {
        _email.value = ""
        _username.value = ""
        _password.value = ""
        _emailError.value = ""
        _usernameError.value = ""
        _passwordError.value = ""
        _avatarURL.value = ""
        Log.d("Reset", "Reset sign up fields")
    }

    fun resetUsername() {
        _username.value = user?.displayName ?: ""
    }

    fun getUserId(): String {
        return auth.currentUser?.uid ?: ""
    }

    // Check username, if doesn't exist use callback to save
    private fun checkUsername(username: String, callback: (Boolean) -> Unit) {
        val usernameRef = getUsernameDatabaseRef(username)
        usernameRef.get().addOnSuccessListener { databaseSnapshot: DataSnapshot ->
            if(databaseSnapshot.exists()) {
                callback(false)
            } else {
                callback(true)
            }
        }.addOnFailureListener {
            Log.e("Update profile", "An error occured when getting from DB")
        }
    }

    fun updateUsername(username: String, callback: (Boolean) -> Unit) {
        val formattedUsername = username.trim()
        val oldUsername = getUsername()

        checkUsername(formattedUsername) { isUsernameValid ->
            if(isUsernameValid) {
                // Save display name
                val profileUpdates = userProfileChangeRequest {
                    displayName = formattedUsername
                }
                user!!.updateProfile(profileUpdates).addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        _username.value = formattedUsername
                        Log.d("Update profile", "Updated user's display name")
                        // Now save new username and delete old ref
                        val oldUsernameRef = getUsernameDatabaseRef(oldUsername.lowercase())
                        oldUsernameRef.removeValue().addOnCompleteListener { deleteUsernameTask ->
                            if (deleteUsernameTask.isSuccessful) {
                                Log.d("Update profile", "Deleted username from DB ${oldUsername.lowercase()}")
                            } else {
                                Log.e("Update profile", "Could not delete username from DB")
                            }
                        }

                        // Set new username value to DB
                        val newUsernameRef = getUsernameDatabaseRef(formattedUsername.lowercase())
                        newUsernameRef.setValue(formattedUsername.lowercase()).addOnSuccessListener {
                            Log.d("Update profile", "Saved ${formattedUsername.lowercase()} to DB")
                            callback(true)
                        }.addOnFailureListener {
                            Log.e("Update profile", "Could not save username to DB")
                            callback(false)
                        }
                    } else {
                        Log.e("Update profile", "Could not update user display name")
                        callback(false)
                    }
                }
            } else {
                viewModelScope.launch {
                    SnackbarController.sendEvent(
                        event = SnackbarEvent(
                            message = StringValue.StringResource(R.string.username_already_exists)
                        )
                    )
                }
            }
        }
    }

    fun updateAvatar(avatarURL: String) {
        val profileUpdates = userProfileChangeRequest {
            photoUri = Uri.parse(avatarURL)
        }

        user!!.updateProfile(profileUpdates).addOnCompleteListener { updateAvatarTask ->
            if(updateAvatarTask.isSuccessful) {
                _avatarURL.value = avatarURL
                Log.d("Update profile", "Updated user avatar url : $avatarURL")
            } else {
                Log.e("Update profile", "Could not update avatar url")
            }
        }
    }

    fun emitUpdates() {
        SocketHandler.getSocket().emit(FriendsEvents.UPDATE_DATA.value)

        val authorNameUpdate = UserIdName(id = user!!.uid, name = _username.value)
        Log.d("Update profile", "AuthorNameUpdate with ${_username.value}")
        val authorUpdateObject = JSONObject(Gson().toJson(authorNameUpdate))
        SocketHandler.getSocket()
            .emit(GameEvents.UPDATE_AUTHOR_NAME.value, authorUpdateObject)
    }

    fun getAvatarURLFromDB(callback: (String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return callback(null)
        val avatarRef = ImageStorage.getAvatarRef(uid)

        ImageStorage.getImageURL(avatarRef) { url ->
            callback(url)
        }
    }

    fun deleteUser() {
        val user = auth.currentUser
        if (user == null) {
            viewModelScope.launch {
                SnackbarController.sendEvent(
                    event = SnackbarEvent(
                        message = StringValue.StringResource(R.string.error_delete_user)
                    )
                )
            }

            Log.e("Delete user", "User is null. Could not delete user")
            return
        }

        // Delete user from DB
        val userRef = getUserDatabaseRef(user.uid)
        userRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Delete user", "Deleted user from DB")
            } else {
                Log.e("Delete user", "Could not delete user from DB")
            }
        }

        // Delete username from DB
        if (user.displayName?.isNotEmpty()!!) {
            val username = getUsername()
            val usernameRef = getUsernameDatabaseRef(getUsername().lowercase())
            usernameRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Delete user", "Deleted username from DB $username")
                } else {
                    Log.e("Delete user", "Could not delete username from DB")
                }
            }
        }

        // Delete avatar from storage
        ImageStorage.deleteAvatar(user.uid)

        val userId = user.uid;

        // Delete user from auth
        user.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                viewModelScope.launch {
                    SnackbarController.sendEvent(
                        event = SnackbarEvent(
                            message = StringValue.StringResource(R.string.delete_feedback)
                        )
                    )
                }
                SocketHandler.getSocket().emit(FriendsEvents.UPDATE_DATA.value)

                SocketHandler.getSocket().emit(FriendsEvents.USER_DELETED.value, userId)

                // Disconnect socket
                SocketHandler.disconnect()

                Log.d("Delete user", "Deleted user successfully")
            } else {
                viewModelScope.launch {
                    SnackbarController.sendEvent(
                        event = SnackbarEvent(
                            message = StringValue.StringResource(R.string.error_delete_user)
                        )
                    )
                }
                Log.e("Delete user", "Could not delete user from Auth")
            }
        }

        // Disconnect user from app
        auth.signOut()
        resetSignUpFields()
        resetAuthState()
    }

    fun updateUserProfile(url: String, username: String) {
        if (url.isEmpty() && username.isEmpty()) {
            Log.e("Save UserProfile", "Nothing to update")
            return
        }

        Log.d("Save UserProfile", "Called update profile")


        if (username.isEmpty() || usernameError.value.isNotEmpty()) {
            Log.e("Save UserProfile", "Invalid username")
            viewModelScope.launch {
                SnackbarController.sendEvent(
                    event = SnackbarEvent(
                        message = StringValue.StringResource(R.string.invalid_username)
                    )
                )
            }
            return
        }
        val newUsernameRef = getUsernameDatabaseRef(username.lowercase())
        val oldUsernameRef = getUsernameDatabaseRef(getUsername().lowercase())
        val oldUsername = getUsername()
        newUsernameRef.get().addOnSuccessListener { databaseSnapshot: DataSnapshot ->
            if (databaseSnapshot.exists()) {
                viewModelScope.launch {
                    SnackbarController.sendEvent(
                        event = SnackbarEvent(
                            message = StringValue.StringResource(R.string.username_already_exists)
                        )
                    )
                }
            } else {
                val profileUpdates = userProfileChangeRequest {
                    if (url.isNotEmpty()) {
                        photoUri = Uri.parse(url)
                    }
                    displayName = username
                }
                user!!.updateProfile(profileUpdates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        oldUsernameRef.removeValue().addOnCompleteListener { removeTask ->
                            if (removeTask.isSuccessful) {
                                Log.d("Delete user", "Deleted username from DB $oldUsername")
                            } else {
                                Log.e("Delete user", "Could not delete username from DB")
                            }
                        }
                        newUsernameRef.setValue(username.lowercase())
                        _username.value = username
                        SocketHandler.getSocket().emit(FriendsEvents.UPDATE_DATA.value)

                        val authorNameUpdate = UserIdName(id = user!!.uid, name = username)
                        val authorUpdateObject = JSONObject(Gson().toJson(authorNameUpdate))
                        SocketHandler.getSocket()
                            .emit(GameEvents.UPDATE_AUTHOR_NAME.value, authorUpdateObject)
                        Log.d(
                            "Profile update",
                            "Used $username ${avatarURL.value}"
                        )
                    } else {
                        Log.e("Profile update", "An error occured...")
                    }
                }
            }
        }
    }

    fun sendUpdateProfileSnackbar() {
        Log.d("Save", "Called snackbar method")
        viewModelScope.launch {
            SnackbarController.sendEvent(
                event = SnackbarEvent(
                    message = StringValue.StringResource(
                        R.string.edited_feedback
                    )
                )
            )
        }
    }


    fun signIn(email: String, password: String, context: Context) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value =
                AuthState.Error(StringValue.StringResource(R.string.empty_username_password))
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userRef = task.result.user?.let { this.getUserDatabaseRef(it.uid) }
                    userRef?.child("isOnline")?.get()
                        ?.addOnSuccessListener { dataSnapshot: DataSnapshot ->
                            val isOnline: Boolean = dataSnapshot.value as Boolean
                            if (isOnline) {
                                auth.signOut()
                                _authState.value =
                                    AuthState.Error(StringValue.StringResource(R.string.already_online))
                                return@addOnSuccessListener
                            }
                            userRef.child("isOnline").setValue(true)
                            userRef.child("isOnline").onDisconnect().setValue(false)
                            user = task.result.user
                            setAvatarUrl(user?.photoUrl.toString())
                            _authState.value = AuthState.Authenticated
                            SocketHandler.connect()
                            SocketHandler.getSocket().emit(FriendsEvents.UPDATE_DATA.value)
                            SocketHandler.getSocket().emit(FriendsEvents.CONNECT.value, user?.uid)
                            Log.d(TAG, "signInWithEmail:success")
                        }
                    userRef?.child("isOnline")?.get()
                        ?.addOnSuccessListener { dataSnapshot: DataSnapshot ->
                            val isOnline: Boolean = dataSnapshot.value as Boolean
                            if (isOnline) {
                                auth.signOut()
                                _authState.value =
                                    AuthState.Error(StringValue.StringResource(R.string.already_online))
                                return@addOnSuccessListener
                            }
                            userRef.child("isOnline").setValue(true)
                            userRef.child("isOnline").onDisconnect().setValue(false)
                            user = task.result.user
                            _username.value = user?.displayName ?: ""
                            _email.value = user?.email ?: ""
                            _authState.value = AuthState.Authenticated
                            SocketHandler.connect()
                            Log.d(TAG, "signInWithEmail:success")
                        }
                } else {
                    handleAuthError(task, context)
                }
            }
    }

    fun signUp(
        email: String,
        username: String,
        password: String,
        context: Context,
        avatarToShow: Any?
    ) {
        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            _authState.value =
                AuthState.Error(StringValue.StringResource(R.string.empty_username_password))
            return
        }
        if (emailError.value.isNotEmpty() || passwordError.value.isNotEmpty() || usernameError.value.isNotEmpty()) {
            _authState.value =
                AuthState.Error(StringValue.StringResource(R.string.invalid_username_password))
            return
        }
        val usernameRef = getUsernameDatabaseRef(username.lowercase())
        usernameRef.get().addOnSuccessListener { databaseSnapshot: DataSnapshot ->
            if (databaseSnapshot.exists()) {
                _authState.value =
                    AuthState.Error(StringValue.StringResource(R.string.username_already_exists))
                Log.e(TAG, StringValue.StringResource(R.string.username_already_exists).toString())
            } else {
                _authState.value = AuthState.Loading
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            user = task.result.user
                            // Save temp avatar if needed
                            if (avatarToShow is Bitmap) {
                                user?.let {
                                    ImageStorage.uploadAvatar(avatarToShow, user!!.uid) { url ->
                                        if (url != null) {
                                            setAvatarUrl(url)
                                        } else {
                                            Log.e("Signin avatar", "Cannot save captured image")
                                        }
                                    }
                                }
                            }

                            val displayNameUpdate = UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .setPhotoUri(Uri.parse(_avatarURL.value))
                                .build()
                            Log.d("Sign up", "Set avatar uri to ${_avatarURL.value}")
                            user?.updateProfile(displayNameUpdate)
                                ?.addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        val userRef =
                                            task.result.user?.let { this.getUserDatabaseRef(it.uid) }
                                        userRef?.child("isOnline")?.setValue(true)
                                        userRef?.child("isOnline")?.onDisconnect()?.setValue(false)
                                        userRef?.child("balance")?.setValue(50)
                                        usernameRef.setValue(username.lowercase())
                                        _username.value = user?.displayName ?: ""
                                        _email.value = user?.email ?: ""
                                        _authState.value = AuthState.Authenticated
                                        setAvatarUrl(user?.photoUrl.toString())
                                        _authState.value = AuthState.Authenticated
                                        SocketHandler.connect()
                                        SocketHandler.getSocket()
                                            .emit(FriendsEvents.UPDATE_DATA.value)
                                        SocketHandler.getSocket()
                                            .emit(FriendsEvents.CONNECT.value, user?.uid)
                                    }
                                    Log.d(TAG, "createUserWithEmail:success")
                                }
                        } else {
                            handleAuthError(task, context)
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

    fun sendResetPasswordEmail(email: String) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _authState.value = AuthState.ResetPassword
            } else {
                _authState.value =
                    AuthState.Error(StringValue.StringResource(R.string.invalid_email_with_emoji))
            }
        }
    }

    fun resetAuthState() {
        // This is to avoid the bug where an error state transfers from login to signup page.
        _authState.value = AuthState.Unauthenticated
    }


    private fun handleAuthError(task: Task<AuthResult>, context: Context) {
        val errorMessage = try {
            throw task.exception!!
        } catch (e: FirebaseAuthUserCollisionException) {
            StringValue.StringResource(R.string.user_already_exists)
        } catch (e: FirebaseAuthWeakPasswordException) {
            StringValue.StringResource(R.string.password_too_short)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            StringValue.StringResource(R.string.invalid_username_password)
        } catch (e: Exception) {
            StringValue.StringResource(R.string.other_error)
        }

        val translatedErrorMessage = errorMessage.asString(context)

        _authState.value = AuthState.Error(StringValue.DynamicString(translatedErrorMessage))
    }

    private fun validatePassword(password: String, context: Context) {
        _passwordError.value = ""
        if (password.length < 6) {
            _passwordError.value += StringValue.StringResource(R.string.password_too_short)
                .asString(context) + "\n"
        }
        if (password.length > 14) {
            _passwordError.value += StringValue.StringResource(R.string.password_too_long)
                .asString(context) + "\n"
        }
        if (!(("(?=.*[a-z\\u00E0-\\u00FC])".toRegex()).containsMatchIn(password))) {
            _passwordError.value += StringValue.StringResource(R.string.password_lowercase)
                .asString(context) + "\n"
        }
        if (!(("(?=.*[A-Z\\u00C0-\\u00DC])".toRegex()).containsMatchIn(password))) {
            _passwordError.value += StringValue.StringResource(R.string.password_uppercase)
                .asString(context) + "\n"
        }
        if (!(("(?=.*\\d)".toRegex()).containsMatchIn(password))) {
            _passwordError.value += StringValue.StringResource(R.string.password_digit)
                .asString(context) + "\n"
        }
        // REFERENCE: Firebase special characters: https://firebase.google.com/docs/auth/web/password-auth
        if (!(("(?=.*[\\^\\$\\*\\.\\[\\]\\{\\}\\(\\)\\?\"!@#%&/\\\\,><':;\\|_~])").toRegex()).containsMatchIn(
                password
            )
        ) {
            _passwordError.value += StringValue.StringResource(R.string.password_special)
                .asString(context) + "\n"
        }
        if (_passwordError.value.isNotEmpty()) {
            _passwordError.value.dropLast(1)
        }
    }

    private fun validateUsername(username: String, context: Context) {
        _usernameError.value = ""
        if (username.matches(".*[^a-zA-ZÀ-ÿ0-9_].*".toRegex())) {
            _usernameError.value += StringValue.StringResource(R.string.special_char_username)
                .asString(context) + "\n"
        }
        if (username.length < 3) {
            _usernameError.value += StringValue.StringResource(R.string.short_username)
                .asString(context) + "\n"
        }
        if (username.length > 20) {
            _usernameError.value += StringValue.StringResource(R.string.long_username)
                .asString(context) + "\n"
        }
        if (_usernameError.value.isNotEmpty()) {
            _usernameError.value.dropLast(1)
        }
    }

    private fun validateEmail(email: String, context: Context) {
        if (email.isBlank() || !isValidEmail(email)) {
            _emailError.value =
                StringValue.StringResource(R.string.invalid_email).asString(context) + "\n"
        } else {
            _emailError.value = ""
        }
    }

    // https://developer.android.com/reference/android/util/Patterns#EMAIL_ADDRESS
    private fun isValidEmail(target: CharSequence): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

    fun getUsername(): String {
        return auth.currentUser?.displayName ?: ""
    }

}

sealed class AuthState {
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Error(val message: StringValue) : AuthState()
    data object ResetPassword : AuthState()
}
