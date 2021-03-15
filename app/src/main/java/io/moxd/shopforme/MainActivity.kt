package io.moxd.shopforme

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController

import io.moxd.shopforme.data.AuthManager
import io.moxd.shopforme.data.UserManager
import io.moxd.shopforme.ui.login.LoginViewModel
import io.moxd.shopforme.ui.splashscreen.SplashScreen.Companion.authManager
import io.moxd.shopforme.ui.splashscreen.SplashScreen.Companion.userManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)

        // Eine AuthManager Instanz erzeugen (benötigt context ggf. für DataStore)


        // ActionBar mit Auth Navigation Graph einstellen
        //

        GlobalScope.launch(Dispatchers.Main) {

                // Authentifizierungsversuch (gelingt wenn eine Session gespeichert ist)

                    authManager?.auth2() // sessionId in dataStore?


                // Auf Events des AuthManagers in Coroutine reagieren

                    authManager?.events?.collect { result ->
                        when(result) {
                            is AuthManager.Result.AuthSucess -> { Log.d("NAV","MAIN")
                                setupActionBarWithGraph(R.navigation.nav_graph_main)}

                            is  AuthManager.Result.AuthError ->  {
                                Log.d("NAV","AUTH")
                                setupActionBarWithGraph(R.navigation.nav_graph_auth)}

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


}