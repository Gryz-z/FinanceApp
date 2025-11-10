package com.example.financeapp

import android.app.Application
import com.example.financeapp.data.AppContainer

/**
 * Clase de Aplicación personalizada.
 * Se inicia antes que cualquier Activity/Fragment.
 */
class FinanceApplication : Application() {

    // Creamos el contenedor. 'this' es el Contexto de la Aplicación.
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // El contenedor se crea una sola vez cuando la app se inicia
        appContainer = AppContainer(this)
    }
}