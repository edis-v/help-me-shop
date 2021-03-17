package io.moxd.shopforme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.moxd.shopforme.R
import io.moxd.shopforme.data.model.Article
import io.moxd.shopforme.data.model.BuyList

class BuyListMinAdapter(private val context: Context, var itemModelArrayList: MutableList<Article>) :
        RecyclerView.Adapter<BuyListMinAdapter.Viewholder>() {

    lateinit var _parent: ViewGroup
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyListMinAdapter.Viewholder {
        // to inflate the layout for each item of recycler view.
        _parent = parent
        val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.article_buylist_cardview, parent, false)
        return Viewholder(view)
    }
    override fun onBindViewHolder(holder:  Viewholder, position: Int) {
        // to set data to textview and imageview of each card layout
        val model: Article = itemModelArrayList[position]
        holder.data.text =  ("${model.item.name} x${model.count} ")
    }

    override fun getItemCount(): Int {

        return itemModelArrayList.size
    }
    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val data: TextView

        //  val more : ImageView
        init {
            data = itemView.findViewById(R.id.article_buylist_data)

            //     more = itemView.findViewById(R.id.buy_cardview_more)
        }
    }

}
