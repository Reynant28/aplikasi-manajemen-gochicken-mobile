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
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
        val totalPrice = CartManager.getTotalPrice()
        val totalItems = CartManager.getTotalItems()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Checkout")
            .setMessage("Total: Rp ${String.format("%,.0f", totalPrice)}\nItem: $totalItems\n\nLanjutkan ke transaksi?")
            .setPositiveButton("Ya") { dialog, _ ->
                // Navigate to transaction fragment
                navigateToTransaction()
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateToTransaction() {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, TransaksiFragment())
        transaction.addToBackStack("keranjang")
        transaction.commit()
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