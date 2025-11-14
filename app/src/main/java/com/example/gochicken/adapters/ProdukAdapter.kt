package com.example.gochicken.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gochicken.R
import com.example.gochicken.main.Produk

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

            // Load image with better handling
            val imageUrl = produk.gambar_url ?: "http://172.16.246.100:8000/storage/${produk.gambar_produk}"
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_logout)
                .error(R.drawable.ic_logout)
                .centerInside()
                .into(productImage)

            cartButton.setOnClickListener {
                onAddToCartListener?.invoke(produk)
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