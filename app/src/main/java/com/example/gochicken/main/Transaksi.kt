package com.example.gochicken.main

data class Transaksi(
    val id_transaksi: Int,
    val kode_transaksi: String,
    val tanggal_waktu: String,
    val total_harga: Double,
    val metode_pembayaran: String,
    val status_pembayaran: String,
    val nama_pelanggan: String = "Walk In Customer",
    val no_meja: String = "",
    val id_cabang: Int
)

data class DetailTransaksi(
    val id_detail: Int = 0,
    val id_transaksi: Int,
    val id_produk: Int,
    val harga_item: Double,
    val jumlah_produk: Int,
    val subtotal: Double
)

data class TransaksiResponse(
    val status: Boolean,
    val message: String,
    val data: Transaksi? = null
)

data class CreateTransaksiRequest(
    val kode_transaksi: String,
    val total_harga: Double,
    val tanggal_waktu: String, // Make sure this is included
    val metode_pembayaran: String,
    val status_pembayaran: String, // This maps to status_transaksi in DB
    val nama_pelanggan: String,
    val id_cabang: Int,
    val items: List<DetailTransaksiRequest> // Changed from "details"
)

data class DetailTransaksiRequest(
    val id_produk: Int,
    val jumlah_produk: Int,
    val harga_item: Double,
    val subtotal: Double
)

data class CreateTransaksiResponse(
    val status: Boolean,
    val message: String,
    val data: Transaksi? = null
)