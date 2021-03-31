package io.moxd.shopforme.ui.shopbuylist.shopcart

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.moxd.shopforme.data.model.LocationDataGSON
import io.moxd.shopforme.data.model.LocationGSON

class ShopcartLifecycleObserver(private val registry: ActivityResultRegistry, private val viewModel: ShopcartViewModel, val context: Context) :
        DefaultLifecycleObserver {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var requestLocationPermission: ActivityResultLauncher<String>
    override fun onCreate(owner: LifecycleOwner) {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        requestLocationPermission = registry.register("locatrionperm", owner, ActivityResultContracts.RequestPermission()) {
            if (it)
                getLocation()
        }


    }

    private fun getLocation() {
        val nManager: LocationManager =
                (context as Activity).getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!nManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            OnGPS();
        else
            requestNewLocationData()
    }

    fun LocationAction() {


        when {
            ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLocation()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                //
                MaterialAlertDialogBuilder(context)
                        .setTitle("Berechtigung")
                        .setMessage("Um dem Helfer ihren Standort zu zeigen benötigen wir die Berechting auf ihren Standort")
                        .setNeutralButton("Nein Danke") { _, _ ->
                            // Respond to neutral button press
                        }
                        .setPositiveButton("Ja") { _, _ ->

                            requestLocationPermission.launch(
                                    Manifest.permission.ACCESS_FINE_LOCATION)
                        }.show()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestLocationPermission.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }


    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 5
        mLocationRequest.fastestInterval = 1
        mLocationRequest.numUpdates = 1

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
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
            Log.d("Location", "lat $lat  ::: long $long")
            viewModel.updateLocation(lat, long)
        }
    }


    private fun OnGPS() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes",
                DialogInterface.OnClickListener { dialog, which -> (context as Activity).startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
                .setNegativeButton("No",
                        DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }
}