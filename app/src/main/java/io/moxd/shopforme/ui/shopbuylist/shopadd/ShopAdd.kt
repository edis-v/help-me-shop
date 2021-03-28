package io.moxd.shopforme.ui.shopbuylist.shopadd

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.result.Result
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import io.moxd.shopforme.*
import io.moxd.shopforme.adapter.BelegeAdapter
import io.moxd.shopforme.adapter.BuyListMinAdapter
import io.moxd.shopforme.data.AuthManager
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.Beleg
import io.moxd.shopforme.data.model.ShopGSON
import io.moxd.shopforme.databinding.AddShopLayoutBinding
import io.moxd.shopforme.databinding.MaxShopCardviewBinding
import io.moxd.shopforme.ui.dialog.CameraGalleryDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class ShopAdd : Fragment() {

    lateinit var observer: ShopAddLifecycleObserver
    lateinit var binding: MaxShopCardviewBinding

    val viewModel: ShopAddViewModel by viewModels {
        ShopAddViewModelFactory(this, arguments)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        binding = MaxShopCardviewBinding.bind(view)

        binding.apply {

            maxShopCardviewTitle.paintFlags =
                    maxShopCardviewTitle.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            ViewCompat.setTransitionName(root, "max_shop${viewModel.modelid}")
            maxShopCardviewMenuDelete.setOnClickListener {
                // delete()
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Shop Löschen")
                        .setMessage("Sind sie sich sicher ?")
                        .setNeutralButton("Cancel") { _, _ ->
                            // Respond to neutral button press
                        }
                        .setPositiveButton("Ja") { _, _ ->
                            viewModel.deleteShop()

                        }.show()
            }
            maxShopCardviewMenuShop.setOnClickListener {
                val popup = CameraGalleryDialog(requireActivity())



                popup.show()
                popup.gallery.setOnClickListener {
                    observer.GalleryAction(if (viewModel.UserType() == "Helfer") ActionType.DoneHF else ActionType.DoneHFS)
                    popup.dismiss()
                }

                popup.camera.setOnClickListener {
                    observer.CameraAction(if (viewModel.UserType() == "Helfer") ActionType.DoneHF else ActionType.DoneHFS)
                    popup.dismiss()
                }
            }
            maxShopCardviewMenuPayed.setOnClickListener {
                //payed()
                if (viewModel.UserType() == "Helfer") {

                    MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Bezahlt worden")
                            .setMessage("Ja oder Noch nicht")
                            .setNeutralButton("Cancel") { _, _ ->
                                // Respond to neutral button press
                            }.setNegativeButton("Noch nicht") { _, _ ->
                                //maybe send alert to user
                            }
                            .setPositiveButton("Ja") { _, _ ->

                                viewModel.shopPayHF()
                            }.show()


                } else {

                    val popup = CameraGalleryDialog(requireActivity())



                    popup.show()
                    popup.gallery.setOnClickListener {
                        observer.GalleryAction(ActionType.PayHFS)
                        popup.dismiss()
                    }

                    popup.camera.setOnClickListener {
                        observer.CameraAction(ActionType.PayHFS)
                        popup.dismiss()
                    }

                }

            }
            maxShopCardviewMenuReport.setOnClickListener {
                //report()
            }

            maxShopCardviewExit.setOnClickListener {
                (context as MainActivity).supportFragmentManager.popBackStack()

            }

        }

        binding.apply {

            viewModel.ShopDelte.observe(viewLifecycleOwner) {
                if (it.isSuccessful)
                    (context as MainActivity).supportFragmentManager.popBackStack()
                else
                    Log.d("Error", getErrorRetro(it.errorBody()))
            }

            viewModel.Shop.observe(viewLifecycleOwner) {
                if (it.isSuccessful) {

                    val model = it.body()!!



                    maxShopCardviewMenuDelete.visibility = if (model.helper == null) View.VISIBLE else View.GONE
                    maxShopCardviewMenuReport.visibility = if (model.helper == null) View.GONE else View.VISIBLE
                    maxShopCardviewMenuShop.visibility = if (viewModel.UserType() == "Helfer" || model.bill_hf != null) View.VISIBLE else View.GONE
                    maxShopCardviewMenuPayed.visibility = if (viewModel.UserType() == "Helfer" || model.payed_prove == null) View.GONE else View.VISIBLE

                    val belege: MutableList<Beleg> = mutableListOf()

                    if (!model.bill_hf.isNullOrEmpty())
                        belege.add(Beleg("K", if (viewModel.UserType() == "Helfer") "Ich" else "${model.helper?.firstname} ${model.helper?.name}", model.bill_hf))
                    if (!model.bill_hfs.isNullOrEmpty())
                        belege.add(Beleg("K", if (viewModel.UserType() == "Helfer") "${model.helpsearcher.firstname} ${model.helpsearcher.name}" else "Ich", model.bill_hfs))
                    if (!model.payed_prove.isNullOrEmpty())
                        belege.add(Beleg("P", if (viewModel.UserType() == "Helfer") "${model.helpsearcher.firstname} ${model.helpsearcher.name}" else "Ich", model.payed_prove))
                    maxShopImagerecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    maxShopImagerecycler.adapter = BelegeAdapter(requireContext(), belege)



                    maxShopCardviewTitle.text = FormatDate(model.creation_date)
                    maxShopCardviewBezahlt.text = if (model.payed) "Bezahlt" else "Zu Bezahlen"
                    if (viewModel.UserType() == "Helfer") {
                        maxShopCardviewMenuPayed.text = "Bezahlung erhalten"
                        maxShopCardviewMenuShop.text = "Einkauf erledigt"
                        maxShopCardviewMenuPayed.isEnabled = (model?.done!! && model?.payed_prove != null)
                        maxShopCardviewMenuShop.isEnabled = model?.helper != null
                    } else {
                        maxShopCardviewMenuPayed.text = "Bezahlung belegen"
                        maxShopCardviewMenuShop.text = "Einkauf erhalten"
                        maxShopCardviewMenuPayed.isEnabled = (model?.done!!)
                        maxShopCardviewMenuShop.isEnabled = (model?.helper != null && model?.bill_hf != null)
                    }

                    if (model.price != 0.0) {
                        maxShopCardviewRealPreis.visibility = View.VISIBLE
                        maxShopCardviewRealPreis.text = "Der Preis des Einkaufs \nbeträgt ${model.price} €"
                    } else {
                        maxShopCardviewRealPreis.visibility = View.GONE
                    }

                    maxShopCardviewPhonenumber.text = if (viewModel.UserType() == "Helfer") model!!.helpsearcher.phone_number else if (model!!.helper != null) model!!.helper?.phone_number else "Kein Helfer"
                    maxShopCardviewBezahlt.text = if (model!!.payed) "Bezahlt" else "Zu Bezahlen"
                    maxShopCardviewPreis.text = "Geschätzter Preis: ${
                        String.format(
                                "%.2f",
                                model!!.buylist.articles.sumOf { (it.count * it.item.cost) })
                    } €"
                    maxShopCardviewAnzahl.text = "Anzahl: ${model.buylist.articles.sumBy { it.count }}"
                    if (model.helper == null) {
                        //searcvhing
                        maxShopCardviewStatus.setImageResource(R.drawable.ic_baseline_person_search_24)
                        maxShopCardviewStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.divivder), android.graphics.PorterDuff.Mode.SRC_IN);
                    } else if (model.done)
                        when (model.payed) {
                            true -> { // green arrow
                                maxShopCardviewStatus.setImageResource(R.drawable.ic_done)
                                maxShopCardviewStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.IconAccept), android.graphics.PorterDuff.Mode.SRC_IN);
                            }
                            false -> {//red X
                                maxShopCardviewStatus.setImageResource(R.drawable.ic_wrong)
                                maxShopCardviewStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
                            }
                        }
                    else {
                        //timer
                        maxShopCardviewStatus.setImageResource(R.drawable.ic_baseline_hourglass_bottom_24)
                        maxShopCardviewStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.divivder), android.graphics.PorterDuff.Mode.SRC_IN);
                    }

                    maxShopCardviewMenuBuylist.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

                    maxShopCardviewMenuBuylist.adapter = BuyListMinAdapter(requireContext(), model.buylist.articles.toMutableList())


                } else {
                    // Server Failed
                }
            }
        }


    }

    override fun onResume() {
        super.onResume()

    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.mainframe
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            scrimColor = Color.TRANSPARENT

        }
        observer = ShopAddLifecycleObserver(requireActivity().activityResultRegistry, viewModel, requireContext())
        lifecycle.addObserver(observer)
        return inflater.inflate(R.layout.max_shop_cardview, container, false)

    }

}

