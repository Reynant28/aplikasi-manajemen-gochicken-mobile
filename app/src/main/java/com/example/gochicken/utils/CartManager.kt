package com.example.gochicken.utils

import com.example.gochicken.main.CartItem
import com.example.gochicken.main.Produk

object CartManager {
    private val cartItems = mutableListOf<CartItem>()
    private var listeners = mutableListOf<(List<CartItem>) -> Unit>()

    fun addToCart(product: Produk, quantity: Int = 1, notes: String = "") {
        val existingItem = cartItems.find { it.product.id_produk == product.id_produk }

        if (existingItem != null) {
            // Update quantity if item already exists
            existingItem.quantity += quantity
        } else {
            // Add new item
            cartItems.add(CartItem(product, quantity, notes))
        }
        notifyListeners()
    }

    fun removeFromCart(productId: Int) {
        cartItems.removeAll { it.product.id_produk == productId }
        notifyListeners()
    }

    fun updateQuantity(productId: Int, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
            return
        }

        val item = cartItems.find { it.product.id_produk == productId }
        item?.let {
            it.quantity = newQuantity
            notifyListeners()
        }
    }

    fun updateNotes(productId: Int, notes: String) {
        val item = cartItems.find { it.product.id_produk == productId }
        item?.notes = notes
    }

    fun getCartItems(): List<CartItem> = cartItems.toList()

    fun getTotalItems(): Int = cartItems.sumOf { it.quantity }

    fun getTotalPrice(): Double = cartItems.sumOf { it.getTotalPrice() }

    fun clearCart() {
        cartItems.clear()
        notifyListeners()
    }

    fun isProductInCart(productId: Int): Boolean {
        return cartItems.any { it.product.id_produk == productId }
    }

    fun addCartListener(listener: (List<CartItem>) -> Unit) {
        listeners.add(listener)
    }

    fun removeCartListener(listener: (List<CartItem>) -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it(cartItems.toList()) }
    }
}