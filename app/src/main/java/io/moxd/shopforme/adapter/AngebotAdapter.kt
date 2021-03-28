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
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import io.moxd.shopforme.*
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.Angebot
import io.moxd.shopforme.data.model.AngebotGSON
import io.moxd.shopforme.data.model.AngebotHelper
import io.moxd.shopforme.data.model.Shop
import io.moxd.shopforme.ui.angebot.AngebotViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

class AngebotAdapter  (private val context: Context, var itemModelArrayList: MutableList<AngebotGSON>, val viewModel: AngebotViewModel) :
        RecyclerView.Adapter<AngebotAdapter.Viewholder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        // to inflate the layout for each item of recycler view.
        val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.angebot_helpsearcher_cardview, parent, false)
        return Viewholder(view)
    }

    var lastmodel : AngebotGSON? = null

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        // to set data to textview and imageview of each card layout
        val model: AngebotGSON = itemModelArrayList[position]

        holder.date.text = FormatDate( model.creation_date)
        holder.user.text = model.helper.firstname + " "+model.helper.name
        Log.d("data",model.helper.profile_pic )
        Picasso.get().load(model.helper.profile_pic).into(holder.profilepic);

        holder.wrong.setOnClickListener {
            lastmodel = model
            viewModel.replyAngebot(model.id.toString(),false)



        }

        holder.right.setOnClickListener {
            lastmodel = model
            viewModel.replyAngebot(model.id.toString(),true)


        }

    }
    fun deleteSuccesses(){
        if(lastmodel != null){
            val pos =  itemModelArrayList.indexOf(lastmodel)
            itemModelArrayList.remove(lastmodel)
            this.notifyItemRemoved(pos)
        }}

    override fun getItemCount(): Int {

        return itemModelArrayList.size
    }




    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val user: TextView
        val date: TextView

        val profilepic : ImageView
        val wrong : ImageView
        val right : ImageView
        init {
            user = itemView.findViewById(R.id.angebot_helpsearcher_user)
            date = itemView.findViewById(R.id.angebot_helpsearcher_shop)
            profilepic = itemView.findViewById(R.id.angebot_helpsearcher_user_pic)
            wrong = itemView.findViewById(R.id.angebot_helpsearcher_wrong)
            right = itemView.findViewById(R.id.angebot_helpsearcher_right)
        }
    }


}
