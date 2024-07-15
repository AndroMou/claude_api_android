package com.andromou.claude


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
 import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast

object Utils {
    fun showToast(message: String, context: Context) {
        val toast = Toast(context)
        val view = LayoutInflater.from(context).inflate(R.layout.custom_toast, null)
        val textView: TextView = view.findViewById(R.id.custom_toast_text)
        textView.text = message
        toast.view = view
        toast.setGravity(Gravity.CENTER or Gravity.CENTER, 0, 0)
        toast.duration = Toast.LENGTH_LONG
        toast.show()
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            } else {
                val networkInfo = connectivityManager.activeNetworkInfo
                return networkInfo?.isConnectedOrConnecting == true
            }
        }
        return false
    }
    fun copyText(context: Context, mText: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clipData = ClipData.newPlainText("text", mText)
        clipboardManager?.setPrimaryClip(clipData)
        showToast("Text is Copied", context)
    }
}
