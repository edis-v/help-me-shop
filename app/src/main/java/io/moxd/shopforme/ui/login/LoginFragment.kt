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

    lateinit var binding: AuthLoginFragmentBinding

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(this, arguments)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = AuthLoginFragmentBinding.bind(view)

        binding.apply {
            loginTxtInputEmail.editText?.setText(viewModel.loginEmail)
            loginTxtInputPw.editText?.setText(viewModel.loginPassword)

            loginTxtInputEmail.editText?.addTextChangedListener {
                viewModel.loginEmail = it.toString()
                loginTxtInputEmail.error = null
            }

            loginTxtInputPw.editText?.addTextChangedListener {
                viewModel.loginPassword = it.toString()
                loginTxtInputPw.error = null
            }

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

        handleEvents()
    }

    private fun handleEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.loginEvent.collect { event ->
                when(event) {
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
                    is LoginViewModel.LoginEvent.LoginSuccess -> {
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
                }.exhaustive
            }
        }
    }
}