package com.example.a1stzoom.ui

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a1stzoom.R
import com.example.a1stzoom.model.Repository
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class RepositoryAdapter(private val dataList: ArrayList<String>) :
    RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    private var repositories = ArrayList<Repository>()
    private val gson = Gson()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]

        FetchDataAsyncTask(holder).execute(data)

    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text1: TextView = itemView.findViewById(R.id.text1)
        val text2: TextView = itemView.findViewById(R.id.text2)
        val share: LinearLayout = itemView.findViewById(R.id.share)
    }

    private inner class FetchDataAsyncTask(private val holder: ViewHolder) :
        AsyncTask<String, Void, ArrayList<Repository>>() {

        override fun doInBackground(vararg params: String?): ArrayList<Repository>? {
            var result: ArrayList<Repository>? = null
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

                    val response = stringBuilder.toString()
                    val firstChar = response.trim().first()
                    val repositoryArray = if (firstChar == '[') {
                        gson.fromJson(response, Array<Repository>::class.java).toList()
                    } else {
                        listOf(gson.fromJson(response, Repository::class.java))
                    }

                    result = ArrayList(repositoryArray.toList())

                } else {
                    Log.e("MainActivity", "Failed to fetch data from the URL")
                }
            } catch (e: IOException) {
                Log.e("MainActivity", e.message.toString())
            }
            return result
        }

        override fun onPostExecute(result: ArrayList<Repository>?) {
            super.onPostExecute(result)

            if (result != null && result.isNotEmpty()) {
                repositories = result
                holder.text1.text = repositories[0].name
                holder.text2.text = repositories[0].description

                if (!repositories[0].htmlUrl!!.startsWith("http://") && !repositories[0].htmlUrl!!.startsWith(
                        "https://"
                    )
                ) repositories[0].htmlUrl = "http://" + repositories[0].htmlUrl

                holder.itemView.setOnClickListener {

                    val browserIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(repositories[0].htmlUrl))
                    holder.itemView.context.startActivity(browserIntent)
                }

                holder.share.setOnClickListener {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
                    shareIntent.putExtra(Intent.EXTRA_TEXT, repositories[0].htmlUrl)
                    holder.itemView.context.startActivity(
                        Intent.createChooser(
                            shareIntent, "Share Via"
                        )
                    )
                }
            }
        }
    }
}
