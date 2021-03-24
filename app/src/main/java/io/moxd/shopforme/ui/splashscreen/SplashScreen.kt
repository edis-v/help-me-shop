package io.moxd.shopforme.ui.splashscreen

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.core.FuelManager
import io.moxd.shopforme.ActitityMain
import io.moxd.shopforme.MainActivity
import io.moxd.shopforme.R
import io.moxd.shopforme.data.AuthManager
import io.moxd.shopforme.data.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SplashScreen : AppCompatActivity() {


    companion object {
        // Nur eine (von außen unveränderliche) Instanz der Manager erzeugen
        var authManager: AuthManager? = null
        var userManager: UserManager? = null

    }

    init {
        FuelManager.instance.basePath = "https://moco.fluffistar.com/"
    }
    val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()
            ) {
                it.forEach{ map ->  Log.d("PErmissions: ${map.key}", map.value.toString())}
                if(it.filter { it.key != "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE" }.all { it.component2() == true }){
                 //   Toast.makeText(this, "All Permission Success", Toast.LENGTH_LONG).show()
                    loginSplash()
                }else{
                  //  Toast.makeText(this, "All Permission not Success", Toast.LENGTH_LONG).show()
                }

            }
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        ActitityMain = this
        setContentView(R.layout.splashscreen)
        authManager = AuthManager(this)
        userManager = UserManager(this)
        findViewById<TextView>(R.id.appVersion).text = "Version ${ packageManager.getPackageInfo(this.packageName, 0).versionName}";

        Thread(Runnable {

            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                // Login here


                this.runOnUiThread {
                    val info: PackageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                    requestPermissionLauncher.launch(info.requestedPermissions)
                }



            },  100)//it will wait 100 millisec before login
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