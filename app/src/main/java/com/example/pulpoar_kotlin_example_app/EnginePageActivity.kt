package com.example.pulpoar_kotlin_example_app

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class EnginePageActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private val INPUT_FILE_REQUEST_CODE = 1
    private val FILECHOOSER_RESULTCODE = 1
    private val TAG: String = EnginePageActivity::class.java.getSimpleName()
    private var mUploadMessage: ValueCallback<Uri>? = null
    private val mCapturedImageURI: Uri? = null
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var mCameraPhotoPath: String? = null
    private lateinit var mEditText : TextView
    @SuppressLint("SetJavaScriptEnabled")
    private fun showWebViewDialog(url: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_webview)

        val webView = dialog.findViewById<WebView>(R.id.dialogWebView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl(url)
        val closeButton = dialog.findViewById<Button>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        // Set the dialog size and show it
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog.show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_engine_page)
        mEditText = findViewById(R.id.editInput)
        val buttonClick = findViewById<Button>(R.id.button_back)
        buttonClick.setOnClickListener {
            val intent = Intent(this, LandingPageActivity::class.java)
            startActivity(intent)
        }

        webView = findViewById(R.id.webView)
        // setJavaScriptEnabled for execute JavaScript commands
        webView.settings.javaScriptEnabled = true
        // setDomStorageEnabled for display model images
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.setSupportZoom(false)
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.addJavascriptInterface(JSBridge(mEditText),"JSBridge")
        webView.settings.setSupportMultipleWindows(true)

        webView.webViewClient = object : WebViewClient() {
            // Override page so it's load on my view only
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return false
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler,
                error: SslError?
            ) {
                handler.proceed()
            }
        }


        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView,
                dialog: Boolean,
                userGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                var url: String? = null
                if (webView.hitTestResult.type == WebView.HitTestResult.SRC_ANCHOR_TYPE
                    || webView.hitTestResult.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                ) {
                    url = webView.hitTestResult.extra
                }
                if (url != null) {
                    showWebViewDialog(url)
                }
                return true
            }
            override fun onPermissionRequest(request: PermissionRequest) {
                println("on permission request")
                Log.e("Not err","on perm req")
                runOnUiThread { request.grant(request.resources) }
            }
            // For Android 5.0
            override fun onShowFileChooser(
                view: WebView,
                filePath: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                // Double check that we don't have any existing callbacks
                if (mFilePathCallback != null) {
                    mFilePathCallback!!.onReceiveValue(null)
                }
                mFilePathCallback = filePath
                var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent!!.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    var photoFile: File? = null
                    try {
                        val destination =
                            Environment.getExternalStorageDirectory().path + "/image.jpg"
                        takePictureIntent.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(File(destination))
                        )
                        photoFile = createImageFile()
                        takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        Log.e(
                            TAG,
                            "Unable to create Image File",
                            ex
                        )
                    }

                    // Continue only if the File was successfully created.
                    // Uri.fromFile is not supported to get camera photos starting SDK 30, use FileProvider instead.
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.absolutePath
                        val photoURI: Uri = FileProvider.getUriForFile(
                            view.context,
                            BuildConfig.APPLICATION_ID + ".provider",
                            photoFile
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    } else {
                        takePictureIntent = null
                    }
                }
                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "image/*"
                val intentArray: Array<Intent?>
                intentArray = takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls(0)
                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                startActivityForResult(
                    chooserIntent,
                    INPUT_FILE_REQUEST_CODE
                )
                return true
            }
        }
        webView.loadUrl("https://engine.pulpoar.com/engine/v0/e4c278ef-0bef-40cc-85d5-5643b179a05c")
    }

    /**
     * Receive message from webview and pass on to native.
     */
    class JSBridge(private val editTextInput: TextView){
        // We are sending function, to child.
        @JavascriptInterface
        fun postMessage(message:String){
            editTextInput.setText(message)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data)
                return
            }
            var results: Array<Uri>? = null

            // Check that the response is a good one
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = arrayOf(Uri.parse(mCameraPhotoPath))
                    }
                } else {
                    val dataString = data.dataString
                    if (dataString != null) {
                        results = arrayOf(Uri.parse(dataString))
                    }
                }
            }
            mFilePathCallback!!.onReceiveValue(results)
            mFilePathCallback = null
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data)
                return
            }
            if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == this.mUploadMessage) {
                    return
                }
                var result: Uri? = null
                try {
                    result = if (resultCode != RESULT_OK) {
                        null
                    } else {

                        // retrieve from the private variable if the intent is null
                        if (data == null) mCapturedImageURI else data.data
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        applicationContext, "activity :$e",
                        Toast.LENGTH_LONG
                    ).show()
                }
                mUploadMessage!!.onReceiveValue(result)
                mUploadMessage = null
            }
        }
        return
    }

}