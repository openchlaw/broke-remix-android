package com.openchlaw.broke

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent
    private lateinit var prefs: SharedPreferences

    // UI Elements
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var tvHeader: TextView
    private lateinit var tvSub: TextView
    private lateinit var btnLock: ImageView
    private lateinit var tvDebug: TextView
    private lateinit var btnConfigure: android.widget.Button
    private lateinit var btnPermission: android.widget.Button

    // State
    private var isBlocking = false
    private var lockedTagId: String? = null

    companion object {
        private const val PREFS_NAME = "BrokePrefs"
        private const val KEY_IS_BLOCKING = "is_blocking"
        private const val KEY_LOCKED_TAG_ID = "locked_tag_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Init UI
        rootLayout = findViewById(R.id.rootLayout)
        tvHeader = findViewById(R.id.tvStatusHeader)
        tvSub = findViewById(R.id.tvStatusSub)
        btnLock = findViewById(R.id.btnLock)
        tvDebug = findViewById(R.id.tvDebug)
        btnConfigure = findViewById(R.id.btnConfigure)
        btnPermission = findViewById(R.id.btnPermission)

        // Init Prefs
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadState()

        // Event Listeners
        btnConfigure.setOnClickListener {
            startActivity(Intent(this, AppSelectionActivity::class.java))
        }

        btnPermission.setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        // Init NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        if (nfcAdapter == null) {
            Toast.makeText(this, "This device does not support NFC", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Create PendingIntent for Foreground Dispatch
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)

        updateUI()
        
        // Manual Toggle (for testing in emulator where NFC might be hard)
        // btnLock.setOnClickListener { /* Add manual override logic if needed for debug */ }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
        checkPermissions()
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            
            val tagIdBytes = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)
            if (tagIdBytes != null) {
                val tagId = HexUtil.bytesToHex(tagIdBytes)
                handleTagScan(tagId)
            }
        }
    }

    private fun handleTagScan(tagId: String) {
        if (isBlocking) {
            // UNLOCK LOGIC
            if (tagId == lockedTagId) {
                isBlocking = false
                lockedTagId = null
                saveState()
                updateUI()
                showToast("Phone Unlocked", true)
            } else {
                showToast("Wrong Tag! Access Denied.", false)
                tvDebug.text = "Scanned: ...${tagId.takeLast(4)}"
            }
        } else {
            // LOCK LOGIC
            isBlocking = true
            lockedTagId = tagId
            saveState()
            updateUI()
            showToast("Phone Locked", true)
        }
    }

    private fun updateUI() {
        if (isBlocking) {
            tvHeader.text = "Phone Locked"
            tvSub.text = "Scan your key tag to unlock."
            btnLock.setImageResource(R.drawable.ic_lock_closed)
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.white)) // Ideally light red
            // In a real implementation, you'd change the theme or background color more drastically
        } else {
            tvHeader.text = "Phone Unlocked"
            tvSub.text = "Tap any NFC tag to lock."
            btnLock.setImageResource(R.drawable.ic_lock_open)
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            tvDebug.text = ""
        }
    }

    private fun loadState() {
        isBlocking = prefs.getBoolean(KEY_IS_BLOCKING, false)
        lockedTagId = prefs.getString(KEY_LOCKED_TAG_ID, null)
    }

    private fun saveState() {
        with(prefs.edit()) {
            putBoolean(KEY_IS_BLOCKING, isBlocking)
            putString(KEY_LOCKED_TAG_ID, lockedTagId)
            apply()
        }
    }

    private fun showToast(msg: String, success: Boolean) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissions() {
        if (!isAccessibilityServiceEnabled()) {
            btnPermission.visibility = android.view.View.VISIBLE
            tvSub.text = "Permission Required! Enable Accessibility Service to block apps."
        } else {
            btnPermission.visibility = android.view.View.GONE
            if (!isBlocking) {
                tvSub.text = "Tap the lock and scan any NFC tag to lock."
            }
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC)
        for (service in enabledServices) {
            if (service.resolveInfo.serviceInfo.packageName == packageName) {
                return true
            }
        }
        return false
    }
}
