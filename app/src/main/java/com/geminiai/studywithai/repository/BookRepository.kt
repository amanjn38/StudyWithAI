package com.geminiai.studywithai.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.geminiai.studywithai.models.Book
import com.google.gson.Gson
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import javax.inject.Inject

class BookRepository @Inject constructor(private val context: Context) {
    fun getBook(): LiveData<Book> {
        val jsonString = loadJSONFromAsset("chemistry.json")
        val book = Gson().fromJson(jsonString, Book::class.java)
        val data = MutableLiveData<Book>()
        data.value = book
        return data
    }

    private fun loadJSONFromAsset(fileName: String): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }
}
