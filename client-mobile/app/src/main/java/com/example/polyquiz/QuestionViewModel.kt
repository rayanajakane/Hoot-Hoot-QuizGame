package com.example.polyquiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf

class QuestionViewModel : ViewModel() {
    val questions = mutableStateOf("Fetching questions...")

    init {
        fetchQuestions()
    }

    fun fetchQuestions() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getAllQuestions()

                //And Remove the advice with _advice
                questions.value = response.elementAt(0).text
            } catch (e: Exception) {
                questions.value = "Error: ${e.message}"
            }
        }
    }
}
