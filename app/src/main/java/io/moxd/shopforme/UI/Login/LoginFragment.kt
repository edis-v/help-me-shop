package io.moxd.shopforme.UI.Login

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import io.moxd.shopforme.R

import com.github.kittinunf.result.Result
import org.json.JSONObject
import kotlin.concurrent.thread

class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.login_fragment, container, false)

        val email = root.findViewById<EditText>(R.id.email)
        val password = root.findViewById<EditText>(R.id.password)
        val login_btn = root.findViewById<Button>(R.id.login_btn)

        login_btn.setOnClickListener{

            val email_txt  = email.text
            val password_txt = password.text


                val asynclogin = Fuel.post(
                    "api/user/login",
                    listOf("email" to "$email_txt", "password" to "$password_txt")
                ).responseString {  request, response, result ->
                    when(result){
                        is Result.Failure -> {

                            Toast.makeText(root.context,"Login Failed",Toast.LENGTH_LONG).show()
                        }
                        is Result.Success -> {
                            val session= JSONObject(result.get()).getString("session_id")
                            Log.d("Session_id",session)

                            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
                            with (sharedPref!!.edit()) {
                                putString("SessionID", session)
                                putString("email",email_txt.toString())
                                putString("password",password_txt.toString())
                                apply()
                            }
                        }
                    }
                }

                asynclogin.join()




        }


        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        // TODO: Use the ViewModel
    }

}