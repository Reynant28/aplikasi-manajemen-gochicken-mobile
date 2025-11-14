package com.example.gochicken.main

data class CabangResponse(
    val status: Boolean,
    val data: List<Cabang>
)

data class Cabang(
    val id_cabang: String,
    val nama_cabang: String
)