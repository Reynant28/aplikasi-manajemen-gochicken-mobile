package com.example.gochicken.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.gochicken.R
import com.example.gochicken.api.ApiClient
import com.example.gochicken.main.CreateTransaksiRequest
import com.example.gochicken.main.CreateTransaksiResponse
import com.example.gochicken.main.DetailTransaksiRequest
import com.example.gochicken.utils.CartManager
import com.example.gochicken.utils.Prefs
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class TransaksiFragment : Fragment() {

    private lateinit var tvTotalHarga: MaterialTextView
    private lateinit var tvTotalItems: MaterialTextView
    private lateinit var etMetodePembayaran: MaterialAutoCompleteTextView
    private lateinit var etNamaPelanggan: TextInputEditText
    private lateinit var btnProsesTransaksi: MaterialButton
    private lateinit var btnBatal: MaterialButton

    private lateinit var prefs: Prefs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transaksi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = Prefs(requireContext())
        initViews(view)
        setupPaymentMethods()
        updateSummary()
        setupButtons()
    }

    private fun initViews(view: View) {
        tvTotalHarga = view.findViewById(R.id.tvTotalHarga)
        tvTotalItems = view.findViewById(R.id.tvTotalItems)
        etMetodePembayaran = view.findViewById(R.id.etMetodePembayaran)
        etNamaPelanggan = view.findViewById(R.id.etNamaPelanggan)
        btnProsesTransaksi = view.findViewById(R.id.btnProsesTransaksi)
        btnBatal = view.findViewById(R.id.btnBatal)
    }

    private fun setupPaymentMethods() {
        val paymentMethods = arrayOf("Cash", "Transfer Bank", "QRIS", "Debit Card", "Credit Card")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_dropdown, paymentMethods)
        etMetodePembayaran.setAdapter(adapter)

        //otomatis milih metode pembayaran pertama
        etMetodePembayaran.threshold = 1
    }

    private fun updateSummary() {
        val totalItems = CartManager.getTotalItems()
        val totalHarga = CartManager.getTotalPrice()

        tvTotalItems.text = "Total Items: $totalItems"
        tvTotalHarga.text = "Total Harga: Rp ${String.format("%,.0f", totalHarga)}"
    }

    private fun setupButtons() {
        btnProsesTransaksi.setOnClickListener {
            if (validateInput()) {
                showConfirmationDialog()
            }
        }

        btnBatal.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun validateInput(): Boolean {
        val metodePembayaran = etMetodePembayaran.text.toString().trim()
        val namaPelanggan = etNamaPelanggan.text.toString().trim()

        if (metodePembayaran.isEmpty()) {
            etMetodePembayaran.error = "Pilih metode pembayaran"
            return false
        }

        // Remove nama pelanggan validation since it's optional
        // if (namaPelanggan.isEmpty()) {
        //     etNamaPelanggan.error = "Masukkan nama pelanggan"
        //     return false
        // }

        if (CartManager.getCartItems().isEmpty()) {
            showError("Keranjang kosong. Tambahkan item terlebih dahulu.")
            return false
        }

        return true
    }

    private fun showConfirmationDialog() {
        val totalHarga = CartManager.getTotalPrice()
        val totalItems = CartManager.getTotalItems()
        val metodePembayaran = etMetodePembayaran.text.toString()
        val namaPelanggan = etNamaPelanggan.text.toString().trim()

        // Use default value if nama pelanggan is empty
        val finalNamaPelanggan = if (namaPelanggan.isEmpty()) "Walk In Customer" else namaPelanggan

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi Transaksi")
            .setMessage(
                """
                Nama Pelanggan: $finalNamaPelanggan
                Metode Pembayaran: $metodePembayaran
                Total Items: $totalItems
                Total Harga: Rp ${String.format("%,.0f", totalHarga)}
                
                Proses transaksi ini?
                """.trimIndent()
            )
            .setPositiveButton("Proses") { dialog, _ ->
                processTransaction()
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun processTransaction() {
        val cartItems = CartManager.getCartItems()
        val totalHarga = CartManager.getTotalPrice()
        val cabangId = prefs.getCabangId()
        val metodePembayaran = etMetodePembayaran.text.toString()
        val namaPelanggan = etNamaPelanggan.text.toString().trim()

        // Use default value if nama pelanggan is empty
        val finalNamaPelanggan = if (namaPelanggan.isEmpty()) "Walk In Customer" else namaPelanggan

        // Generate transaction code
        val kodeTransaksi = generateTransactionCode()

        // Prepare transaction items
        val transactionItems = cartItems.map { cartItem ->
            DetailTransaksiRequest(
                id_produk = cartItem.product.id_produk,
                jumlah_produk = cartItem.quantity,
                harga_item = cartItem.product.harga.toDouble(),
                subtotal = cartItem.getTotalPrice()
            )
        }

        // Create transaction request
        val transactionRequest = CreateTransaksiRequest(
            kode_transaksi = kodeTransaksi,
            total_harga = totalHarga,
            tanggal_waktu = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            metode_pembayaran = metodePembayaran,
            status_pembayaran = "Selesai", // or "OnLoan" based on your business logic
            nama_pelanggan = finalNamaPelanggan, // Use the final value here
            id_cabang = cabangId,
            items = transactionItems
        )

        // Show loading
        btnProsesTransaksi.isEnabled = false
        btnProsesTransaksi.text = "Memproses..."

        // Send to API
        ApiClient.instance.createTransaksi(transactionRequest).enqueue(object : Callback<CreateTransaksiResponse> {
            override fun onResponse(call: Call<CreateTransaksiResponse>, response: Response<CreateTransaksiResponse>) {
                btnProsesTransaksi.isEnabled = true
                btnProsesTransaksi.text = "Proses Transaksi"

                if (response.isSuccessful && response.body() != null) {
                    val transactionResponse = response.body()!!
                    if (transactionResponse.status) {
                        showSuccessDialog(transactionResponse.data?.kode_transaksi ?: kodeTransaksi)
                    } else {
                        showError("Gagal memproses transaksi: ${transactionResponse.message}")
                    }
                } else {
                    showError("Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<CreateTransaksiResponse>, t: Throwable) {
                btnProsesTransaksi.isEnabled = true
                btnProsesTransaksi.text = "Proses Transaksi"
                showError("Network error: ${t.message}")
            }
        })
    }

    private fun generateTransactionCode(): String {
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        val random = (1000..9999).random()
        return "TRX-$timestamp-$random"
    }

    private fun showSuccessDialog(kodeTransaksi: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Transaksi Berhasil")
            .setMessage("Transaksi dengan kode $kodeTransaksi berhasil diproses!")
            .setPositiveButton("OK") { dialog, _ ->
                // Clear cart and navigate back
                CartManager.clearCart()
                requireActivity().supportFragmentManager.popBackStack()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun showError(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    companion object {
        @JvmStatic
        fun newInstance() = TransaksiFragment()
    }
}