package com.example.organizatuvida.ui.dashboard

import android.graphics.Color
import java.text.SimpleDateFormat
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.organizatuvida.R
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.util.*

class DashboardFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var taskList: MutableList<String>
    private lateinit var taskDetails: MutableList<String>
    private lateinit var taskDisplay: LinearLayout
    private var selectedPriority: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Inicializar SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE)

        // Inicializar listas de tareas
        taskList = mutableListOf()
        taskDetails = mutableListOf()
        loadTasks()

        // Vincular vista
        taskDisplay = view.findViewById(R.id.taskDisplay)

        // Mostrar tareas automáticamente al cargar la vista
        showTodayTasks()

        // Configurar botones
        view.findViewById<Button>(R.id.addTaskButton).setOnClickListener { showAddTaskDialog() }
        view.findViewById<Button>(R.id.showTaskButton).setOnClickListener { showTasks() }
        view.findViewById<Button>(R.id.modifyTaskButton).setOnClickListener { showModifyTaskDialog() }
        view.findViewById<Button>(R.id.deleteTaskButton).setOnClickListener { showDeleteTaskDialog() }

        return view
    }

    // Muestra las tareas del día actual
    private fun showTodayTasks() {
        // Obtener la fecha de hoy en formato dd/MM/yyyy
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)

        // Limpiar el LinearLayout antes de agregar las tareas
        taskDisplay.removeAllViews()

        // Iterar sobre las tareas y mostrar las que coinciden con la fecha actual
        for (i in taskDetails.indices) {
            val taskDetail = taskDetails[i]
            val taskDate = taskDetail.split("\n")[1].removePrefix("Fecha De Entrega: ")

            // Comparar las fechas
            if (taskDate == currentDate) {
                val taskTextView = TextView(requireContext()).apply {
                    text = taskDetail
                    textSize = 16f
                    setPadding(0, 8, 0, 8)
                }
                taskDisplay.addView(taskTextView)
            }
        }

        // Si no hay tareas para hoy, mostrar un mensaje
        if (taskDisplay.childCount == 0) {
            val noTasksTextView = TextView(requireContext()).apply {
                text = "No tienes tareas para hoy."
                textSize = 16f
                setPadding(0, 8, 0, 8)
            }
            taskDisplay.addView(noTasksTextView)
        }
    }

    // Muestra un diálogo para agregar una tarea
    private fun showAddTaskDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_task, null)

        val taskTextInput: EditText = view.findViewById(R.id.taskTextInput)
        val dateTextInput: EditText = view.findViewById(R.id.dateTextInput)
        val timeTextInput: EditText = view.findViewById(R.id.timeTextInput)

        // Configurar los campos de fecha y hora
        dateTextInput.setOnClickListener { openDatePicker(dateTextInput) }
        timeTextInput.setOnClickListener { openTimePicker(timeTextInput) }

        // Configurar botones de prioridad
        val priorityButtons = listOf(
            view.findViewById<Button>(R.id.priorityButton1),
            view.findViewById<Button>(R.id.priorityButton2),
            view.findViewById<Button>(R.id.priorityButton3),
            view.findViewById<Button>(R.id.priorityButton4),
            view.findViewById<Button>(R.id.priorityButton5)
        )

        priorityButtons.forEachIndexed { index, button ->
            button.setOnClickListener { selectedPriority = index + 1 }
        }

        // Crear el AlertDialog
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Agregar Tarea")
            .setView(view)
            .setPositiveButton("Agregar") { _, _ ->
                val task = taskTextInput.text.toString()
                val date = dateTextInput.text.toString()
                val time = timeTextInput.text.toString()
                if (task.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty()) {
                    taskList.add(task)
                    taskDetails.add("Tarea: $task\nFecha De Entrega: $date\nHora De Entrega: $time\nPrioridad: $selectedPriority")
                    saveTasks()
                    showTodayTasks()  // Actualiza la lista de tareas después de agregar una nueva
                } else {
                    Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    // Muestra un diálogo para modificar una tarea
    private fun showModifyTaskDialog() {
        if (taskList.isEmpty()) {
            Toast.makeText(requireContext(), "No hay tareas para modificar", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Selecciona la tarea a modificar")

        builder.setItems(taskList.toTypedArray()) { dialog, which ->
            val selectedTask = taskList[which]
            val details = taskDetails[which].split("\n")
            val task = details[0].removePrefix("Tarea: ")
            val date = details[1].removePrefix("Fecha De Entrega: ")
            val time = details[2].removePrefix("Hora De Entrega: ")
            val priority = details[3].removePrefix("Prioridad: ").toIntOrNull() ?: 1

            showEditTaskDialog(task, date, time, priority, which)
            dialog.dismiss()
        }
        builder.show()
    }

    // Muestra un diálogo para editar una tarea
    private fun showEditTaskDialog(task: String, date: String, time: String, priority: Int, position: Int) {
        val view = layoutInflater.inflate(R.layout.dialog_add_task, null)

        val taskTextInput: EditText = view.findViewById(R.id.taskTextInput)
        val dateTextInput: EditText = view.findViewById(R.id.dateTextInput)
        val timeTextInput: EditText = view.findViewById(R.id.timeTextInput)

        // Inicializa los campos con los valores de la tarea seleccionada
        taskTextInput.setText(task)
        dateTextInput.setText(date)
        timeTextInput.setText(time)

        // Configurar los campos de fecha y hora
        dateTextInput.setOnClickListener { openDatePicker(dateTextInput) }
        timeTextInput.setOnClickListener { openTimePicker(timeTextInput) }

        // Configurar botones de prioridad
        selectedPriority = priority
        val priorityButtons = listOf(
            view.findViewById<Button>(R.id.priorityButton1),
            view.findViewById<Button>(R.id.priorityButton2),
            view.findViewById<Button>(R.id.priorityButton3),
            view.findViewById<Button>(R.id.priorityButton4),
            view.findViewById<Button>(R.id.priorityButton5)
        )

        priorityButtons.forEachIndexed { index, button ->
            button.setOnClickListener { selectedPriority = index + 1 }
        }

        // Crear el AlertDialog
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Modificar Tarea")
            .setView(view)
            .setPositiveButton("Guardar cambios") { _, _ ->
                val updatedTask = taskTextInput.text.toString()
                val updatedDate = dateTextInput.text.toString()
                val updatedTime = timeTextInput.text.toString()
                if (updatedTask.isNotEmpty() && updatedDate.isNotEmpty() && updatedTime.isNotEmpty()) {
                    taskList[position] = updatedTask
                    taskDetails[position] = "Tarea: $updatedTask\nFecha De Entrega: $updatedDate\nHora De Entrega: $updatedTime\nPrioridad: $selectedPriority"
                    saveTasks()
                } else {
                    Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    // Muestra un diálogo para eliminar una tarea
    private fun showDeleteTaskDialog() {
        if (taskList.isEmpty()) {
            Toast.makeText(requireContext(), "No hay tareas para eliminar", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Selecciona la tarea a eliminar")

        builder.setItems(taskList.toTypedArray()) { dialog, which ->
            // Eliminar la tarea seleccionada de las listas
            taskList.removeAt(which)
            taskDetails.removeAt(which)

            // Guardar los cambios en SharedPreferences
            saveTasks()

            Toast.makeText(requireContext(), "Tarea eliminada", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.show()
    }

    private fun openDatePicker(dateTextInput: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            dateTextInput.setText("${selectedDay}/${selectedMonth + 1}/$selectedYear")
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun openTimePicker(timeTextInput: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val hourFormatted = if (selectedHour > 12) selectedHour - 12 else selectedHour
            val time = String.format("%02d:%02d %s", hourFormatted, selectedMinute, if (selectedHour >= 12) "PM" else "AM")
            timeTextInput.setText(time)
        }, hour, minute, false)

        timePickerDialog.show()
    }

    private fun showTasks() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Tareas")
        builder.setItems(taskDetails.toTypedArray(), null)
        builder.setPositiveButton("OK", null)
        builder.show()
    }

    private fun saveTasks() {
        val taskSet = taskList.mapIndexed { index, task ->
            val details = taskDetails[index].split("\n")
            "$task::${details[1].removePrefix("Fecha De Entrega: ")}::${details[2].removePrefix("Hora De Entrega: ")}::${details[3].removePrefix("Prioridad: ")}"
        }.toSet()
        sharedPreferences.edit().putStringSet("tasks", taskSet).apply()
    }

    private fun loadTasks() {
        val taskSet = sharedPreferences.getStringSet("tasks", emptySet())
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        taskSet?.forEach { entry ->
            val parts = entry.split("::")
            if (parts.size == 4) {
                val taskDate = parts[1]
                if (taskDate == currentDate) {
                    taskList.add(parts[0])
                    taskDetails.add("Tarea: ${parts[0]}\nFecha De Entrega: ${parts[1]}\nHora De Entrega: ${parts[2]}\nPrioridad: ${parts[3]}")
                }
            }
        }
    }

}
