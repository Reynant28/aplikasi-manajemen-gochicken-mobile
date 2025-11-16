package com.example.gochicken.api

import com.example.gochicken.main.*
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

    // Android-specific product endpoint (public)
    @GET("android/cabang/{id_cabang}/produk")
    fun getProdukByCabangForAndroid(@Path("id_cabang") idCabang: Int): Call<ProdukResponse>

    // Keep the original for reference (optional)
    @GET("cabang/{id_cabang}/produk")
    fun getProdukByCabang(@Path("id_cabang") idCabang: Int): Call<ProdukResponse>

    @GET("current-user")
    fun getCurrentUser(): Call<UserResponse>

    @POST("transaksi")
    fun createTransaksi(@Body request: CreateTransaksiRequest): Call<CreateTransaksiResponse>

    @GET("cabang/{id_cabang}/transaksi")
    fun getTransaksiByCabang(@Path("id_cabang") idCabang: Int): Call<TransaksiResponse>
}