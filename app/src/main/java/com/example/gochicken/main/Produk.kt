package com.example.gochicken.main

data class ProdukResponse(
    val status: Boolean, // Changed from String to Boolean
    val message: String?,
    val data: List<Produk>
)

data class Produk(
    val id_produk: Int,
    val nama_produk: String,
    val kategori: String,
    val harga: String,
    val gambar_produk: String?,
    val gambar_url: String?,
    val jumlah_stok: Int,
    val id_stock_cabang: Int
)

//terakhir kali benerin scrollbar, trus faase selanjutnta.