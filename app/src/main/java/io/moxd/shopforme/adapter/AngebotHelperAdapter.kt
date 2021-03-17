package io.moxd.shopforme.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.squareup.picasso.Picasso
import io.moxd.shopforme.FormatDate
import io.moxd.shopforme.R
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.Angebot
import io.moxd.shopforme.data.model.AngebotHelper
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class AngebotHelperAdapter (private val context: Context, var itemModelArrayList: MutableList<AngebotHelper>) :
        RecyclerView.Adapter<AngebotHelperAdapter.Viewholder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        // to inflate the layout for each item of recycler view.
        val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.angebot_helper_cardview, parent, false)
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        // to set data to textview and imageview of each card layout
        val model: AngebotHelper = itemModelArrayList[position]

        holder.date.text = FormatDate( model.shop.creation_date)
        holder.user.text =  model.shop.helpsearcher.firstname + " "+model.shop.helpsearcher.name
        Picasso.get().load(model.shop.helpsearcher.profile_pic).into(holder.profilepic);
        if(model.viewed)
            if(model.approve) {
                holder.status.setImageResource(R.drawable.ic_done)
                holder.status.setColorFilter(ContextCompat.getColor(context, R.color.green_200), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            else{
                holder.status.setImageResource(R.drawable.ic_wrong)
                holder.status.setColorFilter(ContextCompat.getColor(context, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        else
        {
            holder.status.setImageResource(R.drawable.ic_baseline_timelapse_24)
            holder.status.setColorFilter(ContextCompat.getColor(context, R.color.divivder), android.graphics.PorterDuff.Mode.SRC_IN);
        }





    }

    override fun getItemCount(): Int {

        return itemModelArrayList.size
    }




    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val user: TextView
        val date: TextView

        val profilepic : ImageView
        val status  : ImageView

        init {
            user = itemView.findViewById(R.id.angebot_helper_user)
            date = itemView.findViewById(R.id.angebot_helper_shop)
            profilepic = itemView.findViewById(R.id.angebot_helper_user_pic)
            status = itemView.findViewById(R.id.angebot_helper_status)

        }
    }


}
