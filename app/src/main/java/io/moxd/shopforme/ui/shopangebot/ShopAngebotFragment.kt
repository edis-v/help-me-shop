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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.android.material.tabs.TabLayout
import io.moxd.shopforme.*
import io.moxd.shopforme.adapter.AngebotHelperAdapter
import io.moxd.shopforme.adapter.ShopAdapter
import io.moxd.shopforme.data.RestPath

import io.moxd.shopforme.data.model.AngebotHelper
import io.moxd.shopforme.data.model.Shop
import io.moxd.shopforme.databinding.ShopangebotLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString

class ShopAngebotFragment : Fragment() {




    val viewModel : ShopAngebotViewModel  by viewModels { ShopAngebotViewModelFactory(this,arguments) }

    lateinit var binding: ShopangebotLayoutBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.shopangebot_layout, container, false)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ShopangebotLayoutBinding.bind(view)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        binding.apply {


            shopangebotList.layoutManager = LinearLayoutManager(
            root.context,
            LinearLayoutManager.VERTICAL,
            false
        )

            shopangebotRefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
                when (shopangebotTab.selectedTabPosition) {
                    0 -> {
                        Log.d("Tab", "Angebot clicked")

                       viewModel.getAngebotUpdate()
                    }
                    1 -> {
                        Log.d("Tab", "Shop clicked")

                         viewModel.getShopsUpdate()
                    }
                }
                shopangebotRefresh.setRefreshing(false)
            })



            shopangebotTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when (tab!!.text) {
                        "Shoppen" -> {
                            Log.d("Tab", "Shop clicked")
                            viewModel.getShopsUpdate()

                        }
                        "Angebote" -> {
                            Log.d("Tab", "Angebote clicked")

                            viewModel.getAngebotUpdate()
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




            ViewGroupCompat.setTransitionGroup(shopangebotList,true)

        }



        //observer

        binding.apply {
            viewModel.Angebote.observe(viewLifecycleOwner){
                if (it.isSuccessful) {
                    val Angebote = it.body()!!

                    shopangebotList.adapter =
                        AngebotHelperAdapter(requireContext(), Angebote)


                } else {
                    //error
                }
            }
            viewModel.Shops.observe(viewLifecycleOwner){
                if (it.isSuccessful) {
                    val Shops = it.body()!!

                    shopangebotList.adapter =
                        ShopAdapter(requireContext(), Shops)


                } else {
                    //error
                }
            }
        }


    }


}
