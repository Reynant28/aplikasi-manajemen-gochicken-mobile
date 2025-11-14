package com.example.gochicken.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.gochicken.R
import com.example.gochicken.fragments.KatalogFragment
import com.example.gochicken.fragments.KeranjangFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.ImageView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.gochicken.utils.Prefs
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.material.button.MaterialButton
import android.view.LayoutInflater
import com.example.gochicken.api.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var toolbarTitle: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvClock: TextView
    private lateinit var menuHeader: LinearLayout
    private lateinit var btnSortDropdown: MaterialButton
    private lateinit var iconMenu: ImageView
    private lateinit var prefs: Prefs
    private val handler = Handler(Looper.getMainLooper())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale("id", "ID"))

    // Sort state
    private var currentSortType: SortType = SortType.DEFAULT
    private var sortPopupWindow: PopupWindow? = null
    private var menuPopupWindow: PopupWindow? = null

    enum class SortType {
        DEFAULT, PRICE_HIGH_TO_LOW, PRICE_LOW_TO_HIGH, CATEGORY
    }

    // Sort type display names
    private val sortTypeNames = mapOf(
        SortType.DEFAULT to "Default",
        SortType.PRICE_HIGH_TO_LOW to "Harga Tertinggi",
        SortType.PRICE_LOW_TO_HIGH to "Harga Terendah",
        SortType.CATEGORY to "Kategori"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        prefs = Prefs(this)
        setupToolbar()
        setupMenu()
        setupSortDropdown()
        setupBottomNavigation()
        startDateTime()
        loadUserData()

        // Load KatalogFragment by default
        if (savedInstanceState == null) {
            loadFragment(KatalogFragment())
            showMenuHeader(true)
            findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.navigation_catalog
        }
    }

    private fun loadUserData() {
        // First try to get from shared preferences
        val userName = prefs.getUserName()
        if (userName != null) {
            tvUserName.text = userName
        }

        // Then fetch fresh data from API
        ApiClient.instance.getCurrentUser().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val userResponse = response.body()!!
                    if (userResponse.status == "success") {
                        val user = userResponse.user
                        val name = user.name ?: user.nama ?: "Kasir" // Try both 'name' and 'nama' fields
                        tvUserName.text = name

                        // Save to shared preferences
                        prefs.setUserName(name)
                    }
                } else {
                    Log.e("UserData", "Failed to fetch user data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("UserData", "Network error fetching user data: ${t.message}")
            }
        })
    }

    private fun setupToolbar() {
        toolbarTitle = findViewById(R.id.toolbar_title)
        tvUserName = findViewById(R.id.tvUserName)
        tvDate = findViewById(R.id.tvDate)
        tvClock = findViewById(R.id.tvClock)
        menuHeader = findViewById(R.id.menuHeader)
        btnSortDropdown = findViewById(R.id.btnSortDropdown)
        iconMenu = findViewById(R.id.icon_menu)

        // Set initial date
        updateDate()

        // Update sort button text
        updateSortButtonText()
    }

    private fun setupMenu() {
        iconMenu.setOnClickListener {
            showMenuDropdown(it)
        }
    }

    private fun showMenuDropdown(anchorView: View) {
        val inflater = LayoutInflater.from(this)
        val dropdownView = inflater.inflate(R.layout.menu_dropdown_layout, null)

        // Convert dp to pixels for proper width
        val widthInDp = 150
        val density = resources.displayMetrics.density
        val widthInPixels = (widthInDp * density).toInt()

        val popupWindow = PopupWindow(
            dropdownView,
            widthInPixels,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        // Simple background
        popupWindow.setBackgroundDrawable(resources.getDrawable(R.drawable.dropdown_background, theme))
        popupWindow.elevation = 8f
        popupWindow.isOutsideTouchable = true

        // Set click listener for logout option
        dropdownView.findViewById<TextView>(R.id.menuLogout).setOnClickListener {
            popupWindow.dismiss()
            showLogoutConfirmation()
        }

        // Show dropdown with proper positioning
        popupWindow.showAsDropDown(anchorView, 0, 8)
        menuPopupWindow = popupWindow

        // Dismiss when touched outside
        dropdownView.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_OUTSIDE) {
                popupWindow.dismiss()
                true
            } else {
                false
            }
        }
    }

    private fun setupSortDropdown() {
        btnSortDropdown.setOnClickListener {
            showSortDropdown(it)
        }
    }

    private fun showSortDropdown(anchorView: View) {
        val inflater = LayoutInflater.from(this)
        val dropdownView = inflater.inflate(R.layout.sort_dropdown_layout, null)

        // Convert dp to pixels for proper width
        val widthInDp = 200
        val density = resources.displayMetrics.density
        val widthInPixels = (widthInDp * density).toInt()

        val popupWindow = PopupWindow(
            dropdownView,
            widthInPixels,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        // Simple background
        popupWindow.setBackgroundDrawable(resources.getDrawable(R.drawable.dropdown_background, theme))
        popupWindow.elevation = 8f
        popupWindow.isOutsideTouchable = true

        // Set click listeners
        dropdownView.findViewById<TextView>(R.id.sortDefault).setOnClickListener {
            setSortType(SortType.DEFAULT)
            popupWindow.dismiss()
        }

        dropdownView.findViewById<TextView>(R.id.sortPriceHighToLow).setOnClickListener {
            setSortType(SortType.PRICE_HIGH_TO_LOW)
            popupWindow.dismiss()
        }

        dropdownView.findViewById<TextView>(R.id.sortPriceLowToHigh).setOnClickListener {
            setSortType(SortType.PRICE_LOW_TO_HIGH)
            popupWindow.dismiss()
        }

        dropdownView.findViewById<TextView>(R.id.sortCategory).setOnClickListener {
            setSortType(SortType.CATEGORY)
            popupWindow.dismiss()
        }

        // Show dropdown with proper positioning
        popupWindow.showAsDropDown(anchorView, 0, 8)
        sortPopupWindow = popupWindow

        // Dismiss when touched outside
        dropdownView.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_OUTSIDE) {
                popupWindow.dismiss()
                true
            } else {
                false
            }
        }
    }

    private fun setSortType(sortType: SortType) {
        currentSortType = sortType
        updateSortButtonText()

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is KatalogFragment) {
            currentFragment.onSortChanged(sortType)
        }
    }

    private fun updateSortButtonText() {
        val sortName = sortTypeNames[currentSortType] ?: "Urutkan"
        btnSortDropdown.text = sortName
    }

    private fun showMenuHeader(show: Boolean) {
        menuHeader.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun startDateTime() {
        val dateTimeUpdater = object : Runnable {
            override fun run() {
                updateDate() // Update date at midnight
                tvClock.text = timeFormat.format(Date())
                handler.postDelayed(this, 1000) // Update every second
            }
        }
        handler.post(dateTimeUpdater)
    }

    private fun updateDate() {
        val currentDate = Date()
        tvDate.text = dateFormat.format(currentDate)
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_catalog -> {
                    loadFragment(KatalogFragment())
                    toolbarTitle.text = "Buat Pesanan Baru"
                    showMenuHeader(true)
                    true
                }
                R.id.navigation_cart -> {
                    loadFragment(KeranjangFragment())
                    toolbarTitle.text = "Keranjang Pesanan"
                    showMenuHeader(false)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah Anda yakin ingin logout?")
            .setPositiveButton("Logout") { dialog, which ->
                prefs.clearUserData()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Batal") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        sortPopupWindow?.dismiss()
        menuPopupWindow?.dismiss()
    }
}