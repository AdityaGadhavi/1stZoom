package com.example.a1stzoom.ui

import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.a1stzoom.R
import com.example.a1stzoom.databinding.ActivityMain2Binding
import com.google.gson.Gson
import okhttp3.*
import org.json.JSONArray
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MainActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding
    private var courseList = ArrayList<String>()
    private var sharedPreferences: SharedPreferences? = null
    private var userName: String = ""
    private var repoName: String = ""
    private var API: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        fullScreen()

        sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)

        binding.saveButton.setOnClickListener {
            if (binding.userName.text.toString() != "" && binding.repoName.text.toString() != "") {
                userName = binding.userName.text.toString().trim()
                repoName = binding.repoName.text.toString().trim()

                API = "https://api.github.com/repos/$userName/$repoName"

                Log.d("MainActivity", API!!)
                Log.d("MainActivity", userName)
                Log.d("MainActivity", repoName)
                FetchDataAsyncTask().execute(API)
            } else {
                Toast.makeText(this, "Fill Above Field", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class FetchDataAsyncTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String?): String? {
            var result: String? = null
            try {
                val url = URL(params[0])
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.connectTimeout = 5000
                urlConnection.readTimeout = 5000
                urlConnection.setRequestProperty("Content-Type", "application/json")
                urlConnection.setRequestProperty("Accept", "application/json")

                val statusCode = urlConnection.responseCode
                if (statusCode == 200) {
                    val inputStream = urlConnection.inputStream
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (bufferedReader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    result = stringBuilder.toString()

                    val sharedPreferencesData = sharedPreferences?.getString("repoList", null)

                    if (sharedPreferencesData != null) {
                        val jsonArray = JSONArray(sharedPreferencesData)

                        for (i in 0 until jsonArray.length()) {
                            val url = jsonArray.getString(i)
                            courseList.add(url)
                        }
                    }
                    courseList.add(courseList.size, API.toString())

                    val editor = sharedPreferences?.edit()
                    val gson = Gson()
                    val json: String = gson.toJson(courseList)
                    editor?.putString("repoList", json)
                    editor?.apply()
                } else {
                    Log.e("MainActivity", "Failed to fetch data from the URL")
                }

            } catch (e: IOException) {
                Log.e("MainActivity", e.message.toString())
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            if (result != null && result.isNotEmpty()) {
                Toast.makeText(this@MainActivity2, "Successfully Added", Toast.LENGTH_SHORT).show()
                onBackPressed()
            } else {
                try {
                    main()
                } catch (e: java.lang.Exception) {
                    Toast.makeText(this@MainActivity2, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun main() {
        val apiEndpoint = "https://api.github.com/repos/$userName/$repoName"
        val token = "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"
        val client = OkHttpClient.Builder().addInterceptor(AuthenticationInterceptor(token)).build()
        val request = Request.Builder().url(apiEndpoint).header("User-Agent", "MyApp").build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to retrieve repository data: ${e.message}")
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity2,
                        "Failed to retrieve data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    courseList.add(API.toString())

                    val editor = sharedPreferences?.edit()
                    val gson = Gson()
                    val json: String = gson.toJson(courseList)
                    editor?.putString("repoList", json)
                    editor?.apply()
                    runOnUiThread {
                        Toast.makeText(this@MainActivity2, "Successfully Added", Toast.LENGTH_SHORT)
                            .show()
                    }
                    setResult(2, intent)
                    finish()
                } else {
                    println("Failed to retrieve repository data: ${response.message}")
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity2,
                            "Failed to retrieve data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    class AuthenticationInterceptor(private val token: String) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request =
                chain.request().newBuilder().header("Authorization", "token $token").build()
            return chain.proceed(request)
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

    override fun onBackPressed() {
        setResult(2, intent)
        super.onBackPressed()
    }
}