package com.example.gochicken

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gochicken.network.ApiClient
import com.example.gochicken.network.models.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.ArrayAdapter
import com.example.gochicken.network.models.Cabang
import com.example.gochicken.network.models.CabangResponse


class LoginActivity : AppCompatActivity() {
    private lateinit var actCabang: AutoCompleteTextView
    private lateinit var etPasswordCabang: EditText
    private lateinit var btnLogin: Button

    private var cabangList: List<Cabang> = emptyList()
    private var selectedCabangId: String? = null

    private fun loadCabangData() {
        Log.d("CabangDebug", "Loading cabang data...")

        ApiClient.instance.getCabang().enqueue(object : Callback<CabangResponse> {
            override fun onResponse(call: Call<CabangResponse>, response: Response<CabangResponse>) {
                Log.d("CabangDebug", "Response code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val cabangResponse = response.body()!!
                    cabangList = cabangResponse.data
                    Log.d("CabangDebug", "Cabang count: ${cabangList.size}")

                    val namaCabangList = cabangList.map { it.nama_cabang }
                    val adapter = ArrayAdapter(
                        this@LoginActivity,
                        android.R.layout.simple_dropdown_item_1line,
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

            override fun onFailure(call: Call<CabangResponse>, t: Throwable) {
                Log.e("CabangDebug", "Network error: ${t.message}", t)
            }
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        actCabang = findViewById(R.id.actCabang)
        etPasswordCabang = findViewById(R.id.etPasswordCabang)
        btnLogin = findViewById(R.id.btnLogin)

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
                                saveToken(body.token)
                                Toast.makeText(this@LoginActivity, "Login berhasil", Toast.LENGTH_SHORT).show()
                                Log.d("LoginActivity", "Token: ${body.token}")

                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.putExtra("CABANG_ID", selectedCabangId)
                                startActivity(intent)
                                finish()
                                // proceed to next screen
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

    private fun saveToken(token: String) {
        val sharedPref = getSharedPreferences("USER_PREF", MODE_PRIVATE)
        sharedPref.edit().putString("TOKEN", token).apply()
    }
}
