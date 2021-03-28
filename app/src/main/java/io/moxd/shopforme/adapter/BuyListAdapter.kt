package io.moxd.shopforme.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.android.material.snackbar.Snackbar
import io.moxd.shopforme.*
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.BuyListGSON
import io.moxd.shopforme.ui.shopbuylist.shopcart.Shopcart
import io.moxd.shopforme.ui.shopbuylist.shopcart.ShopcartViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class BuyListAdapter(private val context: Context, var itemModelArrayList: MutableList<BuyListGSON> , val viewModel: ShopcartViewModel) :
    RecyclerView.Adapter<BuyListAdapter.Viewholder>() {

    lateinit  var _parent:ViewGroup
    override fun onCreateViewHolder(  parent: ViewGroup, viewType: Int): Viewholder {
        // to inflate the layout for each item of recycler view.
        _parent = parent
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.buy_cardview, parent, false)
        return Viewholder(view)
    }

    var lastmodel : BuyListGSON? = null

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        // to set data to textview and imageview of each card layout
        val model: BuyListGSON = itemModelArrayList[position]
        holder.Title.paintFlags =
            holder.Title.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        holder.Title.text = FormatDate(model.creation_date)


        holder.cost.text = "Preis: ${
            String.format(
                "%.2f",
                model.articles.sumOf { (it.count * it.item.cost) })
        } €"
        holder.anzahl.text = "Anzahl: ${model.articles.sumBy { it.count }}"

        val stringlist = mutableListOf<String>()
        val transition: Transition = Fade()
        transition.duration = 600;
        transition.addTarget(R.id.buy_cardview_menu);

        TransitionManager.beginDelayedTransition(_parent, transition);

        for (i in model.articles) {
            stringlist.add("${i.item.name} x${i.count} ")
            Log.d("List$i", "${i.item.name} x${i.count} ")
        }

        holder.buylist.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        holder.buylist.adapter = BuyListMinAdapter(context, model.articles.toMutableList())

        holder.expand.setOnClickListener {
            if (holder.menu.visibility == VISIBLE) {
                holder.expand.setImageResource(R.drawable.ic_baseline_expand_more_24)

                holder.menu.visibility = INVISIBLE
                //       holder.menu.startAnimation(animate)
                holder.menu.visibility = GONE

            } else {

                holder.expand.setImageResource(R.drawable.ic_baseline_expand_less_24)
                val animate = AnimationUtils.loadAnimation(context, R.anim.slide_down);
                holder.menu.visibility = VISIBLE
                //    holder.menu.startAnimation(animate)

            }

        }


        holder.createbtn.setOnClickListener {
            viewModel.createShop(
                model.id.toString()
            )
        }


        holder.viewtop.setOnClickListener {
            if (holder.menu.visibility == VISIBLE) {
                holder.expand.setImageResource(R.drawable.ic_baseline_expand_more_24)

                holder.menu.visibility = INVISIBLE
                //       holder.menu.startAnimation(animate)
                holder.menu.visibility = GONE

            } else {

                holder.expand.setImageResource(R.drawable.ic_baseline_expand_less_24)
                val animate = AnimationUtils.loadAnimation(context, R.anim.slide_down);
                holder.menu.visibility = VISIBLE
                //    holder.menu.startAnimation(animate)

            }
        }


        holder.deltebtn.setOnClickListener {
            lastmodel = model
            viewModel.delteBuylist(model.id.toString())


        }

    }

    fun deleteSuccesses(){
    if(lastmodel != null){
        val pos =  itemModelArrayList.indexOf(lastmodel)
        itemModelArrayList.remove(lastmodel)
        this@BuyListAdapter.notifyItemRemoved(pos)
    }}

    override fun getItemCount(): Int {

        return itemModelArrayList.size
    }




    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val Title: TextView
        val cost: TextView
        val anzahl : TextView
        val expand : ImageView
        val menu :RelativeLayout
        val deltebtn : Button
        val createbtn : Button
        val buylist : RecyclerView
        val viewtop : RelativeLayout
      //  val more : ImageView
        init {
            Title = itemView.findViewById(R.id.buy_cardview_title)
            cost = itemView.findViewById(R.id.buy_cardview_Preis)
            anzahl = itemView.findViewById(R.id.buy_cardview_anzahl)
          expand = itemView.findViewById(R.id.buy_cardview_expand)
          menu = itemView.findViewById(R.id.buy_cardview_menu)
          buylist = itemView.findViewById(R.id.buy_cardview_menu_buylist)
          deltebtn = itemView.findViewById(R.id.buy_cardview_menu_delete)
          createbtn = itemView.findViewById(R.id.buy_cardview_menu_shop)
          viewtop = itemView.findViewById(R.id.buy_cardview_top)
       //     more = itemView.findViewById(R.id.buy_cardview_more)
        }
    }


}