package com.example.financeapp.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.financeapp.R
import com.example.financeapp.databinding.FragmentLoginBinding
import com.example.financeapp.fake.FakeAuthService
import com.example.financeapp.presentation.LoginViewModel
// CORRECCIÓN: Importa tu SessionStore
import com.example.financeapp.security.SessionStore

class LoginFragment : Fragment() {

    private var _b: FragmentLoginBinding? = null
    private val b get() = _b!!

    private val vm by viewModels<LoginViewModel> {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // CORRECCIÓN: Pasa ambos argumentos al ViewModel
                val store = SessionStore(requireContext().applicationContext)
                return LoginViewModel(FakeAuthService(), store) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _b = FragmentLoginBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.emailInput.setText(vm.email.value)
        b.passInput.setText(vm.password.value)

        b.btnLogin.setOnClickListener {
            vm.email.value = b.emailInput.text?.toString()
            vm.password.value = b.passInput.text?.toString()
            vm.onLogin()
        }

        b.linkSignup.setOnClickListener { findNavController().navigate(R.id.signUpFragment) }
        b.linkForgot.setOnClickListener { findNavController().navigate(R.id.forgotFragment) }

        vm.success.observe(viewLifecycleOwner) { ok ->
            if (ok == true && findNavController().currentDestination?.id == R.id.loginFragment) {
                findNavController().navigate(R.id.dashboardFragment)
            }
        }

        vm.error.observe(viewLifecycleOwner) { msg ->
            b.errorText.text = msg ?: ""
        }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}