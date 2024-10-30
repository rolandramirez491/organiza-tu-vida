package com.example.organizatuvida.ui.dashboard

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.organizatuvida.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var taskList: MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Inicializamos SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE)

        // Botones
        val addTaskButton: Button = binding.buttonAddTask
        val modifyTaskButton: Button = binding.buttonModifyTask
        val textDashboard: TextView = binding.textDashboard

        // Cargar las tareas guardadas
        loadTasks(textDashboard)

        // Agregar tarea
        addTaskButton.setOnClickListener {
            showTaskDialog(null, textDashboard)
        }

        // Modificar tarea
        modifyTaskButton.setOnClickListener {
            showModifyTaskDialog(textDashboard)
        }

        return root
    }

    private fun loadTasks(textView: TextView) {
        val tasks = sharedPreferences.getStringSet("tasks", setOf())
        taskList = tasks?.toMutableList() ?: mutableListOf()
        if (taskList.isNotEmpty()) {
            textView.text = taskList.joinToString("\n")
        } else {
            textView.text = "No hay tareas guardadas"
        }
    }

    private fun saveTasks() {
        val editor = sharedPreferences.edit()
        editor.putStringSet("tasks", taskList.toSet())
        editor.apply()
    }

    private fun showTaskDialog(task: String?, textView: TextView) {
        // Crear el diálogo
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(if (task == null) "Agregar Tarea" else "Modificar Tarea")

        // Crear el layout programáticamente
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        // Crear los campos de entrada para la tarea
        val taskInput = EditText(requireContext())
        taskInput.hint = "Nombre de la tarea"
        layout.addView(taskInput)

        val priorityInput = EditText(requireContext())
        priorityInput.hint = "Prioridad (1-5)"
        layout.addView(priorityInput)

        val dateInput = EditText(requireContext())
        dateInput.hint = "Fecha de entrega"
        layout.addView(dateInput)

        // Si estamos modificando una tarea, rellenamos los campos con los valores actuales
        if (task != null) {
            val taskParts = task.split(", ")
            taskInput.setText(taskParts[0])
            priorityInput.setText(taskParts[1].replace("Prioridad: ", ""))
            dateInput.setText(taskParts[2].replace("Fecha: ", ""))
        }

        builder.setView(layout)

        // Acciones de los botones
        builder.setPositiveButton(if (task == null) "Agregar" else "Modificar") { dialog, _ ->
            val newTask = "${taskInput.text}, Prioridad: ${priorityInput.text}, Fecha: ${dateInput.text}"
            if (task == null) {
                taskList.add(newTask)
            } else {
                val taskIndex = taskList.indexOf(task)
                taskList[taskIndex] = newTask
            }
            saveTasks()
            loadTasks(textView)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showModifyTaskDialog(textView: TextView) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Modificar Tarea")

        if (taskList.isEmpty()) {
            builder.setMessage("No hay tareas para modificar")
            builder.setPositiveButton("OK", null)
        } else {
            val taskArray = taskList.toTypedArray()
            builder.setItems(taskArray) { _, which ->
                val selectedTask = taskList[which]
                showTaskDialog(selectedTask, textView)
            }
        }

        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
