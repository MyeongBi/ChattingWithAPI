package com.example.chattingwithapi
import com.google.android.gms.tasks.Task
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


interface ApiService {
    @FormUrlEncoded
    @POST("register_token/")
    fun sendToken(@Field("token") token: String): Call<Void>
}
