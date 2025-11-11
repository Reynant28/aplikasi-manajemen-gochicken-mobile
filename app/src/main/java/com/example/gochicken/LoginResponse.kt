package com.example.gochicken.network.models

data class LoginResponse(
    val status: String,
    val token: String,
    val user: User,
    val cabang: Cabang
)

data class User(
    val id: Int,
    val name: String?,
    val email: String?,
    val role: String?,
    val id_cabang: Int?
)

data class CabangResponse(
    val status: Boolean,
    val data: List<Cabang>
)
data class Cabang(
    val id_cabang: String,
    val nama_cabang: String,
)