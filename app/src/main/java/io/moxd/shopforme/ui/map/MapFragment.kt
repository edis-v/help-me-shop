package io.moxd.shopforme.ui.map

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PointF
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.coroutines.awaitObject
import com.github.kittinunf.fuel.serialization.kotlinxDeserializerOf
import com.github.kittinunf.result.Result
import com.google.android.material.slider.Slider
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
import io.moxd.shopforme.JsonDeserializer
import io.moxd.shopforme.R
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.UserManager
import io.moxd.shopforme.data.model.LocationData
import io.moxd.shopforme.data.model.OtherUser
import io.moxd.shopforme.requireAuthManager
import io.moxd.shopforme.requireUserManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString


class MapFragment : Fragment() , OnMapReadyCallback, PermissionsListener,MapboxMap.OnMapClickListener {
    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapView : MapView
    private  lateinit var  symbolManager : SymbolManager
    private  lateinit var mystyle  : Style
    private val SYMBOL_ICON_ID = "SYMBOL_ICON_ID"
    private val SOURCE_ID = "SOURCE_ID"
    private val LAYER_ID = "LAYER_ID"
    private val CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID"
    private val PROPERTY_SELECTED = "selected"
    private val PROPERTY_NAME = "name"
    private val MARKER_IMAGE_ID = "MARKER_IMAGE_ID"
    private val MARKER_LAYER_ID = "MARKER_LAYER_ID"
    private val PROPERTY_CAPITAL = "capital"
    private val coordinates = arrayOf(
        LatLng(50.632, 6.48333),
        LatLng(50.6343, 6.44333),
        LatLng(50.6231, 6.45333),
        LatLng(50.6432, 6.48333),
        LatLng(50.6535, 6.48333),
        LatLng(50.6134, 6.48333),
        LatLng(50.6036, 6.48333)
    )
    private  var otheruserLocations :List<OtherUser> = mutableListOf()
    private  lateinit var mycontext : Context
    private lateinit var lastKnownLocation : Location
    var recyclerlist : List<SingleRecyclerViewLocation> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(requireActivity(), getString(R.string.mapbox_access_token))
        val root = inflater.inflate(R.layout.map_fragment_layout, container, false)


        mycontext = root.context
        mapView = root.findViewById(R.id.map_View)
        val slider = root.findViewById<Slider>(R.id.slider_km)
        val job: Job = GlobalScope.launch(context = Dispatchers.IO) {
            requireAuthManager().SessionID().take(1).collect {

                val url = FuelManager.instance.basePath + RestPath.otherUsers(
                    it,
                    slider.value.toInt().toString()
                )

                Log.d("URL", url)

                Fuel.get(
                    url
                ).responseString { request, response, result ->

                    when (result) {
                        is Result.Success -> {
                            Log.d("result", result.get())
                            otheruserLocations =
                                JsonDeserializer.decodeFromString<List<OtherUser>>(
                                    result.get()
                                )
                        }
                        is Result.Failure -> {
                            Log.d("result", result.getException().message.toString())
                            Toast.makeText(mycontext, "Failure", Toast.LENGTH_LONG)
                        }

                    }


                }.join()
                Log.d("COunt", otheruserLocations.size.toString())

            }

        }
        job.start()


        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {

            }

            override fun onStopTrackingTouch(slider: Slider) {
                if(lastKnownLocation != null){
                GlobalScope.launch {
                    // Responds to when slider's touch event is being stopped
                    val job: Job = GlobalScope.launch(context = Dispatchers.IO) {
                        requireAuthManager().SessionID().take(1).collect {

                            val url = FuelManager.instance.basePath + RestPath.otherUsers(
                                it,
                                slider.value.toInt().toString()
                            )

                            Log.d("URL", url)

                            Fuel.get(
                                url
                            ).responseString { request, response, result ->

                                when (result) {
                                    is Result.Success -> {
                                        Log.d("result", result.get())
                                        otheruserLocations =
                                            JsonDeserializer.decodeFromString<List<OtherUser>>(
                                                result.get()
                                            )
                                    }
                                    is Result.Failure -> {
                                        Log.d("result", result.getException().message.toString())
                                        Toast.makeText(mycontext, "Failure", Toast.LENGTH_LONG)
                                        otheruserLocations = mutableListOf()
                                    }

                                }


                            }.join()
                            Log.d("COunt", otheruserLocations.size.toString())

                        }

                    }
                    job.join()
                    initFeatureCollection();

                    Log.d("OtherUserCount", otheruserLocations.size.toString())
                    LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                    withContext(Dispatchers.Main) {
                        recyclerlist =  createRecyclerViewLocations()!!
                        val locationAdapter = LocationRecyclerViewAdapter(
                            recyclerlist,
                            mapboxMap, mystyle
                        )

                        recyclerView.adapter = locationAdapter
                        symbolManager.deleteAll()
                        createOtherUser(mystyle)

                            val bounds = LatLngBounds.Builder()

                            for (i in otheruserLocations)
                                bounds.include(i.location.getLatLong())
                        bounds.include( LatLng(
                            lastKnownLocation!!.latitude,
                            lastKnownLocation!!.longitude
                        ))
                            val position = CameraPosition.Builder()
                                .target(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    )
                                )
                                .zoom(16 - slider.value.toDouble())
                                .tilt(20.0)
                                .build()
                            val boundsbuild = bounds.build()
                            mapboxMap.setLatLngBoundsForCameraTarget(boundsbuild)


                            mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(boundsbuild, 400)
                            ,
                            1000
                        )
                        }
                    }
                }}


        })






// This contains the MapView in XML and needs to be called after the access token is configured.


        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return root
    }

    lateinit var  scaleBarPlugin : ScaleBarPlugin
    override fun onMapReady(mapboxMap: MapboxMap) {
        this@MapFragment.mapboxMap = mapboxMap



        mapboxMap.setStyle(
            Style.MAPBOX_STREETS
        ) { style ->
            mystyle = style
            mapboxMap.setMinZoomPreference(5.89);
            mapboxMap.setMaxZoomPreference(16.0);

            mapboxMap.uiSettings.isTiltGesturesEnabled = false
            //mapboxMap.uiSettings.isRotateGesturesEnabled = false
            mapboxMap.uiSettings.isZoomGesturesEnabled = false
          //  mapboxMap.uiSettings.isScrollGesturesEnabled = false
            enableLocationComponent(style)
            initFeatureCollection();
            // initMarkerIcons(style);
            createOtherUser(style)
            setUpMarkerLayer(style);
            initRecyclerView();
            // Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
            setUpInfoWindowLayer(style)


            scaleBarPlugin = ScaleBarPlugin(mapView!!, mapboxMap)

            val scaleBarOptions = ScaleBarOptions(mycontext)
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

            // Give the plugin the ScaleBarOptions object to style the scale bar
            scaleBarPlugin.create(scaleBarOptions)


            mapboxMap.addOnMapClickListener(this@MapFragment);

        }
    }

    fun createOtherUser(style: Style){

        symbolManager = SymbolManager(mapView, mapboxMap, style)
        style.addImage(
            SYMBOL_ICON_ID, BitmapFactory.decodeResource(
                this.resources, R.drawable.icon_info
            )
        )
        for (user in otheruserLocations)
            symbolManager.create(
                SymbolOptions()
                    .withLatLng(user.location.getLatLong())
                    .withIconImage(SYMBOL_ICON_ID)
                    .withIconSize(1.0f)


            )



        symbolManager.addClickListener(OnSymbolClickListener { symbol ->
            Toast.makeText(
                mycontext,
                "Hello World", Toast.LENGTH_SHORT
            ).show()
            symbol.iconImage = SYMBOL_ICON_ID
            symbolManager.update(symbol)

            markerAnimator = ValueAnimator()
            markerAnimator?.setObjectValues(2f, 1f)
            markerAnimator?.setDuration(300)
            markerAnimator?.addUpdateListener(ValueAnimator.AnimatorUpdateListener { animator ->
                symbol.iconSize = (animator.animatedValue as Float)
                symbolManager.update(symbol)
            })
            markerAnimator?.start()
            return@OnSymbolClickListener true
        })

        symbolManager.addLongClickListener(OnSymbolLongClickListener { symbol ->
            Toast.makeText(
                mycontext,
                "Hello World long", Toast.LENGTH_SHORT
            ).show()
            symbol.iconImage = SYMBOL_ICON_ID
            symbolManager.update(symbol)
            return@OnSymbolLongClickListener true
        })

// set non-data-driven properties, such as:
        symbolManager?.iconAllowOverlap = true
        symbolManager?.iconTranslate = arrayOf(-4f, 5f)
        symbolManager?.iconRotationAlignment = Property.ICON_ROTATION_ALIGNMENT_VIEWPORT
    }

    private fun selectMarker(iconLayer: SymbolLayer) {
        markerAnimator?.setObjectValues(2f, 1f)
        markerAnimator?.setDuration(300)
        markerAnimator?.addUpdateListener(ValueAnimator.AnimatorUpdateListener { animator ->
            iconLayer.setProperties(
                PropertyFactory.iconSize(animator.animatedValue as Float)
            )
        })
        markerAnimator?.start()
        markerAnimator?.start()
        markerSelected = true
    }

    private var markerSelected = false
    private var markerAnimator: ValueAnimator? = null
    private fun deselectMarker(iconLayer: SymbolLayer) {
        markerAnimator?.setObjectValues(2f, 1f)
        markerAnimator?.setDuration(300)
        markerAnimator?.addUpdateListener(ValueAnimator.AnimatorUpdateListener { animator ->
            iconLayer.setProperties(
                PropertyFactory.iconSize(animator.animatedValue as Float)
            )
        })
        markerAnimator?.start()
        markerSelected = false
    }
    private fun setUpMarkerLayer(loadedStyle: Style) {
        loadedStyle.addLayer(
            SymbolLayer(MARKER_LAYER_ID, SOURCE_ID)
                .withProperties(
                    PropertyFactory.iconImage(MARKER_IMAGE_ID),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.iconOffset(arrayOf(0f, -8f))
                )
        )
    }

    private val source: GeoJsonSource? = null

    private fun refreshSource() {
        if (source != null && featureCollection != null) {
            source.setGeoJson(featureCollection)
        }
    }
    // create symbol manager object


// add click listeners if desired


    override fun onMapClick(point: LatLng): Boolean {
        return handleClickIcon(mapboxMap.projection.toScreenLocation(point))
    }


    private fun setFeatureSelectState(feature: Feature, selectedState: Boolean) {
        if (feature.properties() != null) {
            feature.properties()!!.addProperty(PROPERTY_SELECTED, selectedState)
            refreshSource();
        }
    }

    /**
     * Checks whether a Feature's boolean "selected" property is true or false
     *
     * @param index the specific Feature's index position in the FeatureCollection's list of Features.
     * @return true if "selected" is true. False if the boolean property is false.
     */
    private fun featureSelectStatus(index: Int): Boolean {
        return if (featureCollection == null) {
            false
        } else featureCollection!!.features()!![index].getBooleanProperty(PROPERTY_SELECTED)
    }

    private fun handleClickIcon(screenPoint: PointF): Boolean {
        val features: List<Feature> = mapboxMap.queryRenderedFeatures(screenPoint, MARKER_LAYER_ID)
        return if (features.isNotEmpty()) {
            val name = features[0].getStringProperty(PROPERTY_NAME)
            val featureList = featureCollection!!.features()
            if (featureList != null) {
                for (i in featureList.indices) {
                    if (featureList[i].getStringProperty(PROPERTY_NAME) == name) {
                        if (featureSelectStatus(i)) {
                            setFeatureSelectState(featureList[i], false)
                        } else {
                            setSelected(i)
                        }
                    }
                }
            }
            true
        } else {
            false
        }
    }

    private fun setSelected(index: Int) {
        if (featureCollection!!.features() != null) {
            val feature = featureCollection!!.features()!![index]
            setFeatureSelectState(feature, true)
            refreshSource();
        }
    }


    private fun setUpInfoWindowLayer(loadedStyle: Style) {
        loadedStyle.addLayer(
            SymbolLayer(CALLOUT_LAYER_ID, SOURCE_ID)
                .withProperties( /* show image with id title based on the value of the name feature property */
                    PropertyFactory.iconImage("{name}"),  /* set anchor of icon to bottom-left */
                    PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),  /* all info window and marker image to appear at the same time*/
                    PropertyFactory.iconAllowOverlap(true),  /* offset the info window to be above the marker */
                    PropertyFactory.iconOffset(arrayOf(-2f, -28f))
                ) /* add a filter to show only when selected feature property is true */
                .withFilter(
                    Expression.eq(
                        Expression.get(PROPERTY_SELECTED),
                        Expression.literal(true)
                    )
                )
        )
    }

    private var featureCollection: FeatureCollection? = null

    private fun initFeatureCollection() {
        val featureList: MutableList<Feature> = ArrayList()

        for (user in otheruserLocations) {
            featureList.add(
                Feature.fromGeometry(
                    Point.fromLngLat(
                        user.location.getLatLong().latitude, user.location.getLatLong().longitude
                    )
                )
            )

            featureCollection = FeatureCollection.fromFeatures(featureList)
        }
    }
    lateinit var recyclerView : RecyclerView
    private fun initRecyclerView() {
        recyclerView = (mycontext as Activity).findViewById<RecyclerView>(R.id.rv_on_top_of_map)
        recyclerlist =  createRecyclerViewLocations()!!
        val locationAdapter = LocationRecyclerViewAdapter(
           recyclerlist,
            mapboxMap, mystyle
        )
        recyclerView.layoutManager = LinearLayoutManager(
            mycontext,
            LinearLayoutManager.HORIZONTAL, true
        )
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = locationAdapter
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
    }
    fun getBitmapFromVectorDrawable(context: Context?, drawableId: Int): Bitmap {
        var drawable = ContextCompat.getDrawable(requireContext(), drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable!!).mutate()
        }
        val bitmap: android.graphics.Bitmap = android.graphics.Bitmap.createBitmap(
            drawable?.intrinsicWidth!!,
            drawable?.intrinsicHeight!!, android.graphics.Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return  (bitmap)
    }
    private fun initMarkerIcons(loadedMapStyle: Style) {
        loadedMapStyle.addImage(
            SYMBOL_ICON_ID, BitmapFactory.decodeResource(
                this.resources, R.drawable.icon_info
            )
        )
        loadedMapStyle.addSource(GeoJsonSource(SOURCE_ID, featureCollection))

        var s = SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
            PropertyFactory.iconImage(SYMBOL_ICON_ID),
            PropertyFactory.iconAllowOverlap(true),
            PropertyFactory.iconOffset(arrayOf(0f, -4f))
        )
        loadedMapStyle.addLayer(
            s
        )
    }





    private fun createRecyclerViewLocations(): List<SingleRecyclerViewLocation>? {
        val locationList: ArrayList<SingleRecyclerViewLocation> = ArrayList()
        for (user in otheruserLocations) {
            val singleLocation = SingleRecyclerViewLocation()
            singleLocation.name =  user.City
            singleLocation.bedInfo = user.usertype_txt
            singleLocation.profilepic = user.profile_pic

            singleLocation.locationCoordinates =user.location.getLatLong()
            locationList.add(singleLocation)
        }
        return locationList
    }




    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(mycontext)) {

// Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(mycontext)
                .trackingGesturesManagement(true).pulseEnabled(true )
                .pulseColor(ContextCompat.getColor(mycontext, R.color.mapbox))
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(
                mycontext,
                loadedMapStyle
            )
                .locationComponentOptions(customLocationComponentOptions)
                .build()

// Get an instance of the LocationComponent and then adjust its settings
            mapboxMap.locationComponent.apply {

// Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

// Enable to make the LocationComponent visible
                isLocationComponentEnabled = true


// Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING

// Set the LocationComponent's render mode
                renderMode = RenderMode.NORMAL
                if(lastKnownLocation != null){

                    GlobalScope.launch {

                        //update user Location
                        val job0: Job = GlobalScope.launch(context = Dispatchers.IO) {
                            requireAuthManager().SessionID().take(1).collect {

                                val url = FuelManager.instance.basePath + RestPath.locationUpdate(it)

                                Log.d("URL", url)
                                val data = LocationData (   type = "Point", coordinates = listOf(  lastKnownLocation!!.latitude , lastKnownLocation!!.longitude ) as List<Double>)
                                Fuel.put(
                                    url,  listOf("location" to JsonDeserializer.encodeToString(data))
                                ).responseString { request, response, result ->

                                    when (result) {
                                        is Result.Success -> {
                                            Log.d("result", result.get())
                                            Toast.makeText(mycontext, "Success", Toast.LENGTH_LONG)
                                        }
                                        is Result.Failure -> {
                                            Log.d("result", result.getException().message.toString())
                                            Toast.makeText(mycontext, "Failure", Toast.LENGTH_LONG)
                                            otheruserLocations = mutableListOf()
                                        }

                                    }


                                }.join()
                                Log.d("COunt", otheruserLocations.size.toString())

                            }

                        }
                        job0.join()

                        // get other users
                        val job: Job = GlobalScope.launch(context = Dispatchers.IO) {
                            requireAuthManager().SessionID().take(1).collect {

                                val url = FuelManager.instance.basePath + RestPath.otherUsers(
                                    it,
                                    1.toString()
                                )

                                Log.d("URL", url)

                                Fuel.get(
                                    url
                                ).responseString { request, response, result ->

                                    when (result) {
                                        is Result.Success -> {
                                            Log.d("result", result.get())
                                            otheruserLocations =
                                                JsonDeserializer.decodeFromString<List<OtherUser>>(
                                                    result.get()
                                                )
                                        }
                                        is Result.Failure -> {
                                            Log.d("result", result.getException().message.toString())
                                            Toast.makeText(mycontext, "Failure", Toast.LENGTH_LONG)
                                            otheruserLocations = mutableListOf()
                                        }

                                    }


                                }.join()
                                Log.d("COunt", otheruserLocations.size.toString())

                            }

                        }
                        job.join()
                        initFeatureCollection();

                        Log.d("OtherUserCount", otheruserLocations.size.toString())
                        LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                        withContext(Dispatchers.Main) {
                            recyclerlist =  createRecyclerViewLocations()!!
                            val locationAdapter = LocationRecyclerViewAdapter(
                                recyclerlist,
                                mapboxMap, mystyle
                            )

                            recyclerView.adapter = locationAdapter
                            symbolManager.deleteAll()
                            createOtherUser(mystyle)

                            val bounds = LatLngBounds.Builder()

                            for (i in otheruserLocations)
                                bounds.include(i.location.getLatLong())
                            bounds.include( LatLng(
                                lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude
                            ))

                            val boundsbuild = bounds.build()
                            mapboxMap.setLatLngBoundsForCameraTarget(boundsbuild)


                            mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(boundsbuild, 400)
                                ,
                                1000
                            )
                        }
                    }

                    this@MapFragment.lastKnownLocation = lastKnownLocation!!
                    val position = CameraPosition.Builder()
                        .target(LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude))
                        .zoom(16.0)
                        .tilt(20.0)
                        .build()
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000)
                }
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(activity)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(mycontext, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap.style!!)
        } else {
            Toast.makeText(
                mycontext,
                R.string.user_location_permission_not_granted,
                Toast.LENGTH_LONG
            ).show()
            requireActivity().supportFragmentManager.beginTransaction().remove(this@MapFragment).commit()
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


    class SingleRecyclerViewLocation {


        var name: String? = null
        var bedInfo: String? = null
        var locationCoordinates: LatLng? = null
        var profilepic : String ? = null
    }

    internal class LocationRecyclerViewAdapter(
        private val locationList: List<SingleRecyclerViewLocation>,
        private val map: MapboxMap, private val style: Style
    ) :
        RecyclerView.Adapter<LocationRecyclerViewAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_on_top_of_map_card, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val singleRecyclerViewLocation = locationList[position]
            holder.name.text = singleRecyclerViewLocation.name
            holder.numOfBeds.text = singleRecyclerViewLocation.bedInfo
            Picasso.get().load(  singleRecyclerViewLocation.profilepic).into(holder.profilepic);
            holder.setClickListener(object : ItemClickListener {
                override fun onClick(view: View?, position: Int) {
                    val selectedLocationLatLng = locationList[position].locationCoordinates
                    val newCameraPosition = CameraPosition.Builder()
                        .target(selectedLocationLatLng).zoom(16.0)
                        .build()

                    map.easeCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))
                    val pixel: PointF = map.projection.toScreenLocation(selectedLocationLatLng!!);
                    val features: List<Feature> = map.queryRenderedFeatures(pixel)

                }
            })
        }
        private fun selectMarker(iconLayer: SymbolLayer) {
            val  markerAnimator = ValueAnimator()
            markerAnimator.setObjectValues(1f, 2f)
            markerAnimator.setDuration(300)
            markerAnimator.addUpdateListener(ValueAnimator.AnimatorUpdateListener { animator ->
                iconLayer.setProperties(
                    PropertyFactory.iconSize(animator.animatedValue as Float)
                )
            })
            markerAnimator.start()

        }
        override fun getItemCount(): Int {
            return locationList.size
        }

        internal class MyViewHolder(view: View) : RecyclerView.ViewHolder(view),
            View.OnClickListener {
            var name: TextView
            var numOfBeds: TextView
            var singleCard: CardView
            var profilepic : ImageView
            var clickListener: ItemClickListener? = null
            @JvmName("setClickListener1")
            fun setClickListener(itemClickListener: ItemClickListener) {
                clickListener = itemClickListener
            }

            override fun onClick(view: View?) {
                clickListener!!.onClick(view, layoutPosition)
            }

            init {
                name = view.findViewById(R.id.location_title_tv)
                numOfBeds = view.findViewById(R.id.location_num_of_beds_tv)
                singleCard = view.findViewById(R.id.single_location_cardview)
                profilepic = view.findViewById(R.id.profilepic_cardview)
                singleCard.setOnClickListener(this)
            }
        }
    }

    interface ItemClickListener {
        fun onClick(view: View?, position: Int)
    }





}