package com.example.gochicken.main

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gochicken.api.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.ArrayAdapter

class LoginActivity : AppCompatActivity() {
    private lateinit var actCabang: AutoCompleteTextView
    private lateinit var etPasswordCabang: EditText
    private lateinit var btnLogin: Button

    private var cabangList: List<com.example.gochicken.main.Cabang> = emptyList()
    private var selectedCabangId: String? = null

    private fun loadCabangData() {
        Log.d("CabangDebug", "Loading cabang data...")

        ApiClient.instance.getCabang().enqueue(object : Callback<com.example.gochicken.main.CabangResponse> {
            override fun onResponse(call: Call<com.example.gochicken.main.CabangResponse>, response: Response<com.example.gochicken.main.CabangResponse>) {
                Log.d("CabangDebug", "Response code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val cabangResponse = response.body()!!
                    cabangList = cabangResponse.data
                    Log.d("CabangDebug", "Cabang count: ${cabangList.size}")

                    val namaCabangList = cabangList.map { it.nama_cabang }
                    val adapter = ArrayAdapter(
                        this@LoginActivity,
                        R.layout.simple_dropdown_item_1line,
                        namaCabangList
                    )
                    actCabang.setAdapter(adapter)

                    actCabang.setOnItemClickListener { _, _, position, _ ->
                        selectedCabangId = cabangList[position].id_cabang
                        Log.d("CabangDebug", "Selected cabang ID: $selectedCabangId")
                    }
                } else {
                    Log.e("CabangDebug", "Response error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<com.example.gochicken.main.CabangResponse>, t: Throwable) {
                Log.e("CabangDebug", "Network error: ${t.message}", t)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.gochicken.R.layout.activity_login)

        actCabang = findViewById(com.example.gochicken.R.id.actCabang)
        etPasswordCabang = findViewById(com.example.gochicken.R.id.etPasswordCabang)
        btnLogin = findViewById(com.example.gochicken.R.id.btnLogin)

        loadCabangData()

        btnLogin.setOnClickListener {
            val idCabang = selectedCabangId ?: ""
            val passCabang = etPasswordCabang.text.toString().trim()

            if (idCabang.isEmpty() || passCabang.isEmpty()) {
                Toast.makeText(this, "Isi semua field terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ApiClient.instance.loginKasir(idCabang, passCabang)
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            val body = response.body()!!
                            if (body.status == "success") {
                                saveUserData(body.token, body.user, body.cabang)
                                Toast.makeText(this@LoginActivity, "Login berhasil", Toast.LENGTH_SHORT).show()
                                Log.d("LoginActivity", "Token: ${body.token}, Cabang ID: ${body.user.id_cabang}")

                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, "Login gagal", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Login gagal: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun saveUserData(token: String, user: com.example.gochicken.main.User, cabang: com.example.gochicken.main.Cabang) {
        val sharedPref = getSharedPreferences("USER_PREF", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("TOKEN", token)
            putInt("USER_ID", user.id)
            putString("USER_NAME", user.name ?: user.nama ?: "")
            putString("USER_EMAIL", user.email ?: "")
            putString("USER_ROLE", user.role ?: "")
            putInt("CABANG_ID", user.id_cabang ?: cabang.id_cabang.toInt())
            putString("CABANG_NAME", cabang.nama_cabang)
            apply()
        }
    }
}