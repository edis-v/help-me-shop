package io.moxd.shopforme.ui.splashscreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.result.Result
import io.moxd.shopforme.MainActivity

import io.moxd.shopforme.R
import io.moxd.shopforme.data.AuthManager
import io.moxd.shopforme.data.UserManager
import io.moxd.shopforme.data.model.UserME
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Dispatcher
import java.lang.Exception

class SplashScreen : AppCompatActivity() {


    companion object {
        // Nur eine (von außen unveränderliche) Instanz der Manager erzeugen
        var authManager: AuthManager? = null
        var userManager: UserManager? = null

    }

    init {
        FuelManager.instance.basePath = "https://moco.fluffistar.com/"
    }
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.splashscreen)
        authManager = AuthManager(this)
        userManager = UserManager(this)
        findViewById<TextView>(R.id.appVersion).text = "Version ${ packageManager.getPackageInfo(this.packageName, 0).versionName}";

        Thread(Runnable {

            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                // Login here
            loginSplash()


            }, 100) //it will wait 100 millisec before login
        }).start()


    }
    fun loginSplash() {
        GlobalScope.launch(Dispatchers.IO) {
            //get Session or Login if no Session Avaible
            authManager?.auth2()
            val intent = Intent(this@SplashScreen, MainActivity::class.java)
            startActivity(intent)
            finish()



    }

    }


}