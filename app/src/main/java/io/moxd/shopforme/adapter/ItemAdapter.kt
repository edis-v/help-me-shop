package io.moxd.shopforme.adapter

import android.content.Context
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import io.moxd.shopforme.MinMaxFilter
import io.moxd.shopforme.R
import io.moxd.shopforme.data.model.Item


class ItemAdapter(private val context: Context,   val itemModelArrayList: List<Item>) :
    RecyclerView.Adapter<ItemAdapter.Viewholder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        // to inflate the layout for each item of recycler view.
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.buy_item, parent, false)
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        // to set data to textview and imageview of each card layout
        val model: Item = itemModelArrayList[position]
        holder.Title.text = model.name
        holder.cost.text =  "${model.cost} €"
        holder.anzahl.setText(model.anzahl.value!!.toString())
        holder.subbtn.setOnClickListener {

            if(model.anzahl.value!! > 0)
            model.anzahl.value = model.anzahl.value!! - 1
            holder.anzahl.setText(model.anzahl.value!!.toString()) }
        holder.addbtn.setOnClickListener {
            if(model.anzahl.value!! < 5)
                model.anzahl.value = model.anzahl.value!! + 1
            holder.anzahl.setText(model.anzahl.value!!.toString())}
        holder.anzahl.filters = arrayOf<InputFilter>(MinMaxFilter("0", "5"))
    }

    override fun getItemCount(): Int {

        return itemModelArrayList.size
    }




    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val Title: TextView
        val cost: TextView
        val anzahl : TextInputEditText
        val addbtn : ImageButton
        val subbtn : ImageButton
        init {
            Title = itemView.findViewById(R.id.buy_item_title)
            cost = itemView.findViewById(R.id.buy_item_price)
            anzahl = itemView.findViewById(R.id.buy_item_select)
            addbtn = itemView.findViewById(R.id.buy_item_add)
            subbtn = itemView.findViewById(R.id.buy_item_subtract)
        }
    }


}