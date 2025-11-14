package com.example.gochicken.main

data class LoginResponse(
    val status: String,
    val token: String,
    val user: User,
    val cabang: Cabang
)