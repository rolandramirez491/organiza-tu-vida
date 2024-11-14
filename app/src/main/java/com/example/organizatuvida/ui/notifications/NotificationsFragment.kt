package com.example.organizatuvida.ui.notifications

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.organizatuvida.databinding.FragmentNotificationsBinding
import java.text.SimpleDateFormat
import java.util.*

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskList: List<Task>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar y ordenar las tareas
        loadTasks()

        if (taskList.isNotEmpty()) {
            // Ordenar las tareas por el tiempo restante
            val sortedTasks = taskList.sortedBy { calculateTimeRemainingInMillis(it.dueDate, it.dueTime) }

            // Generar el texto de notificaciones con el tiempo restante
            val notificationsText = sortedTasks.joinToString(separator = "\n") { task ->
                val timeRemaining = formatTimeRemaining(calculateTimeRemainingInMillis(task.dueDate, task.dueTime))
                "${task.name}: $timeRemaining"
            }
            binding.textNotifications.text = notificationsText
        } else {
            Toast.makeText(requireContext(), "No hay tareas para mostrar", Toast.LENGTH_SHORT).show()
        }
    }

    // Calcula el tiempo restante en milisegundos para una tarea
    private fun calculateTimeRemainingInMillis(dueDateString: String, dueTimeString: String): Long {
        val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return try {
            val dueDateTime = dateTimeFormat.parse("$dueDateString $dueTimeString")
            val currentDate = Date()
            if (dueDateTime != null) {
                dueDateTime.time - currentDate.time
            } else {
                Long.MAX_VALUE // Si no se puede parsear la fecha, la enviamos al final de la lista
            }
        } catch (e: Exception) {
            Long.MAX_VALUE // Maneja fechas no válidas
        }
    }

    // Formatea el tiempo restante en días, horas y minutos
    private fun formatTimeRemaining(timeInMillis: Long): String {
        return if (timeInMillis > 0) {
            val days = timeInMillis / (1000 * 60 * 60 * 24)
            val hours = (timeInMillis / (1000 * 60 * 60)) % 24
            val minutes = (timeInMillis / (1000 * 60)) % 60
            "${days}d ${hours}h ${minutes}m restantes"
        } else {
            "Fecha pasada"
        }
    }

    // Carga las tareas desde SharedPreferences
    private fun loadTasks() {
        val sharedPreferences = requireActivity().getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE)
        val taskSet = sharedPreferences.getStringSet("tasks", emptySet())
        val loadedTasks = mutableListOf<Task>()

        taskSet?.forEach { entry ->
            val parts = entry.split("::")
            if (parts.size == 4) {
                val name = parts[0]
                val dueDate = parts[1] // Formato esperado: "dd/MM/yyyy"
                val dueTime = parts[2] // Formato esperado: "HH:mm"
                loadedTasks.add(Task(name, dueDate, dueTime))
            }
        }

        taskList = loadedTasks
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class Task(val name: String, val dueDate: String, val dueTime: String)
}
