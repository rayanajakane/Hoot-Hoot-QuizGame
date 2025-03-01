package com.example.polyquiz

import com.example.polyquiz.constants.Environment
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

import retrofit2.http.*


/*REFERENCES: https://square.github.io/retrofit/
* https://medium.com/@desiappdev24/fetching-data-using-retrofit-in-jetpack-compose-a-complete-guide-97f4c2101cb7
* https://www.baeldung.com/retrofit
* https://stackoverflow.com/questions/48296987/retrofit-expected-a-string-but-was-begin-object-at-line-1-column-2-path
*/

abstract class CommunicationService<T>(
    private val baseUrl: String
) {

    protected val retrofit: Retrofit by lazy {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        Retrofit.Builder()
            .baseUrl(Environment.SERVER_LOCAL_ADDRESS.value + "/api/")
            //.addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    protected abstract val apiService: ApiService

    fun getAll(onSuccess: (List<T>) -> Unit, onError: (String) -> Unit, endpoint: String = "") {
        val fullEndpoint = buildFullEndpoint(endpoint)
        @Suppress("UNCHECKED_CAST")
        val call: Call<List<T>> = apiService.getAll(fullEndpoint) as Call<List<T>>
        println(fullEndpoint)
        call.enqueue(createCallback(onSuccess, onError))
    }

    fun getById(id: String, onSuccess: (T) -> Unit, onError: (String) -> Unit, endpoint: String = "") {
        val fullEndpoint = buildFullEndpoint(endpoint)
        @Suppress("UNCHECKED_CAST")
        val call: Call<T> = apiService.getById("$fullEndpoint/$id") as Call<T>
        call.enqueue(createCallback(onSuccess, onError))
    }

    fun add(payload: T, onSuccess: (String) -> Unit, onError: (String) -> Unit, endpoint: String = "") {
        val fullEndpoint = buildFullEndpoint(endpoint)
        val call = apiService.add(fullEndpoint, payload as Any)
       call.enqueue(createCallback(onSuccess, onError))
        println("add")
    }

    fun delete(id: String, onSuccess: (String) -> Unit, onError: (String) -> Unit, endpoint: String = "") {
        val fullEndpoint = buildFullEndpoint(endpoint)
        val call = apiService.delete("$fullEndpoint/$id")
        call.enqueue(createCallback(onSuccess, onError))
    }

    fun update(payload: T, id: String, onSuccess: (String) -> Unit, onError: (String) -> Unit, endpoint: String = "") {
        val fullEndpoint = buildFullEndpoint(endpoint)
        val call = apiService.update("$fullEndpoint/$id", payload as Any)
        call.enqueue(createCallback(onSuccess, onError))
    }

    fun put(payload: T, id: String, onSuccess: (String) -> Unit, onError: (String) -> Unit, endpoint: String = "") {
        val fullEndpoint = buildFullEndpoint(endpoint)
        val call = apiService.put("$fullEndpoint/$id", payload as Any)
        call.enqueue(createCallback(onSuccess, onError))
    }

    // Helper method to build the full endpoint path in case of custom endpoints
    private fun buildFullEndpoint(endpoint: String): String {
        //println("blahblah $baseUrl/$endpoint")
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
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        onSuccess(body)
                        println(body)

                    } else {
                        onError("Response body is null")
                    }
                } else {
                    println("thisistherror")
                    onError("Failed with HTTP code: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<R>, t: Throwable) {
                onError("Network error: ${t.message}")
            }
        }
    }

    interface ApiService {

//        @GET("{url}")
//        fun getAll(@Path("url") url: String): Call<List<Any>>

        @GET
        fun getAll(@Url url: String): Call<List<Any>>

        @GET("{url}")
        fun getById(@Path("url") url: String): Call<Any>

//        @POST()
//        fun add(@Url url: String) url: String, @Body payload: Any): Call<String>

        @POST
        fun add(@Url url: String, @Body payload: Any): Call<String>


        @DELETE("{url}")
        fun delete(@Path("url") url: String): Call<String>

        @PATCH("{url}")
        fun update(@Path("url") url: String, @Body payload: Any): Call<String>

        @PUT("{url}")
        fun put(@Path("url") url: String, @Body payload: Any): Call<String>
    }
}
