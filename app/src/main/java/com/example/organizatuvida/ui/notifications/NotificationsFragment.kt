package com.example.organizatuvida.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.organizatuvida.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lista de notificaciones de ejemplo
        val notifications = listOf(
            "Tarea 1: Completar en 3 días",
            "Tarea 2: Completar en 1 día",
            "Tarea 3: Completar hoy"
        )

        // Convierte la lista a una cadena de texto para mostrar
        val notificationsText = notifications.joinToString(separator = "\n")

        // Muestra las notificaciones en textNotifications
        binding.textNotifications.text = notificationsText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
