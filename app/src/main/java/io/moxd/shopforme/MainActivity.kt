package io.moxd.shopforme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.github.kittinunf.fuel.core.FuelManager
import io.moxd.shopforme.data.AuthManager
import io.moxd.shopforme.data.UserManager

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    init {
        FuelManager.instance.basePath = "https://moco.fluffistar.com/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)

        // Eine AuthManager Instanz erzeugen (benötigt context ggf. für DataStore)
        authManager = AuthManager(this)
        userManager = UserManager(this)

        // ActionBar mit Auth Navigation Graph einstellen
        setupActionBarWithGraph(R.navigation.nav_graph_auth)
    }

    override fun onNavigateUp() = onSupportNavigateUp()
    override fun onSupportNavigateUp() = navController.navigateUp()

    // NavigationBar Setup für bestimmten NavigationGraph (nutzbar um NavigationGraph zu ersetzen)
    fun setupActionBarWithGraph(id: Int) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        navController = navHostFragment.findNavController()
        val graph = inflater.inflate(id)
        navController.graph = graph
      //  setupActionBarWithNavController(navController)
    }

    companion object {
        // Nur eine (von außen unveränderliche) Instanz der Manager erzeugen
        var authManager: AuthManager? = null
            private set
        var userManager: UserManager? = null
            private set
    }
}