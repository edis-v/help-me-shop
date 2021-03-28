package io.moxd.shopforme.ui.map


import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PointF
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.Looper.getMainLooper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.result.Result
import com.google.android.gms.location.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolLongClickListener
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.pluginscalebar.ScaleBarOptions
import com.mapbox.pluginscalebar.ScaleBarPlugin
import com.squareup.picasso.Picasso
import io.moxd.shopforme.*
import io.moxd.shopforme.R
import io.moxd.shopforme.adapter.MapAdapter
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.LocationData
import io.moxd.shopforme.data.model.LocationDataGSON
import io.moxd.shopforme.data.model.ShopMap
import io.moxd.shopforme.databinding.MapFragmentLayoutBinding
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.lang.ref.WeakReference


class MapFragment : Fragment() , OnMapReadyCallback {




    private  lateinit var mystyle  : Style
    private  lateinit var mapbox: MapboxMap
    private val SYMBOL_ICON_ID = "SYMBOL_ICON_ID"

    private  var Maxnumber  = -1


    lateinit var binding: MapFragmentLayoutBinding
    lateinit var  observer: MapLifecycleObserver

    lateinit var symbolManager : SymbolManager

    val viewModel : MapViewModel by viewModels { MapViewModelFactory(this,arguments) }








    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        Mapbox.getInstance(requireActivity(), getString(R.string.mapbox_access_token))
        val root = inflater.inflate(R.layout.map_fragment_layout, container, false)
        val mapView = root.findViewById<MapView>(R.id.map_View)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this@MapFragment)
        observer = MapLifecycleObserver(requireActivity().activityResultRegistry,LocationServices.getFusedLocationProviderClient(requireContext()),viewModel, requireContext())
       return root



    }


    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MapFragmentLayoutBinding.bind(view)
        binding.apply {
            rvOnTopOfMap.layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL, true
            )
            val snapHelper: SnapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(rvOnTopOfMap)
            rvOnTopOfMap.itemAnimator = DefaultItemAnimator()


            sliderKm.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {

                }

                override fun onStopTrackingTouch(slider: Slider) {

                        val radius = slider.value.toInt()
                        Log.d("Radius",radius.toString())
                        viewModel.getOtherUsers(radius)

                }


            })
        }

        binding.apply {
            viewModel.User.observe(viewLifecycleOwner) {


                if(it.isSuccessful) {

                     val customLocationComponentOptions = LocationComponentOptions.builder(requireContext())
                            .trackingGesturesManagement(true).pulseEnabled(true)
                            .pulseColor(ContextCompat.getColor(requireContext(), R.color.mapbox))
                            .build()

                    val locationComponentActivationOptions = LocationComponentActivationOptions.builder(
                            requireContext(),
                            mystyle
                    )
                            .locationComponentOptions(customLocationComponentOptions)
                            .build()

// Get an instance of the LocationComponent and then adjust its settings
                    mapbox.locationComponent.apply {

// Activate the LocationComponent with options
                        activateLocationComponent(locationComponentActivationOptions)

// Enable to make the LocationComponent visible
                        isLocationComponentEnabled = true


// Set the LocationComponent's camera mode
                        cameraMode = CameraMode.TRACKING

// Set the LocationComponent's render mode
                        renderMode = RenderMode.NORMAL
                    }
                    val position = CameraPosition.Builder()
                            .target(LatLng(viewModel.lastKnownLocation.latitude, viewModel.lastKnownLocation.longitude))
                            .zoom(16.0)
                            .tilt(20.0)
                            .build()
                    mapbox.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000)


                    viewModel.getOtherUsers(1 )

                }else
                {

                    Toast.makeText(requireContext(),"Error: ${getErrorRetro(it.errorBody())}", Toast.LENGTH_LONG).show()
                }

            }
            viewModel.OtherUsers.observe(viewLifecycleOwner) {

                if (it.isSuccessful) {
                    val otherusers = it.body()!!
                    symbolManager.deleteAll()
                    mystyle.addImage(
                            SYMBOL_ICON_ID, BitmapFactory.decodeResource(
                            this@MapFragment.resources, R.drawable.icon_info
                    )
                    )

                    mapbox.uiSettings.isScrollGesturesEnabled = otherusers.isNotEmpty()

                    for (user in otherusers)
                        symbolManager.create(
                                SymbolOptions()
                                        .withLatLng(user.helpsearcher.location.getLatLong())
                                        .withIconImage(SYMBOL_ICON_ID)
                                        .withIconSize(1.0f)


                        )
                    symbolManager?.iconAllowOverlap = true
                    symbolManager?.iconAllowOverlap = true
                    symbolManager?.iconTranslate = arrayOf(-4f, 5f)
                    symbolManager?.iconRotationAlignment = Property.ICON_ROTATION_ALIGNMENT_VIEWPORT



                    rvOnTopOfMap.adapter = MapAdapter(requireContext(), otherusers, mapbox, viewModel)





                    if (otherusers.isNotEmpty()) {

                        val bounds = LatLngBounds.Builder()

                        for (i in otherusers)
                            bounds.include(i.helpsearcher.location.getLatLong())
                        bounds.include(
                                LatLng(
                                        viewModel.lastKnownLocation.latitude,
                                        viewModel.lastKnownLocation.longitude
                                )
                        )

                        val boundsbuild = bounds.build()
                        mapbox.setLatLngBoundsForCameraTarget(boundsbuild)


                        mapbox.easeCamera(
                                CameraUpdateFactory.newLatLngBounds(boundsbuild, 400),
                                1000
                        )
                    }

                }else
                {
                    Toast.makeText(requireContext(), getErrorRetro(it.errorBody()),Toast.LENGTH_LONG).show()
                }
                viewModel.getMaxUser()

            }

                viewModel.OtherUsersMax.observe(viewLifecycleOwner) {
                    if (it.isSuccessful)
                        mapMaxlocations.text = "User im 100km Radius ${it.body()!!.size}"
                    else
                        mapMaxlocations.text = "Failed"
                }


        }

        binding.apply {

            viewModel.Angebot.observe(viewLifecycleOwner){
                if(it.isSuccessful){
                    Snackbar.make(view,"Erfolgreich erstellt",Snackbar.LENGTH_LONG).show()
                }else{
                    Snackbar.make(view, getErrorRetro(it.errorBody()),Snackbar.LENGTH_LONG).show()
                }
            }

        }
    }

    lateinit var  scaleBarPlugin : ScaleBarPlugin
    override fun onMapReady(mapboxMap: MapboxMap) {
        //start location update

        Log.d("REAd","Map")
        mapbox = mapboxMap


                    mapboxMap.setStyle(
                            Style.MAPBOX_STREETS
                    ) { style ->

                        mystyle = style


                        mapboxMap.setMinZoomPreference(5.89);
                        mapboxMap.setMaxZoomPreference(16.0);

                        mapboxMap.uiSettings.isTiltGesturesEnabled = false
                        //mapboxMap.uiSettings.isRotateGesturesEnabled = false
                        mapboxMap.uiSettings.isZoomGesturesEnabled = false
                        mapboxMap.uiSettings.isScrollGesturesEnabled = false



                        binding.apply {
                            symbolManager =  SymbolManager(mapView, mapbox, mystyle)
                            scaleBarPlugin = ScaleBarPlugin(mapView, mapboxMap)

                            val scaleBarOptions = ScaleBarOptions(requireContext())
                            scaleBarOptions
                                    .setTextColor(R.color.black)
                                    .setTextSize(40f)
                                    .setBarHeight(15f)
                                    .setBorderWidth(5f)
                                    .setMetricUnit(true)
                                    .setRefreshInterval(15)
                                    .setMarginTop(30f)
                                    .setMarginLeft(16f)
                                    .setTextBarMargin(15f)
                            scaleBarPlugin.create(scaleBarOptions)
                        }


                        //try get locationPermission

                        observer.GetLocationAction()
                    }





    }






















    override fun onStart() {
        super.onStart()
        binding.apply {
            mapView.onStart()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            mapView.onResume()
        }

       // observer.GetLocationAction()
    }

    override fun onPause() {
        super.onPause()
        binding.apply {
            mapView.onPause()
        }
    }

    override fun onStop() {
        super.onStop()
        binding.apply {
            mapView.onStop()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.apply {
            mapView.onSaveInstanceState(outState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    binding.apply {
        mapView.onDestroy()
    }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.apply {
            mapView.onLowMemory()
        }
    }








}