package io.moxd.shopforme.ui.angebot

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.android.material.snackbar.Snackbar
import io.moxd.shopforme.JsonDeserializer
import io.moxd.shopforme.R
import io.moxd.shopforme.adapter.AngebotAdapter
import io.moxd.shopforme.adapter.BuyListAdapter
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.Angebot
import io.moxd.shopforme.data.model.BuyList
import io.moxd.shopforme.databinding.AngebotLayoutBinding
import io.moxd.shopforme.getError
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString

class AngebotFragment : Fragment() {





    val viewModel : AngebotViewModel by viewModels { AngebotViewModelFactory(this,arguments) }

    lateinit var binding: AngebotLayoutBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return   inflater.inflate(R.layout.angebot_layout,container,false)



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = AngebotLayoutBinding.bind(view)
        binding.apply {
            angebotList.layoutManager = LinearLayoutManager(
                root.context,
                LinearLayoutManager.VERTICAL,
                false
            )
            angebotRefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
                viewModel.getAngebote()
                angebotRefresh.setRefreshing(false)
            })

        }

        binding.apply {
            viewModel.Angebote.observe(viewLifecycleOwner){
                if (it.isSuccessful){

                  val angebote =  it.body()!!

                    angebotList.adapter = AngebotAdapter(requireContext(),angebote.toMutableList(), viewModel)

                }else
                {
                    //error
                }
            }
        }

        binding.apply {
            viewModel.Angebot.observe(viewLifecycleOwner){
                if (it.isSuccessful){


                    (angebotList.adapter as AngebotAdapter).deleteSuccesses()
                    Snackbar.make(view,  "Erfolgreich", Snackbar.LENGTH_LONG).show()
                }else
                {
                    //error
                    Snackbar.make(view,  "Failed", Snackbar.LENGTH_LONG).show()
                }
            }
        }

    }

}