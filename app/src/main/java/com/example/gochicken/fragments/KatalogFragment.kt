package com.example.gochicken.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.gochicken.R
import com.example.gochicken.adapters.ProdukAdapter
import com.example.gochicken.api.ApiClient
import com.example.gochicken.main.MainActivity
import com.example.gochicken.main.Produk
import com.example.gochicken.utils.Prefs

class KatalogFragment : Fragment() {

    private lateinit var recyclerViewProducts: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var prefs: Prefs
    private lateinit var produkAdapter: ProdukAdapter

    private var produkList: List<Produk> = emptyList()
    private var currentSortType: MainActivity.SortType = MainActivity.SortType.DEFAULT

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_katalog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        prefs = Prefs(requireContext())
        setupRecyclerView()
        setupSwipeRefresh()
        loadProdukData()

        produkAdapter.setOnAddToCartListener { produk ->
            Log.d("AddToCart", "AddToCart is Successfull")
        }
    }

    private fun setupRecyclerView() {
        produkAdapter = ProdukAdapter(emptyList())
        recyclerViewProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = produkAdapter
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
            R.color.app_orange,
            R.color.logout_red,
            R.color.app_text_dark
        )

        swipeRefreshLayout.setOnRefreshListener {
            Log.d("KatalogFragment", "Swipe refresh triggered")
            loadProdukData()

            Handler(Looper.getMainLooper()).postDelayed({
                if (swipeRefreshLayout.isRefreshing) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }, 3000)
        }
    }

    private fun loadProdukData() {
        val cabangId = prefs.getCabangId()

        if (cabangId == 0) {
            Log.e("KatalogFragment", "Cabang ID not found")
            swipeRefreshLayout.isRefreshing = false
            return
        }

        Log.d("KatalogFragment", "Loading products for cabang ID: $cabangId")

        ApiClient.instance.getProdukByCabangForAndroid(cabangId).enqueue(object : retrofit2.Callback<com.example.gochicken.main.ProdukResponse> {
            override fun onResponse(call: retrofit2.Call<com.example.gochicken.main.ProdukResponse>, response: retrofit2.Response<com.example.gochicken.main.ProdukResponse>) {
                swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful && response.body() != null) {
                    val produkResponse = response.body()!!
                    if (produkResponse.status) {
                        produkList = produkResponse.data
                        Log.d("KatalogFragment", "Loaded ${produkList.size} products")
                        applySorting() // Apply current sorting
                    } else {
                        Log.e("KatalogFragment", "API returned false status: ${produkResponse.message}")
                    }
                } else {
                    Log.e("KatalogFragment", "Response error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<com.example.gochicken.main.ProdukResponse>, t: Throwable) {
                swipeRefreshLayout.isRefreshing = false
                Log.e("KatalogFragment", "Network error: ${t.message}", t)
            }
        })
    }

    fun onSortChanged(sortType: MainActivity.SortType) {
        currentSortType = sortType
        applySorting()
    }

    private fun applySorting() {
        val sortedList = when (currentSortType) {
            MainActivity.SortType.DEFAULT -> produkList
            MainActivity.SortType.PRICE_HIGH_TO_LOW -> produkList.sortedByDescending { it.harga.toDoubleOrNull() ?: 0.0 }
            MainActivity.SortType.PRICE_LOW_TO_HIGH -> produkList.sortedBy { it.harga.toDoubleOrNull() ?: 0.0 }
            MainActivity.SortType.CATEGORY -> produkList.sortedBy { it.kategori }
        }

        produkAdapter.updateData(sortedList)
    }

    companion object {
        @JvmStatic
        fun newInstance() = KatalogFragment()
    }
}