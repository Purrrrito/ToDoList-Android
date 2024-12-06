package com.example.todolistapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class StoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_store)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val pointsTextView = findViewById<TextView>(R.id.textView_points)
        val backButton = findViewById<Button>(R.id.button_back)

        // Load points when StoreActivity starts
        val sharedPreferenes = getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val points = sharedPreferenes.getInt("points", 0)
        pointsTextView.text = "Points: $points"

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val points = sharedPreferences.getInt("points", 0)
        findViewById<TextView>(R.id.textView_points).text = "Points: $points"
    }
}