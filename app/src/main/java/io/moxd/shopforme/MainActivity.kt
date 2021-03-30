package io.moxd.shopforme

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.github.kittinunf.fuel.core.FuelManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import io.moxd.shopforme.data.AuthManager
import io.moxd.shopforme.data.UserManager
import io.moxd.shopforme.ui.login.LoginFragment
import io.moxd.shopforme.ui.splashscreen.SplashScreenDirections
import io.moxd.shopforme.utils.requireAuthManager
import kotlinx.coroutines.flow.collect
import java.util.*


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

        val context = this

        // Eine AuthManager Instanz erzeugen (benötigt context ggf. für DataStore)
        authManager = AuthManager(applicationContext)
        userManager = UserManager(applicationContext)

        // ActionBar mit Auth Navigation Graph einstellen
        setupActionBarWithGraph(R.navigation.nav_graph_auth)

        lifecycleScope.launchWhenCreated {
            authManager?.events?.collect { result ->
                when (result) {
                    is AuthManager.Result.NoConnection -> {
                        MaterialAlertDialogBuilder(context)
                                .setTitle("Keine Internetverbindung")
                                .setMessage("Zurzeit kann diese App nur mit einer aktiven Internetverbindung genutzt werden.")
                                .setNeutralButton("Ich habe wieder eine Verbindung!") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .show()
                    }

                    is AuthManager.Result.SessionInvalid -> {
                        // Versuche die Session zu erneuern
                        requireAuthManager().auth()
                    }

                    is AuthManager.Result.AuthSucess -> {
                        Firebase.messaging.isAutoInitEnabled = true
                        setupActionBarWithGraph(R.navigation.nav_graph_main)

                    }

                    is AuthManager.Result.AuthError -> {
                        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.let { navHost ->
                            navHost.getChildFragmentManager().getFragments().get(0)?.let { currentFragment ->
                                if(currentFragment is LoginFragment) {
                                    Snackbar.make(currentFragment.requireView(), "Login fehlgeschlagen", Snackbar.LENGTH_LONG).show()
                                    return@collect
                                }
                            }
                        }

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