package com.example.polyquiz.auth.domain

import com.example.polyquiz.http.CommunicationService

object HistoryService{
    private val historyService = object : CommunicationService("history") {
        override val apiService: ApiService = retrofit.create(ApiService::class.java)
    }
    fun getHistoryById(id: String){
        return historyService.getById(id, onSuccess = {}, onError = {})
    }
}
