package com.example.pulpoar_kotlin_example_app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent

class LandingPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing_page)

        val engineButtonClick = findViewById<Button>(R.id.engine_button)
        engineButtonClick.setOnClickListener {
            val intent = Intent(this, EnginePageActivity::class.java)
            startActivity(intent)
        }

        val pluginButtonClick = findViewById<Button>(R.id.product_detail_button)
            pluginButtonClick.setOnClickListener {
            val intent = Intent(this, PluginPageActivity::class.java)
            startActivity(intent)
        }
    }
}