package io.moxd.shopforme.ui.registration

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import io.moxd.shopforme.R
import io.moxd.shopforme.data.model.Registration
import io.moxd.shopforme.databinding.AuthRegistrationFragmentBinding
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.flow.collect

private const val TAG = "RegistrationFragment"

class RegistrationFragment: Fragment(R.layout.auth_registration_fragment) {

    // Für Fragment Layout automatisch erzeugte View Bindings (Ggf.: Build -> Rebuild Project)
    lateinit var binding: AuthRegistrationFragmentBinding

    // ViewModel per Delegation erzeugen + Factory um Konstruktor Parameter zu geben
    private val viewModel: RegistrationViewModel by viewModels {
        RegistrationViewModelFactory(this, arguments)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = AuthRegistrationFragmentBinding.bind(view)

        // Mit apply direkt auf die Elemente des Layouts mit Id zugreifen
        binding.apply {
            // ViewModel benachrichtigen wenn FAB geklickt wird
            registerFabConfirm.setOnClickListener {
                viewModel.onRegistrationConfirmClick()
            }

            registerTxtInputEmail.editText?.addTextChangedListener { viewModel.email = it.toString() }
            registerTxtInputPw.editText?.addTextChangedListener { viewModel.pw = it.toString() }
            registerTxtInputPwConfirm.editText?.addTextChangedListener { viewModel.pwConfirmed = it.toString() }
            registerTxtInputLastname.editText?.addTextChangedListener { viewModel.lastName = it.toString() }
            registerTxtInputFirstname.editText?.addTextChangedListener { viewModel.firstName = it.toString() }
            registerTxtInputAddress.editText?.addTextChangedListener { viewModel.address = it.toString() }
            registerTxtInputPhoneNum.editText?.addTextChangedListener { viewModel.phoneNum = it.toString() }

            registerTxtInputFirstname.editText?.setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus) { registerTxtInputFirstname.error = null }
            }
            registerTxtInputLastname.editText?.setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus) { registerTxtInputLastname.error = null }
            }
            registerTxtInputAddress.editText?.setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus) {
                    registerTxtInputAddress.error = null
                }
            }

            registerTxtInputEmail.editText?.setOnFocusChangeListener { _, hasFocus ->
                if(!hasFocus)
                    viewModel.checkEmail()
                else
                    registerTxtInputEmail.error = null
            }
            registerTxtInputPw.editText?.setOnFocusChangeListener { _, hasFocus ->
                if(!hasFocus) {
                    viewModel.checkPw()
                } else {
                    registerTxtInputPw.error = null
                    registerTxtInputPwConfirm.error = null
                }
            }
            registerTxtInputPwConfirm.editText?.setOnFocusChangeListener { _, hasFocus ->
                if(!hasFocus) {
                    viewModel.checkPw()
                } else {
                    registerTxtInputPw.error = null
                    registerTxtInputPwConfirm.error = null
                }
            }
            registerTxtInputPhoneNum.editText?.setOnFocusChangeListener { _, hasFocus ->
                if(!hasFocus) {
                    viewModel.checkPhoneNum()
                } else {
                    registerTxtInputPhoneNum.error = null
                }
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

                // Navigation (innerhalb von nav_graph_auth)

                when(event) {
                    is RegistrationViewModel.RegistrationEvent.CheckErrors -> {
                        binding.apply {
                            var noError = true

                            if (registerTxtInputEmail.error != null) noError = false
                            if (registerTxtInputPw.error != null) noError = false
                            if (registerTxtInputPwConfirm.error != null) noError = false
                            if (registerTxtInputLastname.error != null) noError = false
                            if (registerTxtInputFirstname.error != null) noError = false
                            if (registerTxtInputAddress.error != null) noError = false
                            if (registerTxtInputPhoneNum.error != null) noError = false

                            if(noError) {
                                viewModel.performRegistration()
                            } else {
                                Snackbar.make(requireView(), "Überprüfen Sie Ihr Formular", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                    is RegistrationViewModel.RegistrationEvent.FeedbackFieldsRequired -> {
                        binding.apply {
                            event.field.forEach {
                                when(it) {
                                    "email" -> registerTxtInputEmail.error = "Pflichtfeld"
                                    "pw" -> registerTxtInputPw.error = "Pflichtfeld"
                                    "pwConfirm" -> registerTxtInputPwConfirm.error = "Pflichtfeld"
                                    "lastName" -> registerTxtInputLastname.error = "Pflichtfeld"
                                    "firstName" -> registerTxtInputFirstname.error = "Pflichtfeld"
                                    "address" -> registerTxtInputAddress.error = "Pflichtfeld"
                                    "phoneNum" -> registerTxtInputPhoneNum.error = "Pflichtfeld"
                                }
                            }
                        }
                    }
                    RegistrationViewModel.RegistrationEvent.FeedbackMalformedEmail -> {
                        binding.apply {
                            registerTxtInputEmail.error = "Ungültige Email-Adresse"
                        }
                    }
                    RegistrationViewModel.RegistrationEvent.FeedbackMalformedPhoneNumber -> {
                        binding.apply {
                            registerTxtInputPhoneNum.error = "Ungültige Telefonnummer"
                        }
                    }
                    RegistrationViewModel.RegistrationEvent.FeedbackPasswordTooWeak -> {
                        binding.apply {
                            registerTxtInputPw.error = "Passwort zu schwach"
                            registerTxtInputPwConfirm.error = "Passwort zu schwach"
                        }
                    }
                    RegistrationViewModel.RegistrationEvent.FeedbackPasswordNotIdentical -> {
                        binding.apply {
                            registerTxtInputPw.error = "Passwörter nicht identisch"
                            registerTxtInputPwConfirm.error = "Passwörter nicht identisch"
                        }
                    }
                    is RegistrationViewModel.RegistrationEvent.Success -> {
                        Snackbar.make(requireView(), "Erfolgreich registriert", Snackbar.LENGTH_LONG).show()
                        requireAuthManager().login2(event.email, event.password)
                    }
                    is RegistrationViewModel.RegistrationEvent.Error -> {
                        Snackbar.make(requireView(), event.lastError, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}