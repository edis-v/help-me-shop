package io.moxd.shopforme

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.result.Result
import io.moxd.shopforme.UI.Login.LoginFragment
import io.moxd.shopforme.UI.Profile.ProfileFragment
import io.moxd.shopforme.UI.Registration.RegistrationFragment
import org.json.JSONObject

private const val NUM_PAGES = 3

class MainActivity : FragmentActivity() {
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FuelManager.instance.basePath = "http://192.168.178.58";
        viewPager = findViewById(R.id.pager)
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val email_txt = sharedPref!!.getString( "email", "")
        val password_txt = sharedPref!!.getString( "password", "")
        val firstime = sharedPref!!.getBoolean( "Firstrun", true)
        val asynclogin = Fuel.post(
            "api/user/login",
            listOf("email" to "$email_txt", "password" to "$password_txt")
        ).responseString {  request, response, result ->
            when(result){
                is Result.Failure -> {

                    Toast.makeText(this,"Login Failed", Toast.LENGTH_LONG).show()
                }
                is Result.Success -> {
                    val session= JSONObject(result.get()).getString("session_id")
                    Log.d("Session_id",session)


                    with (sharedPref!!.edit()) {
                        putString("SessionID", session)

                        apply()
                    }
                }
            }
        }

        if(email_txt != "" && password_txt != "" )
            asynclogin.join()
    }
    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment = when ( position){
            0 -> RegistrationFragment()
            1 -> LoginFragment()
            2 -> ProfileFragment()
            else -> RegistrationFragment()

        }
    }
}