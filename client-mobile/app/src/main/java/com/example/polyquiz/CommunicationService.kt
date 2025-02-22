package com.example.polyquiz
import retrofit2.http.GET

interface CommunicationService {
    @GET("questions")
    suspend fun getAllQuestions(): List<Question>
}
