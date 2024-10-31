package com.example.organizatuvida.ui.dashboard

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
import com.example.organizatuvida.R
import android.app.AlertDialog

class DashboardFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var taskList: MutableList<String>
    private lateinit var taskDetails: MutableList<String>

    private lateinit var taskTextInput: EditText
    private lateinit var dateTextInput: EditText
    private lateinit var timeTextInput: EditText
    private lateinit var taskDisplay: LinearLayout

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

        // Vincular vistas
        taskTextInput = view.findViewById(R.id.taskTextInput)
        dateTextInput = view.findViewById(R.id.dateTextInput)
        timeTextInput = view.findViewById(R.id.timeTextInput)
        taskDisplay = view.findViewById(R.id.taskDisplay)

        // Configurar botones
        view.findViewById<Button>(R.id.addTaskButton).setOnClickListener { addTask() }
        view.findViewById<Button>(R.id.showTaskButton).setOnClickListener { showTasks() }
        view.findViewById<Button>(R.id.modifyTaskButton).setOnClickListener { modifyTask() }

        return view
    }

    private fun addTask() {
        val task = taskTextInput.text.toString()
        val date = dateTextInput.text.toString()
        val time = timeTextInput.text.toString()
        if (task.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty()) {
            taskList.add(task)
            taskDetails.add("Task: $task\nDue Date: $date\nDue Time: $time")
            saveTasks()
            clearInputs()
        }
    }

    private fun showTasks() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Tasks")
        builder.setItems(taskDetails.toTypedArray(), null)
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun modifyTask() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Task to Modify")

        builder.setItems(taskList.toTypedArray()) { dialog, which ->
            val selectedTask = taskList[which]
            taskTextInput.setText(selectedTask)
            val details = taskDetails[which].split("\n")
            dateTextInput.setText(details[1].removePrefix("Due Date: "))
            timeTextInput.setText(details[2].removePrefix("Due Time: "))
            dialog.dismiss()
        }
        builder.show()
    }

    private fun clearInputs() {
        taskTextInput.text.clear()
        dateTextInput.text.clear()
        timeTextInput.text.clear()
    }

    private fun saveTasks() {
        val editor = sharedPreferences.edit()
        editor.putStringSet("tasks", taskList.toSet())
        editor.apply()
    }

    private fun loadTasks() {
        val tasks = sharedPreferences.getStringSet("tasks", setOf())
        tasks?.let {
            taskList.addAll(it)
            taskDetails.addAll(it.map { task -> "Task: $task" })
        }
    }
}
