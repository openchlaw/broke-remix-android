package com.openchlaw.broke

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class AppSelectionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSave: Button
    private lateinit var adapter: AppAdapter
    private val appList = mutableListOf<AppItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_selection)

        recyclerView = findViewById(R.id.recyclerViewApps)
        btnSave = findViewById(R.id.btnSaveApps)

        recyclerView.layoutManager = LinearLayoutManager(this)
        
        loadApps()

        btnSave.setOnClickListener {
            saveSelection()
            finish()
        }
    }

    private fun loadApps() {
        val pm = packageManager
        // Get already blocked apps
        val prefs = getSharedPreferences("BrokePrefs", Context.MODE_PRIVATE)
        val blockedSet = prefs.getStringSet("blocked_packages", emptySet()) ?: emptySet()

        // Fetch installed apps
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        for (appInfo in packages) {
            // Filter: Only show apps that can be launched (have a UI) and exclude system apps (mostly)
            // Or allow system apps but be careful. 
            // Simple filter: if it has an icon and launch intent
            if (pm.getLaunchIntentForPackage(appInfo.packageName) != null) {
                // Skip our own app
                if (appInfo.packageName == packageName) continue

                val name = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getApplicationIcon(appInfo)
                val isSelected = blockedSet.contains(appInfo.packageName)

                appList.add(AppItem(name, appInfo.packageName, icon, isSelected))
            }
        }

        // Sort alphabetically
        appList.sortBy { it.name }

        adapter = AppAdapter(appList) { _ -> 
            // Item clicked/toggled, state is updated in the object
        }
        recyclerView.adapter = adapter
    }

    private fun saveSelection() {
        val prefs = getSharedPreferences("BrokePrefs", Context.MODE_PRIVATE)
        val selectedPackages = appList.filter { it.isSelected }.map { it.packageName }.toSet()
        
        with(prefs.edit()) {
            putStringSet("blocked_packages", selectedPackages)
            apply()
        }
    }
}
