package com.example.organizatuvida.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.organizatuvida.databinding.ItemAppUsageBinding
import com.example.organizatuvida.databinding.ItemDateHeaderBinding

class AppUsageAdapter(private val itemList: List<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Tipos de vista: encabezado de fecha o elemento de aplicación
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (itemList[position] is String) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val binding = ItemDateHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            DateHeaderViewHolder(binding)
        } else {
            val binding = ItemAppUsageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            AppUsageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DateHeaderViewHolder) {
            holder.bind(itemList[position] as String)
        } else if (holder is AppUsageViewHolder) {
            holder.bind(itemList[position] as AppUsageInfo)
        }
    }

    override fun getItemCount(): Int = itemList.size

    // ViewHolder para los encabezados de fecha
    inner class DateHeaderViewHolder(private val binding: ItemDateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            binding.dateHeader.text = "Día: $date"
        }
    }

    // ViewHolder para los elementos de aplicaciones
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
