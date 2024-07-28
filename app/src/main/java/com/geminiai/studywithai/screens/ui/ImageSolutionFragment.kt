package com.geminiai.studywithai.screens.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.geminiai.studywithai.R
import com.geminiai.studywithai.databinding.FragmentImageSolutionBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class ImageSolutionFragment : Fragment() {
    private var _binding: FragmentImageSolutionBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_UPLOAD = 2
    private var imageBitmap: Bitmap? = null
    private lateinit var client: OkHttpClient
    private var editTextPrompt: EditText? = null
    private var imageView: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentImageSolutionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

//        client = OkHttpClient.Builder()
//            .connectTimeout(30, TimeUnit.SECONDS)
//            .writeTimeout(30, TimeUnit.SECONDS)
//            .readTimeout(0, TimeUnit.SECONDS) // Disable read timeout for SSE
//            .build()


        client = getUnsafeOkHttpClient()
        binding.btnCaptureImage.setOnClickListener {
            checkPermissions()
        }

        binding.btnUploadImage.setOnClickListener {
            dispatchUploadPictureIntent()
        }

        binding.btnSendImage.setOnClickListener {
            sendImageToCloudFunction()
        }

    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(context as Activity, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Request CAMERA permission if it has not been granted
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            // Permission has already been granted, proceed with camera operations
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun dispatchUploadPictureIntent() {
        Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ).also { uploadPictureIntent ->
            uploadPictureIntent.type = "image/*"
            startActivityForResult(uploadPictureIntent, REQUEST_IMAGE_UPLOAD)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val extras = data?.extras
                    imageBitmap = extras?.get("data") as Bitmap?
                    binding.imageView.setImageBitmap(imageBitmap)
                }
                REQUEST_IMAGE_UPLOAD -> {
                    val imageUri: Bitmap? = data?.data?.let { uri ->
                        MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                    }
                    System.out.println("testing1" + imageUri.toString())
                    imageBitmap = imageUri
                    binding.imageView.setImageBitmap(imageBitmap)
                }
            }
        }
    }
    private fun sendImageToCloudFunction() {
        val prompt = editTextPrompt?.text.toString().ifEmpty { "Solve this question" }
        val url =
            "https://us-central1-studywithai-2dce2.cloudfunctions.net/getAnswerFromImage"

        val byteArrayOutputStream = ByteArrayOutputStream()
        imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

        val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("prompt", prompt)
            .addFormDataPart(
                "file", "image.jpg",
                RequestBody.create(
                    "image/jpeg".toMediaTypeOrNull(),
                    byteArrayOutputStream.toByteArray()
                )
            )
            .build()

        val request = Request.Builder()
            .url(url)
            .post(multipartBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    println("Response: $responseData")
                } else {
                    println("Request failed: ${response.message}")
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with camera operations
                dispatchTakePictureIntent()
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                // Optionally, you can disable features that require the permission
            }
        }
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL").apply {
                init(null, trustAllCerts, java.security.SecureRandom())
            }

            val sslSocketFactory = sslContext.socketFactory

            return OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 101
    }
}