package com.example.organizatuvida.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.organizatuvida.databinding.ItemAppUsageBinding

class AppUsageAdapter(private val appUsageList: List<AppUsageInfo>) :
    RecyclerView.Adapter<AppUsageAdapter.AppUsageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppUsageViewHolder {
        val binding = ItemAppUsageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppUsageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppUsageViewHolder, position: Int) {
        val appUsage = appUsageList[position]
        holder.bind(appUsage)
    }

    override fun getItemCount(): Int {
        return appUsageList.size
    }

    inner class AppUsageViewHolder(private val binding: ItemAppUsageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appUsageInfo: AppUsageInfo) {
            val usageTimeInMinutes = appUsageInfo.usageTimeInSeconds / 60 // Minutos completos
            val secondsRemainder = appUsageInfo.usageTimeInSeconds % 60   // Segundos restantes

            binding.appName.text = appUsageInfo.packageName
            binding.usageTime.text = "Tiempo de uso: $usageTimeInMinutes min ${secondsRemainder} seg."
            binding.lastUsed.text = "Última vez: ${appUsageInfo.lastUsedTime}"
        }
    }
}
