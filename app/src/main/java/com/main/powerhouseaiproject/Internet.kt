package com.main.powerhouseaiproject

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.widget.Toast

class NetworkChangeReceiver(private val callback: ConnectivityCallback) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            callback.onInternetConnected()
        }
    }
}
