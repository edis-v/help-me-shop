package io.moxd.shopforme.adapter

import android.content.Context
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import io.moxd.shopforme.MinMaxFilter
import io.moxd.shopforme.R
import io.moxd.shopforme.data.model.Item
import io.moxd.shopforme.data.model.Shop

class ShopAdapter (private val context: Context, val itemModelArrayList: List<Shop>) :
        RecyclerView.Adapter<ShopAdapter.Viewholder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        // to inflate the layout for each item of recycler view.
        val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.shop_cardview, parent, false)
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        // to set data to textview and imageview of each card layout
        val model: Shop = itemModelArrayList[position]

        holder.Title.text = model.creation_date
        holder.payed.text = if(model.payed) "Bezahlt" else   "Zu Bezahlen"
        holder.price.text =   "Preis: ${ String.format(
                "%.2f",
                model.buylist.articles.sumOf { (it.count * it.item.cost) })} €"
        holder.count.text = "Anzahl: ${ model.buylist.articles.sumBy {  it.count  } }"
        if(model.helper == null)
        {
            //searcvhing
            holder.status.setImageResource(R.drawable.ic_baseline_person_search_24)
            holder.status.setColorFilter(ContextCompat.getColor(context, R.color.divivder), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        else if (model.done)
            when(model.payed){
                true ->{ // green arrow
                    holder.status.setImageResource(R.drawable.ic_done)
                    holder.status.setColorFilter(ContextCompat.getColor(context, R.color.green_200), android.graphics.PorterDuff.Mode.SRC_IN);
                }
                false -> {//red X
                    holder.status.setImageResource(R.drawable.ic_wrong)
                    holder.status.setColorFilter(ContextCompat.getColor(context, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }
        else {
            //timer
            holder.status.setImageResource(R.drawable.ic_baseline_timelapse_24)
            holder.status.setColorFilter(ContextCompat.getColor(context, R.color.divivder), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    override fun getItemCount(): Int {

        return itemModelArrayList.size
    }




    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val Title: TextView
        val price: TextView
        val count: TextView
        val payed: TextView
        val status : ImageView
        init {
            Title = itemView.findViewById(R.id.shop_cardview_title)
            price = itemView.findViewById(R.id.shop_cardview_Preis)
            count = itemView.findViewById(R.id.shop_cardview_anzahl)
            payed = itemView.findViewById(R.id.shop_cardview_bezahlt)
            status = itemView.findViewById(R.id.shop_cardview_status)
        }
    }


}

