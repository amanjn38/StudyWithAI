package com.geminiai.studywithai

import android.app.Application
import com.google.firebase.FirebaseApp
import com.pdftron.pdf.PDFNet
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StudyWithAIApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        PDFBoxResourceLoader.init(getApplicationContext());
        FirebaseApp.initializeApp(this);
        PDFNet.initialize("demo:1721127842797:7f9a2f1f0300000000c13d7c59402a5441cd1649bed21617eed1219e85")
    }
}