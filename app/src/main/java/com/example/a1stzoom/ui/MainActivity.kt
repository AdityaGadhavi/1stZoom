package com.example.a1stzoom.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a1stzoom.R
import com.example.a1stzoom.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

open class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var dataStringModals = ArrayList<String>()

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullScreen()

        sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)

        getDataFromSharedPref()

        binding.addIcon.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivityForResult(intent, 2)
        }
    }

    private fun getDataFromSharedPref() {
        val json = sharedPreferences.getString("repoList", null)
        if (json != null) {
            try {
                val type: Type = object : TypeToken<ArrayList<String?>?>() {}.type
                dataStringModals = ArrayList()
                dataStringModals = gson.fromJson(json, type) as ArrayList<String>
            } catch (e: Exception) {
                Toast.makeText(this, "There was a problem loading the data.", Toast.LENGTH_SHORT)
                    .show()
                e.printStackTrace()
            }
        }
    }

    private fun fullScreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.transparent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == 2) {
            getDataFromSharedPref()
        }
    }

    override fun onResume() {
        setAdapter()
        super.onResume()
    }

    private fun setAdapter() {
        if (dataStringModals.isEmpty()) {
            binding.noData.visibility = View.VISIBLE
        } else {

            val adapter = RepositoryAdapter(dataStringModals)
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.adapter = adapter
            binding.noData.visibility = if (dataStringModals.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}