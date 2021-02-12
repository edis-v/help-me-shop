package io.moxd.shopforme.ui.login

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import io.moxd.shopforme.MainActivity
import io.moxd.shopforme.R
import io.moxd.shopforme.databinding.AuthLoginFragmentBinding
import io.moxd.shopforme.exhaustive
import io.moxd.shopforme.toClickable
import kotlinx.coroutines.flow.collect

private const val TAG = "LoginFragment"

class LoginFragment: Fragment(R.layout.auth_login_fragment) {

    // Für Fragment Layout automatisch erzeugte View Bindings (Ggf.: Build -> Rebuild Project)
    lateinit var binding: AuthLoginFragmentBinding

    // ViewModel per Delegation erzeugen + Factory um dem Konstruktor Parameter zu geben
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(this, arguments)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = AuthLoginFragmentBinding.bind(view)

        // Mit apply direkt auf die Elemente des Layouts mit Id zugreifen
        // Und Veränderungen/Events dem ViewModel kommunizieren
        binding.apply {
            // Im ViewModel ggf. vorhandene Werte einsetzen
            loginTxtInputEmail.editText?.setText(viewModel.loginEmail)
            loginTxtInputPw.editText?.setText(viewModel.loginPassword)

            // Bei Änderungen ViewModel synchronisieren + Fehlermeldung zurücksetzen
            loginTxtInputEmail.editText?.addTextChangedListener {
                viewModel.loginEmail = it.toString()
                loginTxtInputEmail.error = null
            }

            // Bei Änderungen ViewModel synchronisieren + Fehlermeldung zurücksetzen
            loginTxtInputPw.editText?.addTextChangedListener {
                viewModel.loginPassword = it.toString()
                loginTxtInputPw.error = null
            }

            // Listener für alle Clickables erstellen und alle Aktionen dem ViewModel melden
            loginTxtForgotPw.toClickable {
                viewModel.onForgotPasswordClick()
            }

            loginTxtRegister.toClickable {
                viewModel.onRegisterClick()
            }

            loginTxtShowGuide.toClickable {
                viewModel.onShowGuideClick()
            }

            loginBtn.setOnClickListener {
                viewModel.onLoginClick()
            }
        }

        // Alle vom ViewModel kommenden (verarbeiteten) Events handeln
        handleEvents() // Erfordern Aktion im Fragment selbst (z.B. UI)
    }

    private fun handleEvents() {
        // Couroutine starten sobald Fragment STARTED ist
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {

            // Events des ViewModels mithilfe der Flow API asynchron abarbeiten
            viewModel.events.collect { event ->
                when(event) {

                    // Navigation (innerhalb von nav_graph_auth)

                    is LoginViewModel.LoginEvent.NavigateToRegistrationScreen -> {
                        val action = LoginFragmentDirections.actionRegister()
                        findNavController().navigate(action)
                    }
                    is LoginViewModel.LoginEvent.NavigateToForgotPasswordScreen -> {
                        Log.i(TAG, "handleEvents: NavigateToForgotPasswordScreen")
                    }
                    is LoginViewModel.LoginEvent.NavigateToGuideScreen -> {
                        Log.i(TAG, "handleEvents: NavigateToGuideScreen")
                    }

                    // InputCheck

                    is LoginViewModel.LoginEvent.NoInput -> {
                        binding.apply {
                            loginTxtInputEmail.error = "Feld darf nicht leer sein"
                            loginTxtInputPw.error = "Feld darf nicht leer sein"
                        }
                    }
                    is LoginViewModel.LoginEvent.EmptyEmail -> {
                        // TODO: Zeige im Email EditText eine Warnung
                    }
                    is LoginViewModel.LoginEvent.EmptyPassword -> {
                        // TODO: Zeige im Passwort EditText eine Warnung
                    }
                    is LoginViewModel.LoginEvent.MalformedEmail -> {
                        // TODO: Zeige im Email EditText eine Warnung
                    }

                    // Login Status

                    is LoginViewModel.LoginEvent.LoginSuccess -> {
                        // NavGraph zu nav_graph_main wechseln anstatt zum HomeFragment zu navigieren
                        // Vorteil: Kein Navigation Stack / Back button
                        (requireActivity() as MainActivity).setupActionBarWithGraph(R.navigation.nav_graph_main)
                    }
                    is LoginViewModel.LoginEvent.LoginFailed -> {
                        Log.i(TAG, "handleEvents: LoginFailed")
                        Log.i(TAG, "handleEvents: ${event.exception.message}")
                        // TODO: Meldung an User
                    }
                    is LoginViewModel.LoginEvent.LoggingIn -> {
                        Log.i(TAG, "handleEvents: LoggingIn")
                        // TODO: Blockiere UI und zeige vllt. einen Ladebalken
                    }
                }
            }
        }
    }
}