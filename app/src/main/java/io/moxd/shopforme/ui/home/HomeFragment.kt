package io.moxd.shopforme.ui.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import io.moxd.shopforme.MainActivity
import io.moxd.shopforme.R
import io.moxd.shopforme.data.proto_serializer.toModel
import io.moxd.shopforme.databinding.AuthLoginFragmentBinding
import io.moxd.shopforme.databinding.MainHomeFragmentBinding
import io.moxd.shopforme.requireAuthManager
import io.moxd.shopforme.requireUserManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeFragment: Fragment(R.layout.main_home_fragment) {

    lateinit var binding: MainHomeFragmentBinding

    init {
        // Auf Events des UserManagers in Coroutine reagieren (TODO: ins ViewModel packen)
        lifecycleScope.launchWhenStarted {
            requireUserManager().user.collect { protoUser ->
                val user = protoUser.toModel()
                binding.homeTxtHello.text = "Hallo ${user.firstName}!"
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MainHomeFragmentBinding.bind(view)
        //einfach zu map navigieren
        view.findViewById<Button>(R.id.to_map_btn).setOnClickListener {
            (requireActivity() as MainActivity).setupActionBarWithGraph(R.navigation.nav_graph_map)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_logout -> {
                // (Quick 'n Dirty) Ausloggen ohne ViewModel
                viewLifecycleOwner.lifecycleScope.launch {
                    requireAuthManager().unauth()
                    requireUserManager().sessionRevoked()
                }
                (requireActivity() as MainActivity).setupActionBarWithGraph(R.navigation.nav_graph_auth)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}