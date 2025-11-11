package com.example.gochicken.network

import com.example.gochicken.network.models.LoginResponse
import com.example.gochicken.network.models.CabangResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("cabang")
    fun getCabang(): Call<CabangResponse>

    @FormUrlEncoded
    @POST("kasir/login")
    fun loginKasir(
        @Field("id_cabang") idCabang: String,
        @Field("password_cabang") passwordCabang: String,
    ): Call<LoginResponse>
}
