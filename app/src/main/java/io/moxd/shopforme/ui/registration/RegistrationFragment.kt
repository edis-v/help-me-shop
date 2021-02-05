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
import io.moxd.shopforme.exhaustive
import kotlinx.coroutines.flow.collect

private const val TAG = "RegistrationFragment"

class RegistrationFragment: Fragment(R.layout.auth_registration_fragment) {
    private val viewModel: RegistrationViewModel by viewModels {
        RegistrationViewModelFactory(this, arguments)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = AuthRegistrationFragmentBinding.bind(view)

        binding.apply {
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

        handleEvents()
    }

    private fun handleEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.registrationEvent.collect { event ->
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
                }.exhaustive
            }
        }
    }
}