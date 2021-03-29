package io.moxd.shopforme.ui.shopbuylist.shopcart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewGroupCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import io.moxd.shopforme.*
import io.moxd.shopforme.R
import io.moxd.shopforme.adapter.BuyListAdapter
import io.moxd.shopforme.adapter.ShopAdapter
import io.moxd.shopforme.databinding.AuthShopcartLayoutBinding
import io.moxd.shopforme.ui.shopbuylist.buylistadd.BuylistAdd

class Shopcart : Fragment() {


    lateinit var observer: ShopcartLifecycleObserver
    val viewModel: ShopcartViewModel by viewModels {
        ShopcartViewModelFactory(this, arguments)
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        observer = ShopcartLifecycleObserver(requireActivity().activityResultRegistry, viewModel, requireContext())
        lifecycle.addObserver(observer)
        return inflater.inflate(R.layout.auth_shopcart_layout, container, false)

    }

    lateinit var binding: AuthShopcartLayoutBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = AuthShopcartLayoutBinding.bind(view)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        //setup Listener
        binding.apply {

            shopcartList.layoutManager = LinearLayoutManager(
                    root.context,
                    LinearLayoutManager.VERTICAL,
                    false
            )
            ViewGroupCompat.setTransitionGroup(shopcartList, true)

            shopcartListRefresh.setOnRefreshListener(OnRefreshListener {
                when (shopcartTab.selectedTabPosition) {
                    0 -> {
                        Log.d("Tab", "Shop clicked")
                        viewModel.getShopUpdate()

                    }
                    1 -> {
                        Log.d("Tab", "Buyclicked")
                        viewModel.getBuyListUpdate()
                    }
                }
                shopcartListRefresh.isRefreshing = false
            })

            shopcartTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when (tab!!.text) {
                        "Shopping" -> {
                            Log.d("Tab", "Shop clicked")
                            viewModel.getShopUpdate()

                        }
                        "BuyList" -> {
                            Log.d("Tab", "Buyclicked")

                            viewModel.getBuyListUpdate()
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

            shopcartAddbuy.setOnClickListener {
                val ft = (requireActivity() as MainActivity).supportFragmentManager.beginTransaction()
                ft.replace(R.id.mainframe, BuylistAdd())
                ft.commit()
            }

            shopcartFilter.setOnClickListener {
                shopcartFilterWindow.visibility = View.VISIBLE
                shopcartFloatingActionButton.visibility = View.GONE

            }

            shopcartLocation.setOnClickListener {
                observer.LocationAction()
            }

            shopcartFilterExit.setOnClickListener {
                shopcartFilterWindow.visibility = View.GONE
                shopcartFloatingActionButton.visibility = View.VISIBLE
            }


            try {
                val b = arguments?.getCharSequence("page") as String
                shopcartTab.selectTab(shopcartTab.getTabAt(1))
                viewModel.getBuyListUpdate()
            } catch (ex: Exception) {
                viewModel.getShopUpdate()

            }


        }

        binding.apply {
            viewModel.BuyList.observe(viewLifecycleOwner) {
                if (it.isSuccessful) {
                    val BuyList = it.body()!!

                    shopcartList.adapter =
                            BuyListAdapter(
                                    this@Shopcart.requireContext(),
                                    BuyList.toMutableList(),
                                    viewModel
                            )


                } else {
                    //error
                }
            }
        }

        binding.apply {
            viewModel.Location.observe(viewLifecycleOwner) {
                if (it.isSuccessful) {
                    Snackbar.make(view, "Location updated Successfully", Snackbar.LENGTH_LONG).show()
                } else
                    Snackbar.make(view, "Location update Failed", Snackbar.LENGTH_LONG).show()

            }
        }

        binding.apply {

            viewModel.BuyListDelte.observe(viewLifecycleOwner) {
                if (it.isSuccessful) {


                    (shopcartList.adapter as BuyListAdapter).deleteSuccesses()
                    Snackbar
                            .make(
                                    view,
                                    "EinkaufsListe gelöscht",
                                    Snackbar.LENGTH_LONG
                            )
                            .show()
                } else {
                    Snackbar
                            .make(
                                    view,
                                    getErrorRetro(it.errorBody()),
                                    Snackbar.LENGTH_LONG
                            ).show()
                }
            }

            viewModel.ShopCreated.observe(viewLifecycleOwner) {
                if (it.isSuccessful) {
                    Snackbar
                            .make(
                                    view,
                                    "Einkauf erstellt",
                                    Snackbar.LENGTH_LONG
                            ).setAction(
                                    "Go to Shop")
                            {

                                val ft = (context as MainActivity).supportFragmentManager.beginTransaction()
                                ft.replace(R.id.mainframe, Shopcart())
                                ft.commit()
                            }
                            .show()
                } else {
                    Snackbar
                            .make(
                                    view,
                                    getErrorRetro(it.errorBody()),
                                    Snackbar.LENGTH_LONG
                            ).show()
                }
            }

        }

        binding.apply {
            viewModel.Shop.observe(viewLifecycleOwner) {
                if (it.isSuccessful) {
                    val Shop = it.body()!!

                    shopcartList.adapter =
                            ShopAdapter(this@Shopcart.requireContext(), Shop)


                } else {
                    //error
                }
            }
        }
    }


}




