package com.example.polyquiz

class QuestionService : CommunicationService<Question>("questions") {
    override val apiService: ApiService = retrofit.create(QuestionsApiService::class.java)

    fun getAllQuestions(onSuccess: (List<Question>) -> Unit, onError: (String) -> Unit) {
        getAll(onSuccess, onError)
    }

    fun createQuestion(question: Question, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        add(question, onSuccess, onError)
    }

    fun deleteQuestion(questionId: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        delete(questionId, onSuccess, onError)
    }

    fun verifyQuestion(question: Question, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        add(question, onSuccess, onError, "validate-question")
    }

    fun updateQuestion(modifiedQuestion: Question, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        update(modifiedQuestion, modifiedQuestion.id, onSuccess, onError)
    }

    interface QuestionsApiService : ApiService {
    }
}
