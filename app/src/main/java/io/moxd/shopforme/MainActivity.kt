package io.moxd.shopforme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.github.kittinunf.fuel.core.FuelManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import io.moxd.shopforme.data.AuthManager
import io.moxd.shopforme.data.UserManager
import io.moxd.shopforme.ui.splashscreen.SplashScreenDirections
import kotlinx.coroutines.flow.collect
import java.util.*
import kotlin.concurrent.fixedRateTimer

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private val firebaseAnalytics: FirebaseAnalytics

    init {
        FuelManager.instance.basePath = "https://moco.fluffistar.com/"
        firebaseAnalytics = Firebase.analytics
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)

        // Eine AuthManager Instanz erzeugen (benötigt context ggf. für DataStore)
        authManager = AuthManager(this)
        userManager = UserManager(this)

        // ActionBar mit Auth Navigation Graph einstellen
        setupActionBarWithGraph(R.navigation.nav_graph_auth)

        lifecycleScope.launchWhenCreated {
            authManager?.events?.collect { result ->
                when (result) {
                    is AuthManager.Result.AuthSucess -> {
                        setupActionBarWithGraph(R.navigation.nav_graph_main)
                    }

                    is AuthManager.Result.LoggedIn -> {
                        setupActionBarWithGraph(R.navigation.nav_graph_main)
                        // User Manager?
                    }

                    is AuthManager.Result.AuthError -> {
                        setupActionBarWithGraph(R.navigation.nav_graph_auth)
                        val action = SplashScreenDirections.actionSplashScreenToLoginFragment()
                        navController.navigate(action)
                    }

                    is AuthManager.Result.LoginNeeded -> {
                        setupActionBarWithGraph(R.navigation.nav_graph_auth)
                        val action = SplashScreenDirections.actionSplashScreenToLoginFragment()
                        navController.navigate(action)
                    }
                }
            }
        }
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
        var authManager: AuthManager? = null
        var userManager: UserManager? = null
    }
}