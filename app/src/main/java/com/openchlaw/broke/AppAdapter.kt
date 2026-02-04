package com.openchlaw.broke

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class AppItem(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    var isSelected: Boolean
)

class AppAdapter(
    private val apps: List<AppItem>,
    private val onItemClick: (AppItem) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.imgAppIcon)
        val name: TextView = view.findViewById(R.id.tvAppName)
        val checkBox: CheckBox = view.findViewById(R.id.cbAppSelected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.name.text = app.name
        holder.icon.setImageDrawable(app.icon)
        
        // Remove listener temporarily to avoid trigger during scroll recycling
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = app.isSelected

        holder.itemView.setOnClickListener {
            app.isSelected = !app.isSelected
            holder.checkBox.isChecked = app.isSelected
            onItemClick(app)
        }
        
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            app.isSelected = isChecked
            onItemClick(app)
        }
    }

    override fun getItemCount() = apps.size
}
