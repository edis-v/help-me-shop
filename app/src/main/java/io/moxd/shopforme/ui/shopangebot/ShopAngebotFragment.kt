package io.moxd.shopforme.ui.shopangebot

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewGroupCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.android.material.tabs.TabLayout
import io.moxd.shopforme.*
import io.moxd.shopforme.adapter.AngebotAdapter
import io.moxd.shopforme.adapter.AngebotHelperAdapter
import io.moxd.shopforme.adapter.BuyListAdapter
import io.moxd.shopforme.adapter.ShopAdapter
import io.moxd.shopforme.data.RestPath

import io.moxd.shopforme.data.model.AngebotHelper
import io.moxd.shopforme.data.model.BuyList
import io.moxd.shopforme.data.model.Shop
import io.moxd.shopforme.ui.shopbuylist.BuylistAdd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString

class ShopAngebotFragment : Fragment() {
    lateinit var list : RecyclerView
    lateinit var tabLayout : TabLayout
    lateinit var  refreshlayout : SwipeRefreshLayout
    var angebotlist: List<AngebotHelper> = mutableListOf()
    //   lateinit var Buyadapter : BuyListAdapter
    //  lateinit var Shopadapter : ShopAdapter
    var shop: List<Shop> = mutableListOf()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.shopangebot_layout, container, false)
        list = root.findViewById(R.id.shopangebot_list)
        list.layoutManager = LinearLayoutManager(
                root.context,
                LinearLayoutManager.VERTICAL,
                false
        )
        GetAgebotList()
        tabLayout = root.findViewById(R.id.shopangebot_tab)
        refreshlayout = root.findViewById(R.id.shopangebot_Refresh)
        ViewGroupCompat.setTransitionGroup(list,true)



        list.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        root.findViewById<com.nambimobile.widgets.efab.FabOption>(R.id.shopangebot_filter).setOnClickListener {
                //fgilter
        }





        refreshlayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            when (tabLayout.selectedTabPosition) {
                0 -> {
                    Log.d("Tab", "Angebot clicked")

                        GetAgebotList()
                }
                1 -> {
                    Log.d("Tab", "Shop clicked")

                    GetShop()
                }
            }
            refreshlayout.setRefreshing(false)
        })
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab!!.text) {
                    "Shoppen" -> {
                        Log.d("Tab", "Shop clicked")
                        GetShop()

                    }
                    "Angebote" -> {
                        Log.d("Tab", "Angebote clicked")

                        GetAgebotList()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

    }

    fun GetShop(){
        GlobalScope.launch(context = Dispatchers.IO) {
            requireAuthManager().SessionID().take(1).collect {
                //do actions

                Fuel.get(
                        RestPath.shop(it)
                ).responseString { _, response, result ->

                    when (result) {


                        is Result.Failure -> {
                            this@ShopAngebotFragment.activity?.runOnUiThread() {
                                Log.d(
                                        "Error",
                                      getError(response)
                                )
                                Toast.makeText(
                                        this@ShopAngebotFragment.requireContext(),
                                        getError(response),
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        is Result.Success -> {
                            val data = result.get()

                            Log.d("USerProfile", data)

                            this@ShopAngebotFragment.activity?.runOnUiThread() {

                                shop =
                                        JsonDeserializer.decodeFromString<List<Shop>>(
                                                data
                                        );
                                list.adapter =
                                        ShopAdapter(this@ShopAngebotFragment.requireContext(), shop)

                            }
                        }
                    }
                }.join()


            }
        }
    }

    fun GetAgebotList(){
        GlobalScope.launch(context = Dispatchers.IO) {
            requireAuthManager().SessionID().take(1).collect {
                //do actions

                Fuel.get(
                        RestPath.angebote(it)
                ).responseString { _, response, result ->

                    when (result) {


                        is Result.Failure -> {
                            this@ShopAngebotFragment.activity?.runOnUiThread() {
                                Log.d(
                                        "Error",
                                        getError(response)
                                )
                                Toast.makeText(
                                        this@ShopAngebotFragment.requireContext(),
                                        getError(response),
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        is Result.Success -> {
                            val data = result.get()

                            Log.d("USerProfile", data)

                            this@ShopAngebotFragment.activity?.runOnUiThread() {

                                angebotlist =
                                        JsonDeserializer.decodeFromString<List<AngebotHelper>>(
                                                data
                                        );
                                list.adapter =
                                        AngebotHelperAdapter(this@ShopAngebotFragment.requireContext(), angebotlist.toMutableList())

                            }
                        }
                    }
                }.join()


            }
        }
    }
}
