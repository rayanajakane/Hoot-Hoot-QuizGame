package com.example.polyquiz.auth.domain

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.polyquiz.core.TranslationService
import com.example.polyquiz.http.CommunicationService
import com.example.polyquiz.http.QuestionService.getAll
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


object UsernameSuggestionService {
    val _usernames = MutableStateFlow<List<String>>(emptyList())
    val usernames: Flow<List<String>> = _usernames.asStateFlow()
    var showUsernameDialog by mutableStateOf(false)
    lateinit var translationService: TranslationService

    val apiService = object : CommunicationService("username-suggestion/fr") {
        override val apiService: ApiService = retrofit.create(ApiService::class.java)
    }

    fun getUsernameSuggestions() {
        apiService.getAll(onSuccess = { response ->
            val gson = Gson()
            val usernameList =
                gson.fromJson(gson.toJson(response), Array<String>::class.java).toList()
            _usernames.update { usernameList }
        },

            onError = { error -> Log.d("UsernameSuggestionService", "Error: $error") })

    }

}
