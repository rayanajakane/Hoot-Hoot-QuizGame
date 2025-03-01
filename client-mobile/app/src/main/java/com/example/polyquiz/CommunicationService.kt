package com.example.polyquiz

import com.example.polyquiz.constants.Environment
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.lang.reflect.Type


/*REFERENCES: https://square.github.io/retrofit/
* https://medium.com/@desiappdev24/fetching-data-using-retrofit-in-jetpack-compose-a-complete-guide-97f4c2101cb7
* https://www.baeldung.com/retrofit
*/

abstract class CommunicationService(
    private val baseUrl: String
) {

    protected val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Environment.SERVER_LOCAL_ADDRESS.value + "/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    protected abstract val apiService: ApiService

    fun getAll(onSuccess: (List<Any>) -> Unit, onError: (String) -> Unit, endpoint: String = "") {
        val fullEndpoint = buildFullEndpoint(endpoint)
        val call = apiService.getAll(fullEndpoint)
        call.enqueue(createCallback(onSuccess, onError))
    }

    fun getById(id: String, onSuccess: (Any) -> Unit, onError: (String) -> Unit, endpoint: String = "") {
        val fullEndpoint = buildFullEndpoint(endpoint)
        val call = apiService.getById("$fullEndpoint/$id")
        call.enqueue(createCallback(onSuccess, onError))
    }

    fun add(payload: Any, onSuccess: (Any) -> Unit, onError: (String) -> Unit, endpoint: String = "") {
        val fullEndpoint = buildFullEndpoint(endpoint)
        val call = apiService.add(fullEndpoint, payload)
        call.enqueue(createCallback(onSuccess, onError))
    }

    fun delete(id: String, onSuccess: (String) -> Unit, onError: (String) -> Unit, endpoint: String = "") {
        val fullEndpoint = buildFullEndpoint(endpoint)
        @Suppress("UNCHECKED_CAST")
        val call = apiService.delete("$fullEndpoint/$id") as Call<String>
        call.enqueue(createCallback(onSuccess, onError))
    }

    fun update(payload: Any, id: String, onSuccess: (String) -> Unit, onError: (String) -> Unit, endpoint: String = "") {
        val fullEndpoint = buildFullEndpoint(endpoint)
        println("API URL: $fullEndpoint/$id")
        @Suppress("UNCHECKED_CAST")
        val call = apiService.update("$fullEndpoint/$id", payload) as Call<String>
        call.enqueue(createCallback(onSuccess, onError))
    }

    fun put(payload: Any, id: String, onSuccess: (String) -> Unit, onError: (String) -> Unit, endpoint: String = "") {
        val fullEndpoint = buildFullEndpoint(endpoint)
        @Suppress("UNCHECKED_CAST")
        val call = apiService.put("$fullEndpoint/$id", payload) as Call<String>
        call.enqueue(createCallback(onSuccess, onError))
    }

    // Helper method to build the full endpoint path in case of custom endpoints
    private fun buildFullEndpoint(endpoint: String): String {
        return if (endpoint.isNotEmpty()) {
            "$baseUrl/$endpoint"
        } else {
            baseUrl
        }
    }

    // Helper method to create a Retrofit Callback with centralized error handling
    private fun <R> createCallback(onSuccess: (R) -> Unit, onError: (String) -> Unit): Callback<R> {
        return object : Callback<R> {
            override fun onResponse(call: Call<R>, response: Response<R>) {
                println("API Response: $response")
                if (response.isSuccessful) {
                    val body = response.body()
                    println("API Response: $body")
                    if (body != null) {
                        onSuccess(body)
                    } else {
                        onError("Response body is null")
                    }
                } else {
                    onError("Failed with HTTP code: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<R>, t: Throwable) {
                onError("Network error: ${t}")
                t.printStackTrace()
            }
        }
    }

    protected fun <T> convertJsonResponseToType(jsonResponse: Any, type: Type): T {
        val gson = Gson()
        val json = gson.toJson(jsonResponse)
        return gson.fromJson(json, type)
    }

    interface ApiService {

        @GET
        fun getAll(@Url url: String): Call<List<Any>>

        @GET
        fun getById(@Url url: String): Call<Any>

        @POST
        fun add(@Url url: String, @Body payload: Any): Call<Any>

        @DELETE
        fun delete(@Url url: String): Call<Any>

        @PATCH
        fun update(@Url url: String, @Body payload: Any): Call<Any>

        @PUT
        fun put(@Url url: String, @Body payload: Any): Call<Any>
    }
}
