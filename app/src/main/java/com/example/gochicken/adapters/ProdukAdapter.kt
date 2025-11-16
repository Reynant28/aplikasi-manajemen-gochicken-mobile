package com.example.gochicken.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gochicken.R
import com.example.gochicken.main.Produk
import com.example.gochicken.utils.CartManager
import com.google.android.material.snackbar.Snackbar

class ProdukAdapter(private var produkList: List<Produk>) :
    RecyclerView.Adapter<ProdukAdapter.ProdukViewHolder>() {

    private var onAddToCartListener: ((Produk) -> Unit)? = null

    fun setOnAddToCartListener(listener: (Produk) -> Unit) {
        this.onAddToCartListener = listener
    }

    fun updateData(newProdukList: List<Produk>) {
        produkList = newProdukList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produk, parent, false)
        return ProdukViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdukViewHolder, position: Int) {
        val produk = produkList[position]
        holder.bind(produk)
    }

    override fun getItemCount(): Int = produkList.size

    inner class ProdukViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productCategory: TextView = itemView.findViewById(R.id.productCategory)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val productStock: TextView = itemView.findViewById(R.id.productStock)
        private val cartButton: ImageButton = itemView.findViewById(R.id.cartButton)

        fun bind(produk: Produk) {
            productName.text = produk.nama_produk
            productCategory.text = produk.kategori
            productPrice.text = formatPrice(produk.harga)
            productStock.text = "Stok: ${produk.jumlah_stok}"

            // Update cart button appearance based on stock and cart status
            if (produk.jumlah_stok <= 0) {
                cartButton.isEnabled = false
                cartButton.setColorFilter(ContextCompat.getColor(itemView.context, R.color.gray))
                productStock.setTextColor(ContextCompat.getColor(itemView.context, R.color.logout_red))
            } else {
                cartButton.isEnabled = true
                if (CartManager.isProductInCart(produk.id_produk)) {
                    cartButton.setColorFilter(ContextCompat.getColor(itemView.context, R.color.app_orange))
                } else {
                    cartButton.setColorFilter(ContextCompat.getColor(itemView.context, R.color.app_text_dark))
                }
                productStock.setTextColor(ContextCompat.getColor(itemView.context, R.color.app_text_dark))
            }

            // Load image with better handling
            val imageUrl = produk.gambar_url ?: "http://172.16.246.100:8000/storage/${produk.gambar_produk}"
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerInside()
                .into(productImage)

            cartButton.setOnClickListener {
                if (produk.jumlah_stok > 0) {
                    CartManager.addToCart(produk, 1)
                    onAddToCartListener?.invoke(produk)

                    // Show snackbar feedback
                    Snackbar.make(itemView, "${produk.nama_produk} ditambahkan ke keranjang", Snackbar.LENGTH_SHORT)
                        .setAction("Lihat") {
                            // Optional: Navigate to cart
                            onAddToCartListener?.invoke(produk)
                        }
                        .show()

                    // Update button color
                    cartButton.setColorFilter(ContextCompat.getColor(itemView.context, R.color.app_orange))
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