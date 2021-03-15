package io.moxd.shopforme.ui.angebot

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import io.moxd.shopforme.JsonDeserializer
import io.moxd.shopforme.R
import io.moxd.shopforme.adapter.AngebotAdapter
import io.moxd.shopforme.adapter.BuyListAdapter
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.Angebot
import io.moxd.shopforme.data.model.BuyList
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString

class AngebotFragment : Fragment() {


    lateinit var refreshLayout: SwipeRefreshLayout
    lateinit var recyclerView: RecyclerView
    var angebot : List<Angebot> = mutableListOf()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.angebot_layout,container,false)

        refreshLayout = root.findViewById(R.id.angebot_Refresh)
        recyclerView = root.findViewById(R.id.angebot_list)
        recyclerView.layoutManager = LinearLayoutManager(
                root.context,
                LinearLayoutManager.VERTICAL,
                false
        )
        getAngebot()
        refreshLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            getAngebot()
            refreshLayout.setRefreshing(false)
        })

        return root
    }

    fun getAngebot(){
        GlobalScope.launch(context = Dispatchers.IO) {
            requireAuthManager().SessionID().take(1).collect {
                //do actions

                Fuel.get(
                        RestPath.angebot(it)
                ).responseString { _, _, result ->

                    when (result) {


                        is Result.Failure -> {
                            this@AngebotFragment.activity?.runOnUiThread() {
                                Log.d(
                                        "Error",
                                        result.getException().message.toString()
                                )
                                Toast.makeText(
                                        this@AngebotFragment.requireContext(),
                                        "Login Failed",
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        is Result.Success -> {
                            val data = result.get()

                            Log.d("USerProfile", data)

                            this@AngebotFragment.activity?.runOnUiThread() {

                                angebot =
                                        JsonDeserializer.decodeFromString<List<Angebot>>(
                                                data
                                        );
                                recyclerView.adapter =
                                        AngebotAdapter(this@AngebotFragment.requireContext(), angebot.toMutableList())

                            }
                        }
                    }
                }.join()


            }
        }
    }
}