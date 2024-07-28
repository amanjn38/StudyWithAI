package com.geminiai.studywithai.screens.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geminiai.studywithai.R
import android.app.Activity
import android.content.Intent
import android.graphics.PointF
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.pdftron.common.PDFNetException
import com.pdftron.pdf.PDFDoc
import com.pdftron.pdf.PDFViewCtrl
import com.pdftron.pdf.annots.FileAttachment
import com.pdftron.pdf.config.ToolManagerBuilder
import com.pdftron.pdf.tools.Tool
import com.pdftron.pdf.tools.ToolManager
import com.pdftron.pdf.utils.AppUtils
import com.pdftron.pdf.utils.RequestCode
import com.pdftron.pdf.utils.Utils
import com.pdftron.pdf.utils.ViewerUtils
import com.pdftron.pdf.widget.preset.component.PresetBarComponent
import com.pdftron.pdf.widget.preset.component.PresetBarViewModel
import com.pdftron.pdf.widget.preset.component.view.PresetBarView
import com.pdftron.pdf.widget.preset.signature.SignatureViewModel
import com.pdftron.pdf.widget.toolbar.ToolManagerViewModel
import com.pdftron.pdf.widget.toolbar.builder.AnnotationToolbarBuilder
import com.pdftron.pdf.widget.toolbar.builder.ToolbarButtonType
import com.pdftron.pdf.widget.toolbar.component.AnnotationToolbarComponent
import com.pdftron.pdf.widget.toolbar.component.AnnotationToolbarViewModel
import com.pdftron.pdf.widget.toolbar.component.DefaultToolbars
import com.pdftron.pdf.widget.toolbar.component.view.AnnotationToolbarView
import java.io.File
import java.io.FileOutputStream

class PDFTronActivity : AppCompatActivity(), ToolManager.AdvancedAnnotationListener {
    private val TAG = PDFTronActivity::class.java.name

    private lateinit var mPdfViewCtrl: PDFViewCtrl
    private var mPdfDoc: PDFDoc? = null
    private lateinit var mToolManager: ToolManager
    private lateinit var mAnnotationToolbarComponent: AnnotationToolbarComponent
    private lateinit var mPresetBarComponent: PresetBarComponent
    private lateinit var mToolbarContainer: FrameLayout
    private lateinit var mPresetContainer: FrameLayout

    private var mImageCreationMode: ToolManager.ToolMode? = null
    private var mAnnotTargetPoint: PointF? = null
    private var mOutputFileUri: android.net.Uri? = null
    private var mImageStampDelayCreation = false
    private var mAnnotIntentData: Intent? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pdftron)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mPdfViewCtrl = findViewById(R.id.pdfviewctrl)
        mToolbarContainer = findViewById(R.id.annotation_toolbar_container)
        mPresetContainer = findViewById(R.id.preset_container)
        setupToolManager()
        setupAnnotationToolbar()
        try {
            AppUtils.setupPDFViewCtrl(mPdfViewCtrl)
            viewFromAsset("jee_mains.pdf")  // Replace with your actual asset file name
        } catch (e: PDFNetException) {
            Log.e(TAG, "Error setting up PDFViewCtrl")
        }
    }


    /**
     * Helper method to copy a file from the assets folder to a local file
     *
     * @param fileName name of the asset file
     * @return File object pointing to the copied file
     */
    private fun copyAssetToFile(fileName: String): File {
        val assetManager = assets
        val file = File(filesDir, fileName)
        assetManager.open(fileName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file
    }

    /**
     * Helper method to view a PDF document from the assets folder
     *
     * @param assetFileName name of the asset PDF file
     * @throws PDFNetException if invalid document path is supplied to PDFDoc
     */
    @Throws(PDFNetException::class)
    private fun viewFromAsset(assetFileName: String) {
        val file = copyAssetToFile(assetFileName)
        mPdfDoc = PDFDoc(file.absolutePath)
        mPdfViewCtrl.doc = mPdfDoc
    }

    override fun fileAttachmentSelected(attachment: FileAttachment?) {
        TODO("Not yet implemented")
    }

    override fun freehandStylusUsedFirstTime() {
        TODO("Not yet implemented")
    }

    override fun imageStamperSelected(targetPoint: PointF?) {
        mImageCreationMode = ToolManager.ToolMode.STAMPER
        mAnnotTargetPoint = targetPoint
        mOutputFileUri = ViewerUtils.openImageIntent(this)
    }

    override fun imageSignatureSelected(targetPoint: PointF?, targetPage: Int, widget: Long?) {
        TODO("Not yet implemented")
    }

    override fun attachFileSelected(targetPoint: PointF?) {
        TODO("Not yet implemented")
    }

    override fun freeTextInlineEditingStarted() {
        TODO("Not yet implemented")
    }

    override fun newFileSelectedFromTool(filePath: String?, pageNumber: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun fileCreated(
        fileLocation: String?,
        action: ToolManager.AdvancedAnnotationListener.AnnotAction?
    ) {
        TODO("Not yet implemented")
    }

    private fun setupToolManager() {
        mToolManager = ToolManagerBuilder.from()
            .build(this, mPdfViewCtrl)
        mToolManager.setAdvancedAnnotationListener(this)
        mPdfViewCtrl.toolManager = mToolManager
    }

    private fun setupAnnotationToolbar() {
        val toolManagerViewModel = ViewModelProvider(this)[ToolManagerViewModel::class.java]
        toolManagerViewModel.toolManager = mToolManager
        val signatureViewModel = ViewModelProvider(this)[SignatureViewModel::class.java]
        val presetViewModel = ViewModelProvider(this)[PresetBarViewModel::class.java]
        val annotationToolbarViewModel =
            ViewModelProvider(this)[AnnotationToolbarViewModel::class.java]

        // Create our UI components for the annotation toolbar and preset bar
        mAnnotationToolbarComponent = AnnotationToolbarComponent(
            this,
            annotationToolbarViewModel,
            presetViewModel,
            toolManagerViewModel,
            AnnotationToolbarView(mToolbarContainer)
        )

        mPresetBarComponent = PresetBarComponent(
            this,
            supportFragmentManager,
            presetViewModel,
            toolManagerViewModel,
            signatureViewModel,
            PresetBarView(mPresetContainer)
        )

        // Create our custom toolbar and pass it to the annotation toolbar UI component
        mAnnotationToolbarComponent.inflateWithBuilder(
            AnnotationToolbarBuilder.withTag("Custom Toolbar")
                .addToolButton(ToolbarButtonType.INK, DefaultToolbars.ButtonId.INK.value())
                .addToolButton(ToolbarButtonType.ERASER, DefaultToolbars.ButtonId.ERASER.value())
                .addToolStickyButton(ToolbarButtonType.UNDO, DefaultToolbars.ButtonId.UNDO.value())
                .addToolStickyButton(ToolbarButtonType.REDO, DefaultToolbars.ButtonId.REDO.value())
        )
    }

    /**
     * Helper method to view a PDF document from resource
     *
     * @param resourceId of the sample PDF file
     * @param fileName   of the temporary PDF file copy
     * @throws PDFNetException if invalid document path is supplied to PDFDoc
     */
    @Throws(PDFNetException::class)
    private fun viewFromResource(resourceId: Int, fileName: String) {
        val file = Utils.copyResourceToLocal(this, resourceId, fileName, ".pdf")
        mPdfDoc = PDFDoc(file.absolutePath)
        mPdfViewCtrl.doc = mPdfDoc
        // Alternatively, you can open the document using Uri:
        // val fileUri = Uri.fromFile(file)
        // mPdfDoc = mPdfViewCtrl.openPDFUri(fileUri, null)
    }

    override fun onPause() {
        super.onPause()
        mPdfViewCtrl.pause()
        mPdfViewCtrl.purgeMemory()
    }

    override fun onResume() {
        super.onResume()
        mPdfViewCtrl.resume()

        if (mImageStampDelayCreation) {
            mImageStampDelayCreation = false
            ViewerUtils.createImageStamp(
                this,
                mAnnotIntentData,
                mPdfViewCtrl,
                mOutputFileUri,
                mAnnotTargetPoint
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mPdfViewCtrl.destroy()
        mPdfDoc?.close()
        mPdfDoc = null
    }
}