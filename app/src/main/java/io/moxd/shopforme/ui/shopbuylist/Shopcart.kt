package io.moxd.shopforme.ui.shopbuylist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.android.material.tabs.TabLayout
import io.moxd.shopforme.JsonDeserializer
import io.moxd.shopforme.MainActivity
import io.moxd.shopforme.R
import io.moxd.shopforme.adapter.BuyListAdapter
import io.moxd.shopforme.adapter.ShopAdapter
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.BuyList
import io.moxd.shopforme.data.model.Shop
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString

class Shopcart : Fragment() {
    lateinit var list : RecyclerView
    lateinit var tabLayout : TabLayout
    lateinit var  refreshlayout : SwipeRefreshLayout
    var buylist: List<BuyList> = mutableListOf()
 //   lateinit var Buyadapter : BuyListAdapter
  //  lateinit var Shopadapter : ShopAdapter
    var shop: List<Shop> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.auth_shopcart_layout, container, false)
        list = root.findViewById(R.id.shopcart_list)
        tabLayout = root.findViewById(R.id.shopcart_tab)
        refreshlayout = root.findViewById(R.id.shopcart_list_refresh)
        list.layoutManager = LinearLayoutManager(
            root.context,
            LinearLayoutManager.VERTICAL,
            false
        )

        root.findViewById<com.nambimobile.widgets.efab.FabOption>(R.id.shopcart_addbuy).setOnClickListener {
            val ft = (requireActivity() as MainActivity).supportFragmentManager.beginTransaction()
            ft.replace(R.id.mainframe, BuylistAdd())
            ft.commit()
        }
        GetShop()
        refreshlayout.setOnRefreshListener(OnRefreshListener {
            when (tabLayout.selectedTabPosition) {
                0-> {
                    Log.d("Tab", "Shop clicked")
                    GetShop()

                }
                1 -> {
                    Log.d("Tab", "Buyclicked")

                    GetBuyList()
                }
            }
            refreshlayout.setRefreshing(false)
        })
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab!!.text) {
                    "Shopping" -> {
                        Log.d("Tab", "Shop clicked")
                        GetShop()

                    }
                    "BuyList" -> {
                        Log.d("Tab", "Buyclicked")

                        GetBuyList()
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })

        return root
    }


    fun GetShop(){
        GlobalScope.launch(context = Dispatchers.IO) {
            requireAuthManager().SessionID().take(1).collect {
                //do actions

                Fuel.get(
                    RestPath.shop(it)
                ).responseString { _, _, result ->

                    when (result) {


                        is Result.Failure -> {
                            this@Shopcart.activity?.runOnUiThread() {
                                Log.d(
                                    "Error",
                                    result.getException().message.toString()
                                )
                                Toast.makeText(
                                    this@Shopcart.requireContext(),
                                    "Login Failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        is Result.Success -> {
                            val data = result.get()

                            Log.d("USerProfile", data)

                            this@Shopcart.activity?.runOnUiThread() {

                                shop =
                                    JsonDeserializer.decodeFromString<List<Shop>>(
                                        data
                                    );
                                list.adapter =
                                    ShopAdapter(this@Shopcart.requireContext(), shop)

                            }
                        }
                    }
                }.join()


            }
        }
    }

    fun GetBuyList(){
        GlobalScope.launch(context = Dispatchers.IO) {
            requireAuthManager().SessionID().take(1).collect {
                //do actions

                Fuel.get(
                    RestPath.buylist(it)
                ).responseString { _, _, result ->

                    when (result) {


                        is Result.Failure -> {
                            this@Shopcart.activity?.runOnUiThread() {
                                Log.d(
                                    "Error",
                                    result.getException().message.toString()
                                )
                                Toast.makeText(
                                    this@Shopcart.requireContext(),
                                    "Login Failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        is Result.Success -> {
                            val data = result.get()

                            Log.d("USerProfile", data)

                            this@Shopcart.activity?.runOnUiThread() {

                                buylist =
                                    JsonDeserializer.decodeFromString<List<BuyList>>(
                                        data
                                    );
                                list.adapter =
                                    BuyListAdapter(this@Shopcart.requireContext(), buylist)

                            }
                        }
                    }
                }.join()


            }
        }
    }
}