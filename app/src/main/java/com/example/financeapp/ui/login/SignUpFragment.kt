package com.example.financeapp.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.financeapp.FinanceApplication
import com.example.financeapp.R
import com.example.financeapp.databinding.FragmentSignUpBinding
import com.example.financeapp.presentation.SignUpViewModel
import com.google.android.material.snackbar.Snackbar // <-- IMPORTANTE: Para mostrar errores

class SignUpFragment : Fragment() {

    private var _b: FragmentSignUpBinding? = null
    private val b get() = _b!!

    // --- Conecta al ViewModel real ---
    private val vm by viewModels<SignUpViewModel> {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val application = requireActivity().application as FinanceApplication
                val appContainer = application.appContainer
                return SignUpViewModel(appContainer.authService) as T
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _b = FragmentSignUpBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Maneja el botón "atrás" del sistema (esto ya lo tenías)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        // --- LÓGICA DE UI CORREGIDA ---

        // El ID de tu botón es 'btnCreate'
        b.btnCreate.setOnClickListener {
            // 1. Pasa los datos al ViewModel
            vm.email.value = b.emailInput.text?.toString()
            vm.password.value = b.passInput.text?.toString()
            // 'passConfirmInput' eliminado

            // 2. Llama a la función de registro
            vm.onSignUp()
        }

        // Tu XML no tiene un link para "Volver a Login",
        // pero si lo tuviera, iría aquí. El botón "atrás" ya funciona.

        // Observa si el registro fue exitoso
        vm.success.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                // Si es exitoso, navega al Dashboard
                // (Asegúrate de que el ID 'dashboardFragment' sea correcto en tu nav_graph.xml)
                findNavController().navigate(R.id.dashboardFragment)
            }
        }

        // Observa si hay errores
        vm.error.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg != null) {
                // Como no hay un TextView de error, usamos un Snackbar
                Snackbar.make(b.root, errorMsg, Snackbar.LENGTH_SHORT).show()
                vm.error.value = null // Resetea el error para que no vuelva a salir
            }
        }
        // --- FIN DE LÓGICA DE UI ---
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}