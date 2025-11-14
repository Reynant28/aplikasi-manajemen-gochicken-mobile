package com.example.gochicken.main

data class User(
    val id: Int,
    val name: String? = null,
    val nama: String? = null, // Add this field for database column 'nama'
    val email: String? = null,
    val role: String? = null,
    val id_cabang: Int? = null
)