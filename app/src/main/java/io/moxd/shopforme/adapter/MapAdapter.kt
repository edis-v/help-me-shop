package io.moxd.shopforme.adapter

import android.app.Activity
import android.content.Context
import android.graphics.PointF
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.squareup.picasso.Picasso
import io.moxd.shopforme.FormatDate
import io.moxd.shopforme.R

import io.moxd.shopforme.data.model.ShopMap

import io.moxd.shopforme.ui.map.MapViewModel


class MapAdapter(private val context: Context, val itemModelArrayList: List<ShopMap>, private val map: MapboxMap, private val viewmodel: MapViewModel) :
        RecyclerView.Adapter<MapAdapter.Viewholder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapAdapter.Viewholder {
        // to inflate the layout for each item of recycler view.
        val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.rv_on_top_of_map_card, parent, false)
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: MapAdapter.Viewholder, position: Int) {
        val model = itemModelArrayList[position]
        holder.name.text = model.helpsearcher.firstname + " " + model.helpsearcher.name
        holder.price.text = "Preis: ${
            String.format(
                    "%.2f",
                    model.buylist.articles.sumOf { (it.count * it.item.cost) })
        } €"
        holder.count.text = "Anzahl: ${model.buylist.articles.sumBy { it.count }}"
        holder.createdate.text = FormatDate(model.creation_date)
        Picasso.get().load(model.helpsearcher.profile_pic).into(holder.profilepic);

        holder.btn.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                    .setTitle("Hilfe anbieten")
                    .setMessage("Willst du ${holder.name.text} helfen?")
                    .setNeutralButton("Abbrechen") { dialog, which ->
                        // Respond to neutral button press
                    }
                    .setPositiveButton("Ja") { dialog, which ->
                        // Respond to positive button press
                        //create an antrag with firebase or with a new api table
                        viewmodel.createAngebot(model.id)
                        //view model create
                    }
                    .show()
        }

        holder.itemView.setOnClickListener {

            val selectedLocationLatLng = model.helpsearcher.location.getLatLong()
            val newCameraPosition = CameraPosition.Builder()
                    .target(selectedLocationLatLng).zoom(16.0)
                    .build()

            map.easeCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))
            val pixel: PointF = map.projection.toScreenLocation(selectedLocationLatLng!!);
            val features: List<Feature> = map.queryRenderedFeatures(pixel)


        }

    }

    override fun getItemCount(): Int {

        return itemModelArrayList.size
    }

    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView
        var count: TextView
        var createdate: TextView
        var price: TextView
        var singleCard: CardView
        var profilepic: ImageView
        var btn: Button

        init {
            name = itemView.findViewById(R.id.username_cardview)
            count = itemView.findViewById(R.id.anzahl_cardview)
            singleCard = itemView.findViewById(R.id.single_location_cardview)
            profilepic = itemView.findViewById(R.id.profilepic_cardview)
            price = itemView.findViewById(R.id.price_cardview)
            createdate = itemView.findViewById(R.id.create_cardview)
            btn = itemView.findViewById(R.id.helpbtn_cardview)


        }
    }
}