package com.example.gochicken.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gochicken.R
import com.example.gochicken.main.CartItem
import com.example.gochicken.utils.CartManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class CartAdapter(private var cartItems: List<CartItem>) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    fun updateData(newCartItems: List<CartItem>) {
        cartItems = newCartItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]
        holder.bind(cartItem)
    }

    override fun getItemCount(): Int = cartItems.size

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val productName: TextView = itemView.findViewById(R.id.tvProductName)
        private val productPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val productCategory: TextView = itemView.findViewById(R.id.tvProductCategory)
        private val quantityText: TextView = itemView.findViewById(R.id.tvQuantity)
        private val btnDecrease: MaterialButton = itemView.findViewById(R.id.btnDecrease)
        private val btnIncrease: MaterialButton = itemView.findViewById(R.id.btnIncrease)
        private val btnRemove: MaterialButton = itemView.findViewById(R.id.btnRemove)
        private val etNotes: TextInputEditText = itemView.findViewById(R.id.etNotes)

        fun bind(cartItem: CartItem) {
            val product = cartItem.product

            productName.text = product.nama_produk
            productCategory.text = product.kategori
            productPrice.text = formatPrice(product.harga)
            quantityText.text = cartItem.quantity.toString()
            etNotes.setText(cartItem.notes)

            // Load product image
            val imageUrl = product.gambar_url ?: "http://172.16.246.100:8000/storage/${product.gambar_produk}"
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerInside()
                .into(productImage)

            // Handle quantity changes
            btnDecrease.setOnClickListener {
                if (cartItem.quantity > 1) {
                    CartManager.updateQuantity(product.id_produk, cartItem.quantity - 1)
                }
            }

            btnIncrease.setOnClickListener {
                if (cartItem.quantity < product.jumlah_stok) {
                    CartManager.updateQuantity(product.id_produk, cartItem.quantity + 1)
                }
            }

            btnRemove.setOnClickListener {
                CartManager.removeFromCart(product.id_produk)
            }

            // Handle notes changes
            etNotes.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    CartManager.updateNotes(product.id_produk, etNotes.text.toString())
                }
            }
        }

        private fun formatPrice(price: String): String {
            return try {
                val priceDouble = price.toDouble()
                "Rp ${String.format("%,.0f", priceDouble)}"
            } catch (e: NumberFormatException) {
                "Rp 0"
            }
        }
    }
}