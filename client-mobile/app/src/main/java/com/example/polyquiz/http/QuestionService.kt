package com.example.polyquiz.http

import com.example.polyquiz.match.domain.Question
import com.google.gson.reflect.TypeToken

object QuestionService : CommunicationService("questions") {
    override val apiService: ApiService = retrofit.create(QuestionsApiService::class.java)

    fun getAllQuestions(onSuccess: (List<Question>) -> Unit, onError: (String) -> Unit) {
        getAll(
            { response -> onSuccess(convertJsonResponseToType(response, object : TypeToken<List<Question>>() {}.type)) },
            onError)
    }

    fun createQuestion(question: Question, onSuccess: (Question) -> Unit, onError: (String) -> Unit) {
        add(
            question,
            { response -> onSuccess(convertJsonResponseToType(response, Question::class.java)) },
            onError
        )
    }
    fun getQuestionById(questionId: String, onSuccess: (Question) -> Unit, onError: (String) -> Unit) {
        getById(
            questionId,
            { question -> onSuccess(convertJsonResponseToType(question, Question::class.java)) },
            onError
        )
    }

    fun deleteQuestion(questionId: String, onSuccess: (Unit) -> Unit, onError: (String) -> Unit) {
        delete(
            questionId,
            onSuccess,
            onError
        )
    }

    fun verifyQuestion(question: Question, onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        add(
            question,
            { response -> onSuccess(convertJsonResponseToType(response, Boolean::class.java)) },
            onError,
            "validate-question"
        )
    }

    fun updateQuestion(modifiedQuestion: Question, onSuccess: (Unit) -> Unit, onError: (String) -> Unit) {
        update(
            modifiedQuestion,
            modifiedQuestion.id,
            onSuccess,
            onError
        )
    }

    interface QuestionsApiService : ApiService {
    }
}
