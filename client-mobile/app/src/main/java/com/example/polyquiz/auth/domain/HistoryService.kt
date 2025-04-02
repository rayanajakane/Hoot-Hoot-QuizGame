package com.example.polyquiz.auth.domain

import com.example.polyquiz.http.CommunicationService
import com.example.polyquiz.constants.UserHistoryInfo
import com.google.gson.Gson
import java.lang.reflect.Type

object HistoryService : CommunicationService("history") {
    override  val apiService: ApiService = retrofit.create(HistoryApiService::class.java)
//    private val historyService = object : CommunicationService("history") {
//        override val apiService: ApiService = retrofit.create(ApiService::class.java)
//    }

    fun getHistoryById(
        id: String,
        onSuccess: (UserHistoryInfo) -> Unit,
        onError: (String) -> Unit
    ) {
        getById(
            id,
            { response ->
                onSuccess(convertJsonResponseToType(response, UserHistoryInfo::class.java))
            },
            onError
        )
    }

    interface HistoryApiService : ApiService{
    }
}
