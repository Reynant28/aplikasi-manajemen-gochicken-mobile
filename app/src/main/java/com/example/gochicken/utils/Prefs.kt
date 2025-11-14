package com.example.gochicken.utils

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val sharedPref: SharedPreferences = context.getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)

    fun getToken(): String? = sharedPref.getString("TOKEN", null)
    fun getCabangId(): Int = sharedPref.getInt("CABANG_ID", 0)
    fun getCabangName(): String? = sharedPref.getString("CABANG_NAME", null)
    fun getUserRole(): String? = sharedPref.getString("USER_ROLE", null)

    fun setUserName(name: String) {
        sharedPref.edit().putString("USER_NAME", name).apply()
    }

    fun getUserName(): String? {
        return sharedPref.getString("USER_NAME", null)
    }

    fun clearUserData() {
        sharedPref.edit().clear().apply()
    }
}