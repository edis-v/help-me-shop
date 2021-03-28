package io.moxd.shopforme.ui.profile

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import io.moxd.shopforme.api.ApiProfile

import io.moxd.shopforme.ui.registration.RegistrationViewModel

class ProfileViewModelFactory(owner: SavedStateRegistryOwner, defaultArgs: Bundle?
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
    ): T {
        return ProfileViewModel(handle, ApiProfile()) as T
    }
}