package com.geminiai.studywithai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.geminiai.studywithai.api.GeminiApi
import com.geminiai.studywithai.databinding.ActivityMainBinding
import com.geminiai.studywithai.models.GeminiResponse
import com.geminiai.studywithai.models.TextRequest
import com.geminiai.studywithai.screens.ui.AskQuestionFragment
import com.geminiai.studywithai.screens.ui.HomeFragment
import com.geminiai.studywithai.screens.ui.ImageSolutionFragment
import com.geminiai.studywithai.screens.ui.PreviousYearQuestionsFragment
import com.geminiai.studywithai.utils.API_KEY
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.functions.FirebaseFunctions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var mFunctions: FirebaseFunctions
    private lateinit var geminiApi: GeminiApi
    private lateinit var client: OkHttpClient
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        mFunctions = FirebaseFunctions.getInstance()
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://us-central1-studywithai-2dce2.cloudfunctions.net/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        geminiApi = retrofit.create(GeminiApi::class.java)

        client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // Disable read timeout for SSE
            .build()


        getAnswerFromGeminiStreamed("Please explain AI", "getAnswer")
        getAnswerFromGeminiStreamed("Please explain AI", "getStreamedAnswer")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        replaceFragment(HomeFragment())
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.analytics -> replaceFragment(PreviousYearQuestionsFragment())
                R.id.cards -> replaceFragment(AskQuestionFragment())
                R.id.profile -> replaceFragment(ImageSolutionFragment())

                else -> {

                }
            }

            true
        }
//        getTextResponse()
//        generateTextFromImage()
//        generateTextFromMultipleImages()
//        builtChat()
//        getStreamedResponse()
    }

    private fun getAnswerFromGemini(text: String) {
        val request = TextRequest(text)

        val call = geminiApi.getAnswer(request)
        call?.enqueue(object : Callback<GeminiResponse?> {
            override fun onResponse(
                call: Call<GeminiResponse?>,
                response: Response<GeminiResponse?>
            ) {
                if (response.isSuccessful && response.body() != null) {

                    val geminiResponse = response.body()!!
                    println("testing1" + response.body())
                } else {
                    println(
                        "testing2" + response.errorBody().toString() + "  "
                                + response.body()
                    )

                }
            }

            override fun onFailure(call: Call<GeminiResponse?>, t: Throwable) {
                println("testing3" + t.message)
            }
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()

    }

    private fun getTextResponse() {
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
            apiKey = API_KEY
        )
        lifecycleScope.launch {
            val prompt = "Write a story about a AI and magic"
            val response = generativeModel.generateContent(prompt)
            println("testing" + response.text)
        }
    }

    private fun generateTextFromImage() {
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = API_KEY
        )

        val cookieImage: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.question)

        val inputContent = content() {
            image(cookieImage)
            text("Can you explain this to me? And can you provide the solution?")
        }
        lifecycleScope.launch {
            val response = generativeModel.generateContent(inputContent)
            println("testing1" + response.text)
        }
    }

    private fun generateTextFromMultipleImages() {
        val generativeModel = GenerativeModel(
            // The Gemini 1.5 models are versatile and work with both text-only and multimodal prompts
            modelName = "gemini-1.5-flash",
            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
            apiKey = API_KEY
        )

        val image1: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.cookie)
        val image2: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.cookie)

        val inputContent = content {
            image(image1)
            image(image2)
            text("What's different between these pictures?")
        }
        lifecycleScope.launch {
            val response = generativeModel.generateContent(inputContent)
            println("testing1" + response.text)
        }
    }

    private fun builtChat() {
        val generativeModel = GenerativeModel(
            // The Gemini 1.5 models are versatile and work with multi-turn conversations (like chat)
            modelName = "gemini-1.5-flash",
            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
            apiKey = API_KEY
        )

        val chat = generativeModel.startChat(
            history = listOf(
                content(role = "user") { text("Hey") },
                content(role = "model") { text("Great to meet you. What would you like to know?") }
            )
        )
        lifecycleScope.launch {
            val response = chat.sendMessage("Can you tell me something about KMP?")
            println("testing1" + response.text.toString())
        }
    }

    private fun getStreamedResponse() {
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = API_KEY
        )
        val cookieImage: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.question)

        val inputContent = content() {
            image(cookieImage)
            text("Can you explain this to me? And can you provide the solution?")
        }

        lifecycleScope.launch {
            var fullResponse = ""
            generativeModel.generateContentStream(inputContent).collect { chunk ->
                print(chunk.text)
                fullResponse += chunk.text
            }
        }
    }

    private fun getAnswerFromGeminiStreamed(text: String, endpoint: String) {
        val url = "https://us-central1-studywithai-2dce2.cloudfunctions.net/$endpoint"
        val requestBody = "{\"text\":\"$text\"}"
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val eventSourceListener = object : EventSourceListener() {
            private val buffer = StringBuilder()

            override fun onOpen(eventSource: EventSource, response: okhttp3.Response) {
                println("Connected to SSE server")
            }

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                buffer.append(data)
                println("Received event: $data")
            }

            override fun onClosed(eventSource: EventSource) {
                println("SSE connection closed")
                println("Complete response: $buffer")
                // Process the complete response here
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: okhttp3.Response?
            ) {
                println("SSE connection failed: ${t?.message}")
            }
        }

        EventSources.createFactory(client)
            .newEventSource(request, eventSourceListener)
    }
}