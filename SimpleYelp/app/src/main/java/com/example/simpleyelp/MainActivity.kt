package com.example.simpleyelp

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.yelp.com/v3/"
private const val API_KEY = "_uUZH5tAEZnC2SF3u7AII2VNCM5327VYFlYMGXYUZCj7-glheuW4H0ZtPv_PNHp-KgaH7-VcOzhY6-NQlXwDY4ePri4vHfWeybm6PoKQdL755E23LbENN0oKsj2kX3Yx"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!isOnline()) {
            AlertDialog.Builder(this)
                .setTitle("No Internet")
                .setMessage("Device is not connected to the internet :(")
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }

        val restaurants = mutableListOf<YelpRestaurant>()
        val adapter = RestaurantsAdapter(this, restaurants)
        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager(this)

        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val yelpService = retrofit.create(YelpService::class.java)
        yelpService.searchRestaurants("Bearer $API_KEY","Avocado Toast", "New York").enqueue(object : Callback<YelpSearchResult> {
            override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                Log.i(TAG, "onFailure $t")
            }

            override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                Log.i(TAG, "onResponse $response")
                val body = response.body()
                if (body == null) {
                    Log.w(TAG, "Did not receive valid repsonse body")
                    return
                }
                restaurants.addAll(body.restaurants)
                adapter.notifyDataSetChanged()
            }
        })

    }
}

fun isOnline(): Boolean {
    val runtime = Runtime.getRuntime()
    try {
        val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
        val exitValue = ipProcess.waitFor()
        return exitValue == 0
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
    return false
}