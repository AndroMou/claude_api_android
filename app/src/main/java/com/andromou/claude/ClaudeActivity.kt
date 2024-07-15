package com.andromou.claude

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.andromou.claude.Utils.copyText
import com.andromou.claude.Utils.isOnline
import com.andromou.claude.Utils.showToast
import com.andromou.claude.databinding.ActivityMainBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ClaudeActivity : AppCompatActivity() {


    private var binding: ActivityMainBinding? = null
    private var copiedText = "Copied Text"

    // Retrofit instance and interface setup
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/v1/")
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val apiService: ApiService1 by lazy {
        retrofit.create(ApiService1::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)


       binding?.sendBtn?.setOnClickListener {
           sendMessage()
            }

        binding?.btnCopy?.setOnClickListener {
            copyText( this, copiedText )
        }

    }

    private fun callClaudeAPI(question: String) {
        // Retrieve API key securely (in a real scenario, retrieve it from a secure source)
        val CLAUDE_API_KEY = BuildConfig.CLAUDE_API_KEY

        // Retrofit call setup
        val jsonBody = JSONObject()
        jsonBody.put("model", "claude-3-sonnet-20240229")
        jsonBody.put("max_tokens", 100)
        jsonBody.put("messages", JSONObject().apply {
            put("role", "user")
            put("content", question)
        })

        val body = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

        // Make API call
        val call = apiService.getCompletion("Bearer $CLAUDE_API_KEY", body)
        call.enqueue(object : Callback<ClaudeResponse> {
            override fun onResponse(call: Call<ClaudeResponse>, response: Response<ClaudeResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()?.content?.firstOrNull()?.text ?: "No response"
                    addResponse(result.trim())
                    copiedText = result.trim()
                } else {
                    addResponse("Failed to load response due to ${response.errorBody()?.string()}")
                    copiedText = "Failed to load response due to ${response.errorBody()?.string()}"
                }
            }

            override fun onFailure(call: Call<ClaudeResponse>, t: Throwable) {
                addResponse("Failed to load response due to ${t.message}")
                copiedText = "Failed to load response due to ${t.message}"
            }
        })
    }


    private fun addResponse(response: String) {
        binding?.responseView?.visibility = View.VISIBLE
        binding?.responseTextView?.text = response
    }
    private fun addRequest(request: String) {
        binding?.requestView?.visibility = View.VISIBLE
        binding?.requestTextView?.text = request
    }

    private fun sendMessage() {
        val question = binding?.messageEditText?.text.toString().trim()
       if (question != null) {
            if (isOnline(this)) {
                // rewardNum++
                addRequest(question)
                callClaudeAPI(question)
                binding?.messageEditText?.setText("")
            } else {
                showToast("Please Connect to the Internet", applicationContext)
             }
        }

    }





}

// Retrofit interface
interface ApiService1 {
    @retrofit2.http.Headers("Content-Type: application/json", "anthropic-version: 2023-06-01")
    @retrofit2.http.POST("messages")
    fun getCompletion(
        @retrofit2.http.Header("Authorization") authorization: String,
        @retrofit2.http.Body body: RequestBody
    ): Call<ClaudeResponse>
}

// Data class to parse response
data class ClaudeResponse(
    val content: List<Content>
)

data class Content(
    val text: String
)
