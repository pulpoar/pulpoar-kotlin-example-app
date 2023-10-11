package com.example.pulpoar_kotlin_example_app

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast


class LandingPageActivity : AppCompatActivity() {


    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }
    public fun changeScreen(intent: Intent){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            startActivity(intent)
        }else{
            checkPermission(Manifest.permission.CAMERA,
                CAMERA_PERMISSION_CODE)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_landing_page)
        checkPermission(Manifest.permission.CAMERA,
            CAMERA_PERMISSION_CODE)
        val engineButtonClick = findViewById<Button>(R.id.engine_button)
        engineButtonClick.setOnClickListener {
            val intent = Intent(this, EnginePageActivity::class.java)
            changeScreen(intent)
        }

        val pluginButtonClick = findViewById<Button>(R.id.product_detail_button)
            pluginButtonClick.setOnClickListener {
            val intent = Intent(this, PluginPageActivity::class.java)
            changeScreen(intent)
        }
    }

    private fun rerequestPerm(){

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setMessage("Camera Permission")
            .setTitle("Permission is required to apply makeup on your face.")
            .setPositiveButton("OK") { dialog, which ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("NO") { dialog, which ->
                dialog.cancel()
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
        //Toast.makeText(this, "Camera Permission Denied, Go setting and enable it", Toast.LENGTH_SHORT).show()

    }

    private fun checkPermission(permission: String, requestCode: Int) {
        Log.i("APPP",ContextCompat.checkSelfPermission(this, permission).toString() )
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            Log.i("APPP2",PackageManager.PERMISSION_GRANTED.toString() )
            // Requesting the permission
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else {
            rerequestPerm()
        }
    }



    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            Log.i("APPP2",grantResults[0].toString())
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                rerequestPerm()
            }
        }
    }


}