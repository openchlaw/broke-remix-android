package com.openchlaw.broke

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BlockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block)

        val packageName = intent.getStringExtra("blocked_package") ?: "Unknown App"
        
        findViewById<TextView>(R.id.tvBlockedMsg).text = "Access to this app is restricted."
        
        // "Go Home" button to escape the loop
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)
        }
    }

    // Override back button to do nothing or go home, to prevent returning to the blocked app
    override fun onBackPressed() {
        // Go home instead of back
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
        super.onBackPressed()
    }
}
