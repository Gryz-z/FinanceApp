package com.example.financeapp.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.domain.model.ports.AuthService
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Registro (Sign Up).
 * --- CORREGIDO para tu XML ---
 */
class SignUpViewModel(
    private val authService: AuthService
) : ViewModel() {

    // Guarda el estado de los campos de texto
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    // 'passwordConfirm' ELIMINADO

    // Para comunicar al Fragment si el registro fue exitoso
    val success = MutableLiveData<Boolean>(false)
    // Para comunicar mensajes de error
    val error = MutableLiveData<String?>(null)

    fun onSignUp() {
        val em = email.value
        val pw = password.value

        // --- Validaciones CORREGIDAS ---
        if (em.isNullOrBlank() || pw.isNullOrBlank()) {
            error.value = "Por favor, completa email y contraseña"
            return
        }
        if (pw.length < 6) {
            error.value = "La contraseña debe tener al menos 6 caracteres"
            return
        }
        // --- Fin Validaciones ---

        // --- Ejecución ---
        viewModelScope.launch {
            try {
                // Llama al servicio de autenticación REAL
                val result = authService.signUp(em, pw)

                if (result.isSuccess) {
                    // ¡Éxito!
                    success.value = true
                } else {
                    // Muestra el error (ej. "El email ya está registrado")
                    error.value = result.exceptionOrNull()?.message ?: "Error desconocido"
                }
            } catch (e: Exception) {
                error.value = e.message ?: "Error al contactar el servicio"
            }
        }
    }
}