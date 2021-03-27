package io.moxd.shopforme.ui.shopbuylist

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.transition.Slide
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.result.Result
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.nambimobile.widgets.efab.ExpandableFab
import com.nambimobile.widgets.efab.ExpandableFabLayout
import io.moxd.shopforme.*
import io.moxd.shopforme.R
import io.moxd.shopforme.adapter.BuyListAdapter
import io.moxd.shopforme.adapter.ShopAdapter
import io.moxd.shopforme.data.AuthManager
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.BuyList
import io.moxd.shopforme.data.model.LocationData
import io.moxd.shopforme.data.model.Shop

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class Shopcart : Fragment() {
    lateinit var list : RecyclerView
    lateinit var tabLayout : TabLayout
    lateinit var  refreshlayout : SwipeRefreshLayout
    lateinit var shopfilterwindow :CardView
    lateinit var shopcartfab : ExpandableFab
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    var buylist: List<BuyList> = mutableListOf()
    var filtered = false //observe the data to enable disable reset filter or hard code
    lateinit var closewindow : ImageView
 //  lateinit var Buyadapter : BuyListAdapter
 //   lateinit var Shopadapter : ShopAdapter
    var shop: List<Shop> = mutableListOf()



    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 5
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.myLooper()
        )
    }
    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            val lat: Double = mLastLocation.latitude
            val long: Double = mLastLocation.longitude

            GlobalScope.launch(context = Dispatchers.IO) {


                                val url =
                                        FuelManager.instance.basePath + RestPath.locationUpdate(
                                            requireAuthManager().SessionID()
                                        )

                                Log.d("URL", url)
                                Log.d("HomeLocation", "$lat $long")
                                val data = LocationData(
                                        type = "Point",
                                        coordinates = listOf(
                                                lat,
                                                long
                                        ) as List<Double>
                                )
                                if (requireAuthManager().SessionID().isEmpty())
                                    Fuel.put(
                                            url, listOf(
                                            "location" to JsonDeserializer.encodeToString(
                                                    data
                                            )
                                    )
                                    )
                                            .responseString { request, response, result ->

                                                when (result) {
                                                    is Result.Success -> {
                                                        Log.d(
                                                                "result",
                                                                result.get()
                                                        )

                                                    }
                                                    is Result.Failure -> {
                                                        Log.d(
                                                                "Error",
                                                                getError(
                                                                        response
                                                                )
                                                        )


                                                    }

                                                }


                                            }.join()
                            }
                }}

    fun OnGPS() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes",
                DialogInterface.OnClickListener { dialog, which -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
                .setNegativeButton("No",
                        DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.auth_shopcart_layout, container, false)
        list = root.findViewById(R.id.shopcart_list)

        list.layoutManager = LinearLayoutManager(
                root.context,
                LinearLayoutManager.VERTICAL,
                false
        )


        tabLayout = root.findViewById(R.id.shopcart_tab)
        refreshlayout = root.findViewById(R.id.shopcart_list_refresh)
        ViewGroupCompat.setTransitionGroup(list,true)
        shopfilterwindow = root.findViewById(R.id.shopcart_filter_window)
        shopcartfab = root.findViewById(R.id.shopcart_floating_action_button)
        closewindow = root.findViewById(R.id.shopcart_filter_exit)
        try {
            val b =  arguments?.getCharSequence("page") as String
            tabLayout.selectTab(tabLayout.getTabAt(1))
            GetBuyList()
        }catch (ex: Exception){
            GetShop()

        }
       // list.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        root.findViewById<com.nambimobile.widgets.efab.FabOption>(R.id.shopcart_addbuy).setOnClickListener {
            val ft = (requireActivity() as MainActivity).supportFragmentManager.beginTransaction()
            ft.replace(R.id.mainframe, BuylistAdd())
            ft.commit()
        }

        root.findViewById<com.nambimobile.widgets.efab.FabOption>(R.id.shopcart_filter).setOnClickListener {
                shopfilterwindow.visibility = View.VISIBLE
                shopcartfab.visibility = View.GONE

        }

        root.findViewById<com.nambimobile.widgets.efab.FabOption>(R.id.shopcart_filter).setOnClickListener {
            requestNewLocationData()
        }

        closewindow.setOnClickListener {
            shopfilterwindow.visibility = View.GONE
            shopcartfab.visibility = View.VISIBLE
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        requestNewLocationData()

        val nManager: LocationManager =
                requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!nManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            OnGPS();

        if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {

        } else {

            mFusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            val lat: Double = location.latitude
                            val long: Double = location.longitude
                            GlobalScope.launch(context = Dispatchers.IO) {


                                            val url =
                                                    FuelManager.instance.basePath + RestPath.locationUpdate(
                                                        requireAuthManager().SessionID()
                                                    )

                                            Log.d("URL", url)
                                            Log.d("HomeLocation", "$lat $long")
                                            val data = LocationData(
                                                    type = "Point",
                                                    coordinates = listOf(
                                                            lat,
                                                            long
                                                    ) as List<Double>
                                            )
                                            if (requireAuthManager().SessionID().isNotEmpty())
                                                Fuel.put(
                                                        url, listOf(
                                                        "location" to JsonDeserializer.encodeToString(
                                                                data
                                                        )
                                                )
                                                )
                                                        .responseString { request, response, result ->

                                                            when (result) {
                                                                is Result.Success -> {
                                                                    Log.d(
                                                                            "result",
                                                                            result.get()
                                                                    )

                                                                }
                                                                is Result.Failure -> {
                                                                    Log.d(
                                                                            "Error",
                                                                            getError(
                                                                                    response
                                                                            )
                                                                    )


                                                                }

                                                            }


                                                        }.join()

                            }
                        } else {
                            val mLocationRequest = LocationRequest()
                            mLocationRequest.priority =
                                    LocationRequest.PRIORITY_HIGH_ACCURACY
                            mLocationRequest.interval = 5
                            mLocationRequest.fastestInterval = 0
                            mLocationRequest.numUpdates = 1

                            // setting LocationRequest
                            // on FusedLocationClient

                            // setting LocationRequest
                            // on FusedLocationClient
                            mFusedLocationClient =
                                    LocationServices.getFusedLocationProviderClient(
                                            requireContext()
                                    )
                            mFusedLocationClient.requestLocationUpdates(
                                    mLocationRequest,
                                    mLocationCallback,
                                    Looper.myLooper()
                            )
                        }
                    }


        }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

    }

    fun GetShop(){
        GlobalScope.launch(context = Dispatchers.IO) {

                //do actions

                Fuel.get(
                    RestPath.shop(requireAuthManager().SessionID())
                ).responseString { _, response, result ->

                    when (result) {


                        is Result.Failure -> {
                            this@Shopcart.activity?.runOnUiThread() {
                                Log.d(
                                    "Error",
                                        getError(response)
                                )
                                Toast.makeText(
                                    this@Shopcart.requireContext(),
                                        getError(response),
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


    fun shopDeleted(){
        Snackbar
                .make(
                        requireView(),
                        "Shop is deleted",
                        Snackbar.LENGTH_LONG
                ).show()
    }

    fun GetBuyList(){
        GlobalScope.launch(context = Dispatchers.IO) {

                //do actions

                Fuel.get(
                    RestPath.buylist(requireAuthManager().SessionID())
                ).responseString { _, response, result ->

                    when (result) {


                        is Result.Failure -> {
                            this@Shopcart.activity?.runOnUiThread() {
                                Log.d(
                                    "Error",
                                        getError(response)
                                )
                                Toast.makeText(
                                    this@Shopcart.requireContext(),
                                        getError(response),
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
                                    BuyListAdapter(this@Shopcart.requireContext(), buylist.toMutableList())

                            }
                        }
                    }
                }.join()



        }
    }
}