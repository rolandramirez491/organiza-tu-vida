package com.example.organizatuvida.ui.dashboard

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.organizatuvida.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val TASKS_PREFS_KEY = "tasks_prefs"
    private val TASKS_LIST_KEY = "tasks_list"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Referencia al botón "Agregar Tarea"
        val buttonAddTask: Button = binding.buttonAddTask
        buttonAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        // Referencia al botón "Mostrar Tareas"
        val buttonShowTasks: Button = binding.buttonShowTasks
        buttonShowTasks.setOnClickListener {
            showTaskList()
        }

        // Referencia al botón "Modificar Tarea"
        val buttonModifyTask: Button = binding.buttonModifyTask
        buttonModifyTask.setOnClickListener {
            showModifyTaskDialog()
        }

        return root
    }

    // Función para mostrar el cuadro de diálogo de agregar tarea
    private fun showAddTaskDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Agregar Tarea")

        // Layout para los inputs
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL

        // Campo para el nombre de la tarea
        val inputTaskName = EditText(requireContext())
        inputTaskName.hint = "Nombre de la tarea"
        layout.addView(inputTaskName)

        // Campo para la prioridad de la tarea
        val inputTaskPriority = EditText(requireContext())
        inputTaskPriority.hint = "Prioridad (1-5)"
        layout.addView(inputTaskPriority)

        // Campo para la fecha de entrega de la tarea
        val inputTaskDueDate = EditText(requireContext())
        inputTaskDueDate.hint = "Fecha de entrega"
        layout.addView(inputTaskDueDate)

        builder.setView(layout)

        // Botón "Agregar"
        builder.setPositiveButton("Agregar") { dialog, _ ->
            val taskName = inputTaskName.text.toString()
            val taskPriority = inputTaskPriority.text.toString()
            val taskDueDate = inputTaskDueDate.text.toString()

            // Validar los datos ingresados
            if (taskName.isNotEmpty() && taskPriority.isNotEmpty() && taskDueDate.isNotEmpty()) {
                val task = "Tarea: $taskName, Prioridad: $taskPriority, Fecha: $taskDueDate"
                saveTask(task)
            }
            dialog.dismiss()
        }

        // Botón "Cancelar"
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    // Función para guardar la tarea en SharedPreferences
    private fun saveTask(task: String) {
        val sharedPreferences = requireActivity().getSharedPreferences(TASKS_PREFS_KEY, Context.MODE_PRIVATE)
        val tasks = sharedPreferences.getStringSet(TASKS_LIST_KEY, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        tasks.add(task)

        sharedPreferences.edit().putStringSet(TASKS_LIST_KEY, tasks).apply()
    }

    // Función para mostrar la lista de tareas guardadas
    private fun showTaskList() {
        val sharedPreferences = requireActivity().getSharedPreferences(TASKS_PREFS_KEY, Context.MODE_PRIVATE)
        val tasks = sharedPreferences.getStringSet(TASKS_LIST_KEY, mutableSetOf())

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Lista de Tareas")

        // Si hay tareas guardadas, las mostramos
        if (tasks != null && tasks.isNotEmpty()) {
            val taskList = tasks.joinToString(separator = "\n")
            builder.setMessage(taskList)
        } else {
            builder.setMessage("No hay tareas guardadas.")
        }

        builder.setPositiveButton("Cerrar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    // Función para modificar una tarea existente
    private fun showModifyTaskDialog() {
        val sharedPreferences = requireActivity().getSharedPreferences(TASKS_PREFS_KEY, Context.MODE_PRIVATE)
        val tasks = sharedPreferences.getStringSet(TASKS_LIST_KEY, mutableSetOf())?.toMutableSet()

        // Si no hay tareas, mostramos un mensaje
        if (tasks.isNullOrEmpty()) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Modificar Tarea")
            builder.setMessage("No hay tareas para modificar.")
            builder.setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
            builder.show()
            return
        }

        // Crear un array de las tareas para seleccionar cuál modificar
        val tasksArray = tasks.toTypedArray()
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Selecciona una tarea para modificar")

        // Mostrar las tareas en un cuadro de diálogo
        builder.setItems(tasksArray) { _, which ->
            val selectedTask = tasksArray[which]

            // Mostrar un cuadro de diálogo con los detalles de la tarea seleccionada para modificarla
            val modifyBuilder = AlertDialog.Builder(requireContext())
            modifyBuilder.setTitle("Modificar Tarea")

            val layout = LinearLayout(requireContext())
            layout.orientation = LinearLayout.VERTICAL

            val inputTaskName = EditText(requireContext())
            inputTaskName.setText(selectedTask.substringAfter("Tarea: ").substringBefore(", Prioridad: "))
            layout.addView(inputTaskName)

            val inputTaskPriority = EditText(requireContext())
            inputTaskPriority.setText(selectedTask.substringAfter("Prioridad: ").substringBefore(", Fecha: "))
            layout.addView(inputTaskPriority)

            val inputTaskDueDate = EditText(requireContext())
            inputTaskDueDate.setText(selectedTask.substringAfter("Fecha: "))
            layout.addView(inputTaskDueDate)

            modifyBuilder.setView(layout)

            modifyBuilder.setPositiveButton("Modificar") { dialog, _ ->
                // Eliminar la tarea anterior
                tasks.remove(selectedTask)

                // Crear la tarea modificada
                val modifiedTask = "Tarea: ${inputTaskName.text}, Prioridad: ${inputTaskPriority.text}, Fecha: ${inputTaskDueDate.text}"
                tasks.add(modifiedTask)

                // Guardar los cambios en SharedPreferences
                sharedPreferences.edit().putStringSet(TASKS_LIST_KEY, tasks).apply()

                dialog.dismiss()
            }

            modifyBuilder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

            modifyBuilder.show()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
