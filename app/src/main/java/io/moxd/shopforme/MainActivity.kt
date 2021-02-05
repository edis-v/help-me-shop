package io.moxd.shopforme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.github.kittinunf.fuel.core.FuelManager
import io.moxd.shopforme.data.AuthManager

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    init {
        FuelManager.instance.basePath = "http://pc.fritz.box"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)

        authManager = AuthManager(this)
        setupActionBarWithGraph(R.navigation.nav_graph_auth)
    }

    override fun onNavigateUp() = onSupportNavigateUp()
    override fun onSupportNavigateUp() = navController.navigateUp()

    fun setupActionBarWithGraph(id: Int) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        navController = navHostFragment.findNavController()
        val graph = inflater.inflate(id)
        navController.graph = graph
        setupActionBarWithNavController(navController)
    }

    companion object {
        var authManager: AuthManager? = null
            private set
    }
}