package com.example.gochicken.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gochicken.R
import com.example.gochicken.adapters.CartAdapter
import com.example.gochicken.utils.CartManager
import com.google.android.material.button.MaterialButton

class KeranjangFragment : Fragment() {

    private lateinit var recyclerViewCart: RecyclerView
    private lateinit var tvEmptyCart: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var tvTotalItems: TextView
    private lateinit var btnCheckout: MaterialButton
    private lateinit var btnClearCart: MaterialButton
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_keranjang, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerViewCart = view.findViewById(R.id.recyclerViewCart)
        tvEmptyCart = view.findViewById(R.id.tvEmptyCart)
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice)
        tvTotalItems = view.findViewById(R.id.tvTotalItems)
        btnCheckout = view.findViewById(R.id.btnCheckout)
        btnClearCart = view.findViewById(R.id.btnClearCart)

        setupRecyclerView()
        setupButtons()
        updateCartUI()

        // Listen for cart changes
        CartManager.addCartListener { cartItems ->
            updateCartUI()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(emptyList())
        recyclerViewCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    private fun setupButtons() {
        btnCheckout.setOnClickListener {
            // Handle checkout logic here
            if (CartManager.getCartItems().isNotEmpty()) {
                showCheckoutDialog()
            }
        }

        btnClearCart.setOnClickListener {
            if (CartManager.getCartItems().isNotEmpty()) {
                CartManager.clearCart()
            }
        }
    }

    private fun updateCartUI() {
        val cartItems = CartManager.getCartItems()
        val totalItems = CartManager.getTotalItems()
        val totalPrice = CartManager.getTotalPrice()

        if (cartItems.isEmpty()) {
            tvEmptyCart.visibility = View.VISIBLE
            recyclerViewCart.visibility = View.GONE
            btnCheckout.isEnabled = false
            btnClearCart.isEnabled = false
        } else {
            tvEmptyCart.visibility = View.GONE
            recyclerViewCart.visibility = View.VISIBLE
            btnCheckout.isEnabled = true
            btnClearCart.isEnabled = true

            cartAdapter.updateData(cartItems)
        }

        tvTotalItems.text = "Total Items: $totalItems"
        tvTotalPrice.text = "Total: Rp ${String.format("%,.0f", totalPrice)}"
    }

    private fun showCheckoutDialog() {
        // Implement your checkout dialog logic here
        // This could show order summary and proceed to payment/order confirmation
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Checkout")
            .setMessage("Total: Rp ${String.format("%,.0f", CartManager.getTotalPrice())}\n\nLanjutkan dengan pesanan?")
            .setPositiveButton("Ya") { dialog, _ ->
                // Handle checkout process
                processCheckout()
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun processCheckout() {
        // Implement your checkout process here
        // This could involve sending order to API, etc.
        // For now, just show a success message and clear cart
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Pesanan Berhasil")
            .setMessage("Pesanan Anda telah berhasil dibuat!")
            .setPositiveButton("OK") { dialog, _ ->
                CartManager.clearCart()
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove cart listener to prevent memory leaks
        CartManager.removeCartListener { updateCartUI() }
    }

    companion object {
        @JvmStatic
        fun newInstance() = KeranjangFragment()
    }
}