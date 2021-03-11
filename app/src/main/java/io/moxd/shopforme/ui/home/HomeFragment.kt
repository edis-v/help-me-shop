package io.moxd.shopforme.ui.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView

import io.moxd.shopforme.MainActivity
import io.moxd.shopforme.R
import io.moxd.shopforme.databinding.MainHomeFragmentBinding
import io.moxd.shopforme.requireAuthManager
import io.moxd.shopforme.requireUserManager
import io.moxd.shopforme.ui.map.MapFragment
import io.moxd.shopforme.ui.profile.ProfileFragment
import io.moxd.shopforme.ui.profile_list.ProfileListFragment
import kotlinx.coroutines.launch


class HomeFragment: Fragment(R.layout.main_home_fragment) {


    lateinit var binding: MainHomeFragmentBinding
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var mainframe: FrameLayout
   // lateinit var ft: FragmentTransaction
    init {

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MainHomeFragmentBinding.bind(view)
       // ft = (requireActivity() as MainActivity).getSupportFragmentManager().beginTransaction()

        mainframe = view.findViewById(R.id.mainframe)
        bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
        when(item.itemId) {
            R.id.item1 -> {
                // Respond to navigation item 1 click
                true
            }
            R.id.item2 -> {
                // Respond to navigation item 2 click
                val ft = (requireActivity() as MainActivity).getSupportFragmentManager().beginTransaction()
                ft.replace(R.id.mainframe, MapFragment())
                ft.commit()
                true
            }
            R.id.item3 -> {
                // Respond to navigation item 3 click
                val ft = (requireActivity() as MainActivity).getSupportFragmentManager().beginTransaction()
                ft.replace(R.id.mainframe, ProfileListFragment())
                ft.commit()
                true
            }
            else -> false
        }
    }
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