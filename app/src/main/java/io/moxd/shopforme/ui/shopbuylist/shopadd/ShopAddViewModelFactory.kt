package io.moxd.shopforme.ui.shopbuylist.shopadd

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import io.moxd.shopforme.api.ApiShopAdd


class ShopAddViewModelFactory(owner: SavedStateRegistryOwner, defaultArgs: Bundle?
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return ShopAddViewModel(handle, ApiShopAdd()) as T
    }
}