package com.example.financeapp.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible // <-- 1. IMPORTANTE AÑADIR ESTE
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.financeapp.FinanceApplication
import com.example.financeapp.R
import com.example.financeapp.databinding.FragmentLoginBinding
import com.example.financeapp.presentation.LoginViewModel

class LoginFragment : Fragment() {

    private var _b: FragmentLoginBinding? = null
    private val b get() = _b!!

    // Conecta al AppContainer para obtener el ViewModel real
    private val vm by viewModels<LoginViewModel> {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val application = requireActivity().application as FinanceApplication
                val appContainer = application.appContainer
                return LoginViewModel(
                    appContainer.authService,
                    appContainer.sessionStore
                ) as T
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _b = FragmentLoginBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        // Asigna los valores iniciales (si el ViewModel los tiene)
        b.emailInput.setText(vm.email.value)
        b.passInput.setText(vm.password.value)

        b.btnLogin.setOnClickListener {
            // Pasa los datos de la UI al ViewModel
            vm.email.value = b.emailInput.text?.toString()
            vm.password.value = b.passInput.text?.toString()
            // Llama al cerebro
            vm.onLogin()
        }

        // Navegación a otras pantallas
        b.linkSignup.setOnClickListener { findNavController().navigate(R.id.signUpFragment) }
        b.linkForgot.setOnClickListener { findNavController().navigate(R.id.forgotFragment) }

        // Observa el éxito
        vm.success.observe(viewLifecycleOwner) { ok ->
            if (ok == true && findNavController().currentDestination?.id == R.id.loginFragment) {
                findNavController().navigate(R.id.dashboardFragment)
            }
        }

        // --- BLOQUE CORREGIDO ---
        // Observa los errores
        vm.error.observe(viewLifecycleOwner) { msg ->
            // 2. Hazlo visible si 'msg' no es nulo
            b.errorText.isVisible = (msg != null)
            // 3. Asigna el texto
            b.errorText.text = msg ?: ""
        }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}