package io.moxd.shopforme.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import io.moxd.shopforme.*
import io.moxd.shopforme.R
import io.moxd.shopforme.data.AuthManager
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.UserME
import io.moxd.shopforme.databinding.MainHomeFragmentBinding
import io.moxd.shopforme.ui.angebot.AngebotFragment
import io.moxd.shopforme.ui.map.MapFragment
import io.moxd.shopforme.ui.profile_list.ProfileListFragment
import io.moxd.shopforme.ui.shopangebot.ShopAngebotFragment
import io.moxd.shopforme.ui.shopbuylist.shopcart.Shopcart
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString


class HomeFragment : Fragment(R.layout.main_home_fragment) {


    lateinit var binding: MainHomeFragmentBinding
    val viewModel: HomeViewModel by viewModels { HomeViewModelFactory(this, arguments) }


    var last = 0
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        return super.onCreateView(inflater, container, savedInstanceState)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MainHomeFragmentBinding.bind(view)

        binding.apply {


            bottomNavigation.setOnNavigationItemSelectedListener { item ->
                if (last != item.itemId) {
                    last = item.itemId
                    when (item.itemId) {
                        R.id.item1 -> {
                            // Respond to navigation item 1 click
                            Screen1()
                            true
                        }
                        R.id.item2 -> {
                            // Respond to navigation item 2 click
                            Screen2()
                            true
                        }
                        R.id.item3 -> {
                            // Respond to navigation item 3 click
                            Screen3()
                            true
                        }
                        else -> false
                    }
                } else
                    true

            }
        }




        binding.apply {
            viewModel.User.observe(viewLifecycleOwner) {
                if (it.isSuccessful) {
                    val user = it.body()!!
                    bottomNavigation.menu.clear()
                    if (user.usertype_txt == "Helfer")
                        bottomNavigation.inflateMenu(R.menu.bottom_navigation_menu_helfer)
                    else
                        bottomNavigation.inflateMenu(R.menu.bottom_navigation_menu)
                    Screen1()
                } else {
                    //error
                }
            }
        }
    }


    fun Screen1() {
        binding.apply {
            if (viewModel.UserType() == "Helfer") {
                val ft =
                        (requireActivity() as MainActivity).supportFragmentManager.beginTransaction()
                ft.replace(R.id.mainframe, ShopAngebotFragment())
                ft.commit()
            } else {
                val ft =
                        (requireActivity() as MainActivity).supportFragmentManager.beginTransaction()
                ft.replace(R.id.mainframe, Shopcart())
                ft.commit()
            }
        }
    }


    fun Screen2() {
        if (viewModel.UserType() == "Helfer") {
            val ft =
                    (requireActivity() as MainActivity).supportFragmentManager.beginTransaction()
            ft.replace(R.id.mainframe, MapFragment())
            ft.commit()
        } else {
            val ft =
                    (requireActivity() as MainActivity).supportFragmentManager.beginTransaction()
            ft.replace(R.id.mainframe, AngebotFragment())
            ft.commit()
        }
    }

    fun Screen3() {
        val ft =
                (requireActivity() as MainActivity).supportFragmentManager.beginTransaction()
        ft.replace(R.id.mainframe, ProfileListFragment())
        ft.commit()
    }


}