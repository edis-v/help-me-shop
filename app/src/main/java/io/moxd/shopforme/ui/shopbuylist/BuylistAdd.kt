package io.moxd.shopforme.ui.shopbuylist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.squareup.picasso.Picasso
import io.moxd.shopforme.JsonDeserializer
import io.moxd.shopforme.R
import io.moxd.shopforme.adapter.ItemAdapter
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.ArticleAdd
import io.moxd.shopforme.data.model.Item
import io.moxd.shopforme.data.model.UserME
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*


class BuylistAdd: Fragment() {
    private lateinit var itemlist: RecyclerView
    private  lateinit var priceinfo : TextView
    private  lateinit var countinfo : TextView
    private lateinit var createbtn : Button
    private   var items :List<Item> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.add_buylist_layout, container, false)

        //get items


        itemlist = root.findViewById(R.id.buy_item_list)
        priceinfo = root.findViewById(R.id.buylist_info_price)
        countinfo = root.findViewById(R.id.buylist_info_count)
        createbtn = root.findViewById(R.id.buylist_create)
       itemlist.layoutManager = LinearLayoutManager(
           root.context,
           LinearLayoutManager.VERTICAL,
           false
       )
        Fuel.get(
            RestPath.items
        ).responseString { _, _, result ->

            when (result) {


                is Result.Failure -> {
                    this@BuylistAdd.activity?.runOnUiThread() {
                        Log.d("Error", result.getException().message.toString())
                        Toast.makeText(root.context, "ItemList Failed", Toast.LENGTH_LONG).show()
                    }
                }
                is Result.Success -> {
                    val data = result.get()

                    Log.d("USerProfile", data)

                    this@BuylistAdd.activity?.runOnUiThread() {

                        items = Json.decodeFromString<List<Item>>(data);


                        val ad = ItemAdapter(root.context, items.toList())
                        itemlist.adapter =  ad
                        for(i in items)
                            i.anzahl.observe(requireActivity(), {
                                priceinfo.text =  "Preis: ${ String.format("%.2f" ,items.sumOf { (it.anzahl.value!! * it.cost) })} €"
                                countinfo.text = "Anzahl: ${ items.sumBy {  it.anzahl.value!!  } }"
                            })
                    }
                }
            }
        }.join()

        createbtn.setOnClickListener {
            //create buylist

            //maybe a dialog beforre
            if(items.filter { it.anzahl.value!! > 0  }.isNotEmpty())
            GlobalScope.launch(Dispatchers.IO) {
                val listids : MutableList<Int> =  ArrayList<Int>(items.filter { it.anzahl.value!! > 0  }.size)
                requireAuthManager().SessionID().take(1).collect {
                    //do actions

                    Log.d("articles" ,  items.filter { it2 -> it2.anzahl.value!! > 0 }.size.toString())
                    for (item in items.filter { it2 -> it2.anzahl.value!! > 0 }){
                        Fuel.post(
                            RestPath.article, listOf("item" to item.id , "count" to item.anzahl.value)
                        ).responseString { _, _, result ->

                            when (result) {


                                is Result.Failure -> {
                                    this@BuylistAdd.activity?.runOnUiThread() {
                                        Log.d("Error", result.getException().message.toString())
                                        Toast.makeText(root.context, "Creation FAiled", Toast.LENGTH_LONG)
                                            .show()
                                    }
                                }
                                is Result.Success -> {
                                    val data = result.get()
                                    Log.d("Succsess", data)
                                    val id = JsonDeserializer.decodeFromString<ArticleAdd>(data).id

                                    listids.add(id)
                                    Log.d("Added ",  id.toString())
                                    this@BuylistAdd.activity?.runOnUiThread() {


                                    }
                                }
                            }
                        }.join()

                    }

                    Log.d("articles2" ,  listids.size.toString())
                    while (items.filter { it2 -> it2.anzahl.value!! > 0 }.size != listids.size){}
                    val body = "{\n" +
                            "    \"articles\": [${listids.joinToString()}],\n" +
                            "    \"session_id\": \"$it\"\n" +
                            "}"

                    Log.d("Body" , body)

                    Fuel.post(
                        RestPath.buylistadd
                    ).header("Content-Type" to "application/json").body(body).responseString { request, response, result ->

                        when (result) {


                            is Result.Failure -> {
                                this@BuylistAdd.activity?.runOnUiThread() {
                                    Log.d("Error", result.getException().message.toString())
                                    Toast.makeText(root.context, "Creation Failed", Toast.LENGTH_LONG)
                                        .show()


                                    Log.d("Buylist", request.headers.toString())
                                }
                            }
                            is Result.Success -> {
                                val data = result.get()

                                Log.d("Buylist", data)

                                this@BuylistAdd.activity?.runOnUiThread() {


                                }
                            }
                        }
                    }.join()

                }
            }

        }

        return root
    }
}


