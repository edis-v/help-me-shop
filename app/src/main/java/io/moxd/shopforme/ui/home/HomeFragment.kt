package io.moxd.shopforme.ui.home

import android.os.Bundle
import android.transition.Fade
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.transition.MaterialElevationScale
import com.squareup.picasso.Picasso
import io.moxd.shopforme.*

import io.moxd.shopforme.data.AuthManager
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.UserME
import io.moxd.shopforme.databinding.MainHomeFragmentBinding
import io.moxd.shopforme.ui.angebot.AngebotFragment
import io.moxd.shopforme.ui.map.MapFragment
import io.moxd.shopforme.ui.profile.ProfileFragment
import io.moxd.shopforme.ui.profile_list.ProfileListFragment
import io.moxd.shopforme.ui.shopangebot.ShopAngebotFragment
import io.moxd.shopforme.ui.shopbuylist.BuylistAdd
import io.moxd.shopforme.ui.shopbuylist.Shopcart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString


class HomeFragment: Fragment(R.layout.main_home_fragment) {


    lateinit var binding: MainHomeFragmentBinding
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var mainframe: FrameLayout


    var last = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return super.onCreateView(inflater, container, savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MainHomeFragmentBinding.bind(view)
       // ft = (requireActivity() as MainActivity).getSupportFragmentManager().beginTransaction()

        mainframe = view.findViewById(R.id.mainframe)
        bottomNavigationView = view.findViewById(R.id.bottom_navigation)

        //get user
        val job: Job = GlobalScope.launch(context = Dispatchers.IO) {
            requireAuthManager().SessionID().take(1).collect {
                //do actions

                Fuel.get(
                        RestPath.user(it)
                ).responseString { _, _, result ->

                    when (result) {


                        is Result.Failure -> {
                            this@HomeFragment.activity?.runOnUiThread() {
                                Log.d("Error", result.getException().message.toString())
                                Toast.makeText(context, "Login Failed", Toast.LENGTH_LONG).show()
                            }
                        }
                        is Result.Success -> {
                            val data = result.get()

                            Log.d("USerProfile", data)

                            this@HomeFragment.activity?.runOnUiThread() {

                                AuthManager.User = JsonDeserializer.decodeFromString<UserME>(data);

                                if(AuthManager.User!!.usertype_txt == "Helfer") {
                                    val ft = (requireActivity() as MainActivity).supportFragmentManager.beginTransaction()
                                    ft.replace(R.id.mainframe, ShopAngebotFragment())
                                    ft.commit()
                                }else
                                {
                                    val ft = (requireActivity() as MainActivity).supportFragmentManager.beginTransaction()
                                    ft.replace(R.id.mainframe, Shopcart())
                                    ft.commit()
                                }


                                updateNavbar()

                            }
                        }
                    }
                }.join()


            }

        }
        job.start()




        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            if(last != item.itemId){
                last = item.itemId
            when (item.itemId) {
                R.id.item1 -> {
                    // Respond to navigation item 1 click
                    if (AuthManager.User!!.usertype_txt == "Helfer") {
                        val ft = (requireActivity() as MainActivity).supportFragmentManager.beginTransaction()
                        ft.replace(R.id.mainframe,ShopAngebotFragment() )
                        ft.commit()
                    }
                    else{
                        val ft = (requireActivity() as MainActivity).supportFragmentManager.beginTransaction()
                        ft.replace(R.id.mainframe,Shopcart()  )
                        ft.commit()
                    }
                    true
                }
                R.id.item2 -> {
                    // Respond to navigation item 2 click
                    if (AuthManager.User!!.usertype_txt == "Helfer") {
                        val ft = (requireActivity() as MainActivity).supportFragmentManager.beginTransaction()
                        ft.replace(R.id.mainframe, MapFragment() )
                        ft.commit()
                    }
                    else
                    {
                        val ft = (requireActivity() as MainActivity).supportFragmentManager.beginTransaction()
                        ft.replace(R.id.mainframe, AngebotFragment() )
                        ft.commit()
                    }
                    true
                }
                R.id.item3 -> {
                    // Respond to navigation item 3 click
                    val ft = (requireActivity() as MainActivity).supportFragmentManager.beginTransaction()
                    ft.replace(R.id.mainframe, ProfileListFragment() )
                    ft.commit()
                    true
                }
                else -> false
            }}else
                true

    }
    }

   fun updateNavbar(){

       bottomNavigationView.menu.clear()
       if(AuthManager.User!!.usertype_txt == "Helfer")
        bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu_helfer)
       else
           bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu)
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
                 //   requireUserManager().sessionRevoked()
                }
                (requireActivity() as MainActivity).setupActionBarWithGraph(R.navigation.nav_graph_auth)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}