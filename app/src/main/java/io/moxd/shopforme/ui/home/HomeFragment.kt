package io.moxd.shopforme.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
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
import io.moxd.shopforme.data.model.LocationData
import io.moxd.shopforme.data.model.UserME
import io.moxd.shopforme.databinding.MainHomeFragmentBinding
import io.moxd.shopforme.ui.angebot.AngebotFragment
import io.moxd.shopforme.ui.map.MapFragment
import io.moxd.shopforme.ui.profile_list.ProfileListFragment
import io.moxd.shopforme.ui.shopangebot.ShopAngebotFragment
import io.moxd.shopforme.ui.shopbuylist.Shopcart
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString


class HomeFragment: Fragment(R.layout.main_home_fragment) {


    lateinit var binding: MainHomeFragmentBinding
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var mainframe: FrameLayout
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    var last = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return super.onCreateView(inflater, container, savedInstanceState)

    }
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
                requireAuthManager().SessionID().take(1)
                    .collect {

                        val url =
                            FuelManager.instance.basePath + RestPath.locationUpdate(
                                it
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
                        if (it.isNullOrEmpty())
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
    }}}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MainHomeFragmentBinding.bind(view)
       // ft = (requireActivity() as MainActivity).getSupportFragmentManager().beginTransaction()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        requestNewLocationData()
        mainframe = view.findViewById(R.id.mainframe)
        bottomNavigationView = view.findViewById(R.id.bottom_navigation)

        //get user
        val job: Job = GlobalScope.launch(context = Dispatchers.IO) {
            requireAuthManager().SessionID().take(1).collect {
                //do actions

                Fuel.get(
                    RestPath.user(it)
                ).responseString { _, response, result ->

                    when (result) {


                        is Result.Failure -> {

                            GlobalScope.launch(Dispatchers.IO) {
                                Log.d("Error", getError(response))
                            }

                        }
                        is Result.Success -> {
                            val data = result.get()

                            Log.d("USerProfile", data)

                            this@HomeFragment.activity?.runOnUiThread() {

                                AuthManager.User = JsonDeserializer.decodeFromString<UserME>(data);

                                if (AuthManager.User!!.usertype_txt == "Helfer") {
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
                                Firebase.messaging.isAutoInitEnabled = true

                                FirebaseMessaging.getInstance().token.addOnCompleteListener(
                                    OnCompleteListener { task ->
                                        if (!task.isSuccessful) {
                                            Log.w(
                                                "Firebase",
                                                "Fetching FCM registration token failed",
                                                task.exception
                                            )
                                            return@OnCompleteListener
                                        }

                                        try {
                                            // Get new FCM registration token
                                            val token = task.result

                                            // sendRegistrationToServer(token)
                                            // Log and toast
                                            val msg = getString(R.string.msg_token_fmt, token)
                                            Log.d("Firebase", msg)
                                        } catch (ex: Exception) {

                                        }
                                        //    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                                    })


                                updateNavbar()
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
                                                    requireAuthManager().SessionID().take(1)
                                                        .collect {

                                                            val url =
                                                                FuelManager.instance.basePath + RestPath.locationUpdate(
                                                                    it
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
                                                            if (it.isNullOrEmpty())
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
                            }

                        }
                    }
                }


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
                    true
                }
                R.id.item2 -> {
                    // Respond to navigation item 2 click
                    if (AuthManager.User!!.usertype_txt == "Helfer") {
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
                    true
                }
                R.id.item3 -> {
                    // Respond to navigation item 3 click
                    val ft =
                        (requireActivity() as MainActivity).supportFragmentManager.beginTransaction()
                    ft.replace(R.id.mainframe, ProfileListFragment())
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

    fun OnGPS() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes",
            DialogInterface.OnClickListener { dialog, which -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
            .setNegativeButton("No",
                DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
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