package com.example.polyquiz

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class QuestionService : CommunicationService<Question>("questions") {
    override val apiService: ApiService = retrofit.create(QuestionsApiService::class.java)

    fun getAllQuestions(onSuccess: (List<Question>) -> Unit, onError: (String) -> Unit) {
        getAll(onSuccess, onError)
    }

    fun getQuestionById(questionId: String, onSuccess: (Any) -> Unit, onError: (String) -> Unit) {
        getById(questionId, onSuccess, onError)
    }

    fun createQuestion(question: Question, onSuccess: (Any) -> Unit, onError: (String) -> Unit) {
        add(question, onSuccess, onError)
    }

    fun deleteQuestion(questionId: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        delete(questionId, onSuccess, onError)
    }

    fun verifyQuestion(question: Question, onSuccess: (Any) -> Unit, onError: (String) -> Unit) {
        add(question, onSuccess, onError, "validate-question")
    }

    fun updateQuestion(modifiedQuestion: Question, onSuccess: (Any) -> Unit, onError: (String) -> Unit) {
        update(modifiedQuestion, modifiedQuestion.id, onSuccess, onError)
    }

    inline fun <reified T> convertToGenericType(data: Any): T? {
        val gson = Gson()
        val json = gson.toJson(data)
        val type: Type = object : TypeToken<T>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    interface QuestionsApiService : ApiService {
    }
}
