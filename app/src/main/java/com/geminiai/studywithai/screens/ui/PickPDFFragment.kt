package com.geminiai.studywithai.screens.ui

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.geminiai.studywithai.R
import com.geminiai.studywithai.utils.API_KEY
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import java.io.IOException

class PickPDFFragment : Fragment() {

//    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
//        uri?.let {
//            handlePdfUri(it)
//        }
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_pick_p_d_f, container, false)
//        view.findViewById<Button>(R.id.uploadPdfButton).setOnClickListener {
//            pickPdfFile()
//        }
        return view;
    }

//    private fun pickPdfFile() {
//        filePickerLauncher.launch(arrayOf("application/pdf"))
//    }

//    private fun handlePdfUri(uri: Uri) {
//        // Use the URI to read the PDF content
//        val pdfContent = readPdfContent(uri)
//        generateResponse(pdfContent)
//    }

//    private fun readPdfContent(uri: Uri): String {
//        val contentResolver = requireContext().contentResolver
//        val inputStream = contentResolver.openInputStream(uri) ?: throw IOException("Unable to open input stream")
//
//        val pdfiumCore = PdfiumCore(requireContext())
//        val pdfDocument = pdfiumCore.newDocument(inputStream)
//
//        val pageCount = pdfiumCore.getPageCount(pdfDocument)
//        val contentBuilder = StringBuilder()
//
//        for (i in 0 until pageCount) {
//            pdfiumCore.openPage(pdfDocument, i)
//            val pageWidth = pdfiumCore.getPageWidthPoint(pdfDocument, i)
//            val pageHeight = pdfiumCore.getPageHeightPoint(pdfDocument, i)
//            val pageText = pdfiumCore.extractText(pdfDocument, i, 0, 0, pageWidth, pageHeight)
//            contentBuilder.append(pageText)
//        }
//
//        pdfiumCore.closeDocument(pdfDocument)
//        return contentBuilder.toString()
//    }

    private fun generateResponse(pdfContent: String) {
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = API_KEY
        )

        lifecycleScope.launch {
            val prompt = "Generate a summary based on the following content:\n$pdfContent"
            val response = generativeModel.generateContent(prompt)
            println("Response: ${response.text}")
        }
    }
}