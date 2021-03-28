package io.moxd.shopforme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.moxd.shopforme.R
import io.moxd.shopforme.data.AuthManager
import io.moxd.shopforme.data.model.Beleg

class BelegeAdapter(private val context: Context, val itemModelArrayList: List<Beleg>) :
        RecyclerView.Adapter<BelegeAdapter.Viewholder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        // to inflate the layout for each item of recycler view.
        val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.shopmax_cardview, parent, false)
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        // to set data to textview and imageview of each card layout
        val model: Beleg = itemModelArrayList[position]

        holder.User.text = model.user
        holder.type.text = if (model.type == "K") "KassenZettle" else "Bezahlung"
        Picasso.get().load(model.beleg).into(holder.beleg)

    }

    override fun getItemCount(): Int {

        return itemModelArrayList.size
    }


    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val type: TextView
        val User: TextView
        val beleg: ImageView

        init {
            type = itemView.findViewById(R.id.maxshop_type)
            User = itemView.findViewById(R.id.maxshop_user)
            beleg = itemView.findViewById(R.id.maxshop_beleg)

        }
    }


}