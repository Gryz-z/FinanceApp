package com.example.financeapp.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.domain.model.ports.AuthService
import com.example.financeapp.security.SessionStore
import kotlinx.coroutines.launch

class LoginViewModel(
    private val auth: AuthService,
    private val store: SessionStore
) : ViewModel() {

    val email = MutableLiveData("")
    val password = MutableLiveData("")
    val loading = MutableLiveData(false)
    val error = MutableLiveData<String?>(null)
    val success = MutableLiveData(false)

    fun onLogin() {
        val e = email.value?.trim().orEmpty()
        val p = password.value.orEmpty()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches() || p.length < 6) {
            error.value = "Correo o contraseña inválidos"
            return
        }
        viewModelScope.launch {
            loading.value = true
            auth.login(e, p)
                .onSuccess { token -> store.saveToken(token); success.value = true }
                .onFailure { th -> error.value = th.message }
            loading.value = false
        }
    }
}
