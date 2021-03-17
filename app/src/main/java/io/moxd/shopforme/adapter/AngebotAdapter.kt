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
import io.moxd.shopforme.JsonDeserializer
import io.moxd.shopforme.R
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.Angebot
import io.moxd.shopforme.data.model.Shop
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

class AngebotAdapter  (private val context: Context, var itemModelArrayList: MutableList<Angebot>) :
        RecyclerView.Adapter<AngebotAdapter.Viewholder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        // to inflate the layout for each item of recycler view.
        val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.angebot_helpsearcher_cardview, parent, false)
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        // to set data to textview and imageview of each card layout
        val model: Angebot = itemModelArrayList[position]

        holder.date.text = FormatDate( model.shop.creation_date)
        holder.user.text = model.helper.firstname + " "+model.helper.name
        Picasso.get().load(model.helper.profile_pic).into(holder.profilepic);

        holder.wrong.setOnClickListener {
            updateAngebot(model.id,false)
            val pos =  itemModelArrayList.indexOf(model)
            itemModelArrayList.remove(model)
            this.notifyItemRemoved(pos)

        }

        holder.right.setOnClickListener {
            updateAngebot(model.id,true)
            val pos =  itemModelArrayList.indexOf(model)
            itemModelArrayList.remove(model)
            this.notifyItemRemoved(pos)
        }

    }

    override fun getItemCount(): Int {

        return itemModelArrayList.size
    }


    fun updateAngebot(id:Int,approve: Boolean){
        GlobalScope.launch(context = Dispatchers.IO) {
            requireAuthManager().SessionID().take(1).collect {
                Fuel.put(
                        RestPath.angebotapprove(it, id), listOf("viewed" to true, "approve" to approve)
                ).responseString { request, response, result ->

                    when (result) {
                        is Result.Success -> {
                            Log.d("result", result.get())
                            Toast.makeText(context, "Success", Toast.LENGTH_LONG)
                        }
                        is Result.Failure -> {
                            Log.d(
                                    "result",
                                    result.getException().message.toString()
                            )
                            Toast.makeText(context, "Failure", Toast.LENGTH_LONG)

                        }

                    }


                }.join()
            }}
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
