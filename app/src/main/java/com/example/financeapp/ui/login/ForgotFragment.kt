package com.example.financeapp.ui.login // O el paquete donde esté

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback // <-- 1. AÑADE ESTA IMPORTACIÓN
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.financeapp.databinding.FragmentForgotBinding // Asumiendo que tu binding se llama así

// Asumiendo que tu clase se llama ForgotFragment
class ForgotFragment : Fragment() {

    private var _b: FragmentForgotBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _b = FragmentForgotBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) // Es bueno llamar a super

        // --- 2. AÑADE ESTE BLOQUE ENTERO ---
        // Maneja el botón "atrás" del sistema (del celular)
        val callback = object : OnBackPressedCallback(true /* enabled */) {
            override fun handleOnBackPressed() {
                // Esta acción le dice al navegador que "vuelva"
                // (probablemente al LoginFragment)
                findNavController().popBackStack()
            }
        }
        // Registra el callback
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        // --- FIN DEL BLOQUE ---

        //
        // AQUÍ VA TU CÓDIGO EXISTENTE
        //
        // b.btnRecover.setOnClickListener { ... }
        // b.linkLogin.setOnClickListener { findNavController().popBackStack() }
        //

    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}