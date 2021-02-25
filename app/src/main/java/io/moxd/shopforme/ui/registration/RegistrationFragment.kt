package io.moxd.shopforme.ui.registration

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import io.moxd.shopforme.R
import io.moxd.shopforme.data.model.Registration
import io.moxd.shopforme.databinding.AuthRegistrationFragmentBinding
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
                viewModel.onRegistrationConfirmClick(Registration(
                    email = registerTxtInputEmail.editText?.text.toString(),
                    password = registerTxtInputPw.editText?.text.toString(),
                    name = registerTxtInputLastname.editText?.text.toString(),
                    firstName = registerTxtInputFirstname.editText?.text.toString(),
                    address = registerTxtInputAddress.editText?.text.toString(),
                    phoneNumber = registerTxtInputPhoneNum.editText?.text.toString()
                ))
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
                    is RegistrationViewModel.RegistrationEvent.PerformRegistration -> {
                        Log.i(TAG, "handleEvents: PerformRegistration")
                    }
                    RegistrationViewModel.RegistrationEvent.FeedbackMalformedEmail -> TODO()
                    RegistrationViewModel.RegistrationEvent.FeedbackMalformedPhoneNumber -> TODO()
                    RegistrationViewModel.RegistrationEvent.FeedbackPasswordTooWeak -> TODO()
                    RegistrationViewModel.RegistrationEvent.FeedbackPasswordNotIdentical -> TODO()
                    RegistrationViewModel.RegistrationEvent.FeedbackFieldObligatory -> TODO()
                    RegistrationViewModel.RegistrationEvent.FeedbackAddressNotParsable -> TODO()
                }
            }
        }
    }
}