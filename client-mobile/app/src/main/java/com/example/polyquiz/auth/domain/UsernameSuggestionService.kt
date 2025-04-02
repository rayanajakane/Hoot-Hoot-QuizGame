package com.example.polyquiz.auth.domain
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.polyquiz.core.TranslationService
import com.example.polyquiz.http.CommunicationService
import com.example.polyquiz.http.QuestionService.getAll
import com.google.gson.Gson


object UsernameSuggestionService {
    var usernames: List<String> = listOf()
    var showUsernameDialog by mutableStateOf(false)

    lateinit var translationService: TranslationService
    val apiService = object : CommunicationService("username-suggestion/:fr") {
        override val apiService: ApiService = retrofit.create(ApiService::class.java)
    }

    fun getUsernameSuggestions() {
        apiService.getAll(onSuccess = { response ->
           // data:List<Any> -> this.usernames = data as List<String>
            val gson = Gson()
            val usernameList = gson.fromJson(gson.toJson(response), Array<String>::class.java).toList()
            this.usernames = usernameList},
            onError = { error -> Log.d("UsernameSuggestionService", "Error: $error")})

    }

}
