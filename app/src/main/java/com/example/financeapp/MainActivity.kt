package com.example.financeapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.financeapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Carga manual del gráfico de navegación
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val controller = navHost.navController
        val graph = controller.navInflater.inflate(R.navigation.nav_graph)
        controller.graph = graph
    }
}
