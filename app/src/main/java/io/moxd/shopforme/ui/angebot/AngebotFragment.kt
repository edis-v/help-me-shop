package io.moxd.shopforme.ui.angebot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import io.moxd.shopforme.R
import io.moxd.shopforme.adapter.AngebotAdapter
import io.moxd.shopforme.databinding.AngebotLayoutBinding

class AngebotFragment : Fragment() {


    val viewModel: AngebotViewModel by viewModels { AngebotViewModelFactory(this, arguments) }

    lateinit var binding: AngebotLayoutBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.angebot_layout, container, false)


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
            viewModel.Angebote.observe(viewLifecycleOwner) {
                if (it.isSuccessful) {

                    val angebote = it.body()!!

                    angebotList.adapter = AngebotAdapter(requireContext(), angebote.toMutableList(), viewModel)

                } else {
                    //error
                }
            }
        }

        binding.apply {
            viewModel.Angebot.observe(viewLifecycleOwner) {
                if (it.isSuccessful) {


                    (angebotList.adapter as AngebotAdapter).deleteSuccesses()
                    Snackbar.make(view, "Erfolgreich", Snackbar.LENGTH_LONG).show()
                } else {
                    //error
                    Snackbar.make(view, "Failed", Snackbar.LENGTH_LONG).show()
                }
            }
        }

    }

}