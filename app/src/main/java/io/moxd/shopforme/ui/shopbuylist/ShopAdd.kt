package io.moxd.shopforme.ui.shopbuylist

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.TransitionInflater
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import io.moxd.shopforme.FormatDate
import io.moxd.shopforme.MainActivity
import io.moxd.shopforme.R
import io.moxd.shopforme.adapter.BuyListMinAdapter
import io.moxd.shopforme.data.model.Shop


class ShopAdd : Fragment() {
    lateinit var title :TextView
    lateinit var price :TextView
    lateinit var count :TextView
    lateinit var bezahlt :TextView
    lateinit var status :ImageView
    lateinit var buylist : RecyclerView
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.max_shop_cardview, container, false)

        title = root.findViewById(R.id.max_shop_cardview_title)
        title.paintFlags = title.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        val model  = arguments?.getSerializable("model") as Shop
        ViewCompat.setTransitionName(root,"max_shop${model.id}")
        price = root.findViewById(R.id.max_shop_cardview_Preis)
        count = root.findViewById(R.id.max_shop_cardview_anzahl)
        bezahlt = root.findViewById(R.id.max_shop_cardview_bezahlt)
        status = root.findViewById(R.id.max_shop_cardview_status)
        buylist = root.findViewById(R.id.max_shop_cardview_menu_buylist)
        buylist.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        buylist.adapter = BuyListMinAdapter(root.context,model.buylist.articles.toMutableList())
        title.text = FormatDate( model.creation_date)
        bezahlt.text = if(model.payed) "Bezahlt" else   "Zu Bezahlen"
         price.text =   "Preis: ${ String.format(
                "%.2f",
                model.buylist.articles.sumOf { (it.count * it.item.cost) })} €"
        count.text = "Anzahl: ${ model.buylist.articles.sumBy {  it.count  } }"
        if(model.helper == null)
        {
            //searcvhing
             status.setImageResource(R.drawable.ic_baseline_person_search_24)
             status.setColorFilter(ContextCompat.getColor(root.context, R.color.divivder), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        else if (model.done)
            when(model.payed){
                true -> { // green arrow
                    status.setImageResource(R.drawable.ic_done)
                    status.setColorFilter(ContextCompat.getColor(root.context, R.color.green_200), android.graphics.PorterDuff.Mode.SRC_IN);
                }
                false -> {//red X
                     status.setImageResource(R.drawable.ic_wrong)
                    status.setColorFilter(ContextCompat.getColor(root.context, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }
        else {
            //timer
             status.setImageResource(R.drawable.ic_baseline_timelapse_24)
           status.setColorFilter(ContextCompat.getColor(root.context, R.color.divivder), android.graphics.PorterDuff.Mode.SRC_IN);
        }
     sharedElementEnterTransition =   MaterialContainerTransform().apply {
            drawingViewId = R.id.mainframe
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            scrimColor = Color.TRANSPARENT

        }


        exitTransition = Fade().apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }



        root.findViewById<ImageView>(R.id.max_shop_cardview_exit).setOnClickListener {
          (context as MainActivity).supportFragmentManager.popBackStack()

        }
        return root
    }
}