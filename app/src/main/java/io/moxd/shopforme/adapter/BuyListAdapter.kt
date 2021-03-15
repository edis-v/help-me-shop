package io.moxd.shopforme.adapter

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import io.moxd.shopforme.R
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.BuyList
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch


class BuyListAdapter(private val context: Context, val itemModelArrayList: List<BuyList>) :
    RecyclerView.Adapter<BuyListAdapter.Viewholder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        // to inflate the layout for each item of recycler view.
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.buy_cardview, parent, false)
        return Viewholder(view)
    }


    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        // to set data to textview and imageview of each card layout
        val model: BuyList = itemModelArrayList[position]
        holder.Title.text = model.creation_date
        holder.cost.text =   "Preis: ${ String.format(
            "%.2f",
            model.articles.sumOf { (it.count * it.item.cost) })} €"
        holder.anzahl.text = "Anzahl: ${ model.articles.sumBy {  it.count  } }"

        holder.more.setOnClickListener {
            val popup = PopupMenu(context, holder.more)
            popup.menuInflater.inflate(R.menu.buylist_menu, popup.menu)



            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener { item ->

                Log.d("BuyListAdapter" , item.title as String)
                    when(item.title){
                        "Löschen" ->{}
                        "Einkauf Erstellen" -> {
                            GlobalScope.launch {
                                requireAuthManager().SessionID().take(1).collect {
                                    Fuel.post(
                                        RestPath.shopadd, listOf("session_id" to it ,"buylist" to model.id )
                                    ).responseString { request, response, result ->

                                        when (result) {


                                            is Result.Failure -> {
                                                (context as Activity).runOnUiThread() {
                                                    Log.d(
                                                        "Error",
                                                        result.getException().message.toString()
                                                    )
                                                    Toast.makeText(
                                                        context,
                                                        "Creation Failed",
                                                        Toast.LENGTH_LONG
                                                    )
                                                        .show()


                                                    Log.d("Buylist", request.headers.toString())
                                                }
                                            }
                                            is Result.Success -> {
                                                val data = result.get()

                                                Log.d("Shop", data)

                                                (context as Activity).runOnUiThread() {


                                                }
                                            }
                                        }
                                    }.join()

                                }}
                        }
                    }
                true
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                popup.setForceShowIcon(true)
            }
            popup.show() //showing popup menu

        }




    }

    override fun getItemCount(): Int {

        return itemModelArrayList.size
    }




    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val Title: TextView
        val cost: TextView
        val anzahl : TextView
        val more : ImageView
        init {
            Title = itemView.findViewById(R.id.buy_cardview_title)
            cost = itemView.findViewById(R.id.buy_cardview_Preis)
            anzahl = itemView.findViewById(R.id.buy_cardview_anzahl)
            more = itemView.findViewById(R.id.buy_cardview_more)
        }
    }


}