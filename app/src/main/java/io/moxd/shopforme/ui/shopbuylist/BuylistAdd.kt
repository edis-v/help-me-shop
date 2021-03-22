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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import io.moxd.shopforme.*
import io.moxd.shopforme.adapter.ItemAdapter
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.ArticleAdd
import io.moxd.shopforme.data.model.Item
import io.moxd.shopforme.data.model.UserME
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
        ).responseString { _, response, result ->

            when (result) {


                is Result.Failure -> {
                    this@BuylistAdd.activity?.runOnUiThread() {
                        Log.d("Error", getError(response))
                        Toast.makeText(root.context,  getError(response), Toast.LENGTH_LONG).show()
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
                                createbtn.isEnabled = items.filter { it.anzahl.value!! > 0  }.isNotEmpty()
                            })
                    }
                }
            }
        }.join()

        createbtn.setOnClickListener {
            //create buylist
            MaterialAlertDialogBuilder(root.context)
                .setTitle("Fertig?")
                .setMessage("Willst du die Einkaufsliste erstellen?")
                .setNeutralButton("Nein") { _, _ ->
                    // Respond to neutral button press
                }.setNegativeButton("Abbrechen"){_, _ ->
                    val ft = (context as MainActivity).supportFragmentManager.beginTransaction()
                    ft.replace(R.id.mainframe, Shopcart())
                    ft.commit()
                }
                .setPositiveButton("Ja") { _, _ ->
                    // Respond to positive button press
                    //create an antrag with firebase or with a new api table
                    GlobalScope.launch(Dispatchers.IO) {
                        val listids : MutableList<Int> =  ArrayList<Int>(items.filter { it.anzahl.value!! > 0  }.size)
                        requireAuthManager().SessionID().take(1).collect {
                            //do actions

                            Log.d("articles" ,  items.filter { it2 -> it2.anzahl.value!! > 0 }.size.toString())
                            for (item in items.filter { it2 -> it2.anzahl.value!! > 0 }){
                                Fuel.post(
                                    RestPath.article, listOf("item" to item.id , "count" to item.anzahl.value)
                                ).responseString { _, response, result ->

                                    when (result) {


                                        is Result.Failure -> {
                                            this@BuylistAdd.activity?.runOnUiThread() {
                                                Log.d("Error",  getError(response))
                                                Toast.makeText(root.context,  getError(response), Toast.LENGTH_LONG)
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
                                            Log.d("Error", getError(response))
                                            Toast.makeText(root.context,  getError(response), Toast.LENGTH_LONG)
                                                .show()

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
                    val args = Bundle()
                    args.putCharSequence("page","B")
                    val f = Shopcart()
                    f.arguments = args
                    val ft = (context as MainActivity).supportFragmentManager.beginTransaction()
                    ft.replace(R.id.mainframe, f)
                    ft.commit()
                }
                .show()
            //maybe a dialog beforre


        }

        return root
    }
}


