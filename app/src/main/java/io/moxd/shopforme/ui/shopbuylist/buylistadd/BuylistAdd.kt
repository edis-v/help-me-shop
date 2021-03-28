package io.moxd.shopforme.ui.shopbuylist.buylistadd

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
import io.moxd.shopforme.*
import io.moxd.shopforme.adapter.ItemAdapter
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.ArticleAdd
import io.moxd.shopforme.data.model.Item
import io.moxd.shopforme.databinding.AddBuylistLayoutBinding
import io.moxd.shopforme.ui.shopbuylist.shopcart.Shopcart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*


class BuylistAdd : Fragment() {
    private lateinit var itemlist: RecyclerView
    private lateinit var priceinfo: TextView
    private lateinit var countinfo: TextView
    private lateinit var createbtn: Button
    private var items: List<Item> = mutableListOf()
    lateinit var binding: AddBuylistLayoutBinding

    val viewModel: BuyListAddViewModel by viewModels { BuyListViewModelFactory(this, arguments) }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_buylist_layout, container, false)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = AddBuylistLayoutBinding.bind(view)
        binding.apply {
            buyItemList.layoutManager = LinearLayoutManager(
                    root.context,
                    LinearLayoutManager.VERTICAL,
                    false
            )

            viewModel.Items.observe(viewLifecycleOwner) {
                if (it.isSuccessful) {
                    val items = it.body()!!
                    for (item in items) {
                        item.anzahl = MutableLiveData(0)
                    }
                    for (item in items) {
                        item.anzahl.observe(viewLifecycleOwner) {
                            //update ShopCart
                            buylistInfoPrice.text = "Preis: ${String.format("%.2f", viewModel.Items.value?.body()!!.sumOf { (it.anzahl.value!! * it.cost) })} €"
                            buylistInfoCount.text = "Anzahl: ${viewModel.Items.value?.body()!!.sumBy { it.anzahl.value!! }}"
                            buylistCreate.isEnabled = items.filter { it.anzahl.value!! > 0 }.isNotEmpty()
                        }
                    }
                    val ad = ItemAdapter(requireContext(), items)
                    buyItemList.adapter = ad
                } else {
                    //error
                }
            }
        }




        binding.apply {

            buylistCreate.setOnClickListener {
                //create buylist
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Fertig?")
                        .setMessage("Willst du die Einkaufsliste erstellen?")
                        .setNeutralButton("Nein") { _, _ ->
                            // Respond to neutral button press
                        }.setNegativeButton("Abbrechen") { _, _ ->
                            val ft = (context as MainActivity).supportFragmentManager.beginTransaction()
                            ft.replace(R.id.mainframe, Shopcart())
                            ft.commit()
                        }
                        .setPositiveButton("Ja") { _, _ ->
                            // Respond to positive button press
                            viewModel.createBuylist()

                        }
                        .show()
            }
        }





        viewModel.BuyList.observe(viewLifecycleOwner) {
            if (it.isSuccessful) {

                val args = Bundle()
                args.putCharSequence("page", "B")
                val f = Shopcart()
                f.arguments = args
                val ft = (context as MainActivity).supportFragmentManager.beginTransaction()
                ft.replace(R.id.mainframe, f)
                ft.commit()

            } else {
                Log.d("ErrorBuylist", getErrorRetro(it.errorBody()))
                //error
            }
        }

    }

}



