package com.example.gochicken.main

data class CartItem(
    val product: Produk,
    var quantity: Int,
    var notes: String = ""
) {
    fun getTotalPrice(): Double {
        return (product.harga.toDoubleOrNull() ?: 0.0) * quantity
    }
}
