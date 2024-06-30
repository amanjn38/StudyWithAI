package com.geminiai.studywithai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.geminiai.studywithai.utils.API_KEY
import com.google.ai.client.generativeai.GenerativeModel
import com.geminiai.studywithai.utils.Constants
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        getTextResponse()
//        generateTextFromImage()
//        generateTextFromMultipleImages()
//        builtChat()
        getStreamedResponse()
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

        val cookieImage: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.cookie)

        val inputContent = content() {
            image(cookieImage)
            text("Can you explain this to me?")
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

        val inputContent = content {
            text("Write a story about a magic backpack.")
        }

        lifecycleScope.launch {
            var fullResponse = ""
            generativeModel.generateContentStream(inputContent).collect { chunk ->
                print(chunk.text)
                fullResponse += chunk.text
            }
        }
    }
}