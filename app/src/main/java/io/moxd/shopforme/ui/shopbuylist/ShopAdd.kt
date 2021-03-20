package io.moxd.shopforme.ui.shopbuylist

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.result.Result
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import com.squareup.picasso.Picasso
import io.moxd.shopforme.*
import io.moxd.shopforme.adapter.BuyListMinAdapter
import io.moxd.shopforme.data.AuthManager
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.Shop
import io.moxd.shopforme.data.model.UserME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class ShopAdd : Fragment() {
    lateinit var title :TextView
    lateinit var price :TextView
    lateinit var count :TextView
    lateinit var bezahlt :TextView
    lateinit var status :ImageView
    lateinit var buylist : RecyclerView
    lateinit var exit : ImageView
    //options
    lateinit var delete : Button
    lateinit var report : Button
    lateinit var pay : Button
    lateinit var done : Button
    var model : Shop? = null


    companion object
    {
      val  PERMISSION_CODE = 1111
        var lastid = -1
    }
    fun getmodel(){
        val job: Job = GlobalScope.launch(context = Dispatchers.IO) {
            requireAuthManager().SessionID().take(1).collect {
                //do actions
                Log.d("Url: ", RestPath.getOneShop(it, lastid))
                Fuel.get(
                        RestPath.getOneShop(it, lastid)
                ).responseString { _, _, result ->

                    when (result) {


                        is Result.Failure -> {
                            this@ShopAdd.activity?.runOnUiThread() {

                                Log.d("Error", result.getException().message.toString())
                                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_LONG).show()
                            }
                        }
                        is Result.Success -> {
                            val data = result.get()

                            Log.d("USerProfile", data)

                            this@ShopAdd.activity?.runOnUiThread() {

                                  model = JsonDeserializer.decodeFromString<Shop>(data);

                                //does actions on Ui-Thread u neeed it because Ui-elements can only be edited in Main/Ui-Thread



                            }
                        }
                    }
                }.join()


            }

        }
        job.start()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
           sharedElementEnterTransition =   MaterialContainerTransform().apply {
            drawingViewId = R.id.mainframe
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            scrimColor = Color.TRANSPARENT

        }
    }
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.max_shop_cardview, container, false)

        title = root.findViewById(R.id.max_shop_cardview_title)
        title.paintFlags = title.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        try {
            model = arguments?.getSerializable("model") as Shop
            lastid = model!!.id
        }catch (ex: Exception){

        }
        getmodel()
        ViewCompat.setTransitionName(root, "max_shop${model!!.id}")
        price = root.findViewById(R.id.max_shop_cardview_Preis)
        count = root.findViewById(R.id.max_shop_cardview_anzahl)
        bezahlt = root.findViewById(R.id.max_shop_cardview_bezahlt)
        status = root.findViewById(R.id.max_shop_cardview_status)
        buylist = root.findViewById(R.id.max_shop_cardview_menu_buylist)
        exit =  root.findViewById<ImageView>(R.id.max_shop_cardview_exit)


        delete = root.findViewById(R.id.max_shop_cardview_menu_delete)
        done = root.findViewById(R.id.max_shop_cardview_menu_shop)
        pay = root.findViewById(R.id.max_shop_cardview_menu_payed)
        report = root.findViewById(R.id.max_shop_cardview_menu_report)

        delete.setOnClickListener { delete() }
        done.setOnClickListener { done() }
        pay.setOnClickListener { payed() }
        report.setOnClickListener { report() }


        buylist.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        buylist.adapter = BuyListMinAdapter(root.context, model!!.buylist.articles.toMutableList())
        title.text = FormatDate(model!!.creation_date)
        bezahlt.text = if(model!!.payed) "Bezahlt" else   "Zu Bezahlen"
         price.text =   "Preis: ${ String.format(
                 "%.2f",
                 model!!.buylist.articles.sumOf { (it.count * it.item.cost) })} €"
        count.text = "Anzahl: ${ model!!.buylist.articles.sumBy {  it.count  } }"
        if(model!!.helper == null)
        {
            //searcvhing
             status.setImageResource(R.drawable.ic_baseline_person_search_24)
             status.setColorFilter(ContextCompat.getColor(root.context, R.color.divivder), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        else if (model!!.done)
            when(model!!.payed){
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
             status.setImageResource(R.drawable.ic_baseline_hourglass_bottom_24)
           status.setColorFilter(ContextCompat.getColor(root.context, R.color.divivder), android.graphics.PorterDuff.Mode.SRC_IN);
        }


        delete.isEnabled = (model!!.helper == null) //optical not visible if enable or disable !!



       exit.setOnClickListener {
                exit()

        }
        return root
    }


    fun exit(){

        (context as MainActivity).supportFragmentManager.popBackStack()
    }

    fun delete(){

        GlobalScope.launch(Dispatchers.IO) {
            requireAuthManager().SessionID().take(1).collect {
                Fuel.delete(
                        RestPath.shopdelete(it, model!!.id)
                ).responseString { request, response, result ->

                    when (result) {


                        is Result.Failure -> {
                            (context as Activity).runOnUiThread() {
                                Log.d(
                                        "Error",
                                        result.getException().message.toString()
                                )
                                Toast.makeText(
                                        context,
                                        "Delete Failed",
                                        Toast.LENGTH_LONG
                                )
                                        .show()


                                Log.d("Buylist", request.headers.toString())
                            }
                        }
                        is Result.Success -> {
                            val data = result.get()

                            Log.d("Shop", data)

                            (context as Activity).runOnUiThread {
                                exit()
                            }

                        }
                    }
                }.join()

            }}

    }

    fun report(){
        //nice to have

    }

    fun done(){
        //create dialog from galleri  or take a picture or abort
        //than upload like profile upload
        MaterialAlertDialogBuilder(requireContext())
                .setTitle("Rechnung")
                .setMessage("Upload From?")
                .setNeutralButton("Cancel") { _, _ ->
                    // Respond to neutral button press
                }.setNegativeButton("Camera"){ _, _ ->
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Test Subject")
                    val chooseIntent = Intent.createChooser(intent, "Select Picture")
                    mGetContentDoneCamera.launch(chooseIntent)
                }
                .setPositiveButton("Gallery") { _, _ ->
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Test Subject")
                    val chooseIntent = Intent.createChooser(intent, "Select Picture")
                    mGetContentDoneGallery.launch(chooseIntent)

                }.show()

    }
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when(requestCode){
            ShopAdd.PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this.context, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null)
        return Uri.parse(path)
    }

    fun getRealPathFromURI(uri: Uri?): String? {
        var path = ""
        if (requireContext().getContentResolver() != null) {
            val cursor: Cursor? = requireContext().contentResolver.query(uri!!, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val idx = cursor.getColumnIndex(Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }
    var mGetContentPayedGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
    ) {
        Log.d("Picturerun", "Running")



        Log.d("Picturerun", "Running")
        if (it.resultCode == Activity.RESULT_OK) {

            val uri = it.data!!.data


            Log.d("Picturerun", getPath(uri)!!)

            val data = FileDataPart.from(getPath(uri)!!, name = "payed_prove")
            // from(data?.data?.encodedPath.toString() , name = "profile_pic")

            val job: Job = GlobalScope.launch(context = Dispatchers.IO) {
                requireAuthManager().SessionID().take(1).collect {
                    //do actions
                    val url =  if(AuthManager.User?.usertype_txt == "Helfer" )RestPath.shopaddpayHF(it, model!!.id) else RestPath.shopaddpayHFS(it, model!!.id)
                    Log.d("URL: ", url)
                    val asyncupload = Fuel.upload(url, Method.PUT)
                            .add(data)
                            .responseString { _, _, result ->
                                when (result) {
                                    is Result.Failure -> {
                                        Log.d("error", result.getException().message!!)
                                        Toast.makeText(this@ShopAdd.context, "Upload Failed", Toast.LENGTH_LONG).show()
                                    }
                                    is Result.Success -> {
                                        Toast.makeText(this@ShopAdd.context, "Upload Sucess", Toast.LENGTH_LONG).show()
                                        Log.d("Succsess", "Upload")
                                    }
                                }
                            }
                    asyncupload.join();
                }}
            job.start()
        }
    }
    var mGetContentPayedCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
    ) {
        Log.d("Picturerun", "Running")



        Log.d("Picturerun", "Running")
        if(  it.resultCode == Activity.RESULT_OK   ){
            val photo = it.data!!.getExtras()!!.get("data") as Bitmap

            // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
            val tempUri = getImageUri(this.requireActivity().applicationContext, photo)


                val data = FileDataPart.from((getRealPathFromURI(tempUri))!!, name = "payed_prove")


            // from(data?.data?.encodedPath.toString() , name = "profile_pic")

            val job: Job = GlobalScope.launch(context = Dispatchers.IO) {
                requireAuthManager().SessionID().take(1).collect {
                    //do actions
                    val url =    RestPath.shopaddpayHFS(it, model!!.id)
                    Log.d("URL: ", url)
                    val asyncupload = Fuel.upload(url, Method.PUT)
                            .add(data)
                            .responseString { _, _, result ->
                                when (result) {
                                    is Result.Failure -> {
                                        Log.d("error", result.getException().message!!)
                                        Toast.makeText(this@ShopAdd.context, "Upload Failed", Toast.LENGTH_LONG).show()
                                    }
                                    is Result.Success -> {
                                        Toast.makeText(this@ShopAdd.context, "Upload Sucess", Toast.LENGTH_LONG).show()
                                        Log.d("Succsess", "Upload")
                                    }
                                }
                            }
                    asyncupload.join();
                }}
            job.start()


        }}

    var mGetContentDoneGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
    ) {
        Log.d("Picturerun", "Running")



        Log.d("Picturerun", "Running")
        if (it.resultCode == Activity.RESULT_OK) {

            val uri = it.data!!.data


            Log.d("Picturerun", getPath(uri)!!)

            val data = FileDataPart.from(getPath(uri)!!, name = if (AuthManager.User?.usertype_txt == "Helfer") "bill_hf" else "bill_hfs")
            // from(data?.data?.encodedPath.toString() , name = "profile_pic")

            val job: Job = GlobalScope.launch(context = Dispatchers.IO) {
                requireAuthManager().SessionID().take(1).collect {
                    //do actions
                    val url =  RestPath.shopadddone(it, model!!.id, if (AuthManager.User?.usertype_txt == "Helfer") "hf" else "hfs")
                    Log.d("URL: ", url)
                    val asyncupload = Fuel.upload(url, Method.PUT, if (AuthManager.User?.usertype_txt == "Helfer") listOf() else listOf("done" to true))
                            .add(data)
                            .responseString { _, _, result ->
                                when (result) {
                                    is Result.Failure -> {
                                        Log.d("error", result.getException().message!!)
                                        Toast.makeText(this@ShopAdd.context, "Upload Failed", Toast.LENGTH_LONG).show()
                                    }
                                    is Result.Success -> {
                                        Toast.makeText(this@ShopAdd.context, "Upload Sucess", Toast.LENGTH_LONG).show()
                                        Log.d("Succsess", "Upload")
                                    }
                                }
                            }
                    asyncupload.join();
                }}
            job.start()
        }
    }
    var mGetContentDoneCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
    ) {
        Log.d("Picturerun", "Running")



        Log.d("Picturerun", "Running")
        if(  it.resultCode == Activity.RESULT_OK   ){
            val photo = it.data!!.getExtras()!!.get("data") as Bitmap

            // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
            val tempUri = getImageUri(this.requireActivity().applicationContext, photo)


            val data = FileDataPart.from((getRealPathFromURI(tempUri))!!, name = if (AuthManager.User?.usertype_txt == "Helfer") "bill_hf" else "bill_hfs")
            // from(data?.data?.encodedPath.toString() , name = "profile_pic")

            val job: Job = GlobalScope.launch(context = Dispatchers.IO) {
                requireAuthManager().SessionID().take(1).collect {
                    //do actions
                    val url =  RestPath.shopadddone(it, model!!.id, if (AuthManager.User?.usertype_txt == "Helfer") "hf" else "hfs")
                    Log.d("URL: ", url)
                    val asyncupload = Fuel.upload(url, Method.PUT, if (AuthManager.User?.usertype_txt == "Helfer") listOf() else listOf("done" to true))
                            .add(data)
                            .responseString { _, _, result ->
                                when (result) {
                                    is Result.Failure -> {
                                        Log.d("error", result.getException().message!!)
                                        Toast.makeText(this@ShopAdd.context, "Upload Failed", Toast.LENGTH_LONG).show()
                                    }
                                    is Result.Success -> {
                                        Toast.makeText(this@ShopAdd.context, "Upload Sucess", Toast.LENGTH_LONG).show()
                                        Log.d("Succsess", "Upload")
                                    }
                                }
                            }
                    asyncupload.join();
                }}
            job.start()


        }}

    fun getPath(uri: Uri?): String? {
        var cursor: Cursor = this.requireContext().contentResolver.query(uri!!, null, null, null, null)!!
        cursor.moveToFirst()
        var document_id: String = cursor.getString(0)
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1)
        cursor.close()
        cursor = this.requireContext().contentResolver!!.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", arrayOf(document_id), null)!!
        cursor.moveToFirst()
        val path: String = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        cursor.close()
        return path
    }



    fun ParseDate(Date : Date) : String{
        val sdf2 = SimpleDateFormat("yyyy-MM-dd HH:mm.ss", Locale.getDefault())
        val stwentyfourhour = sdf2.format(Date)
        Log.d("Parse Date" , stwentyfourhour.replace(" ","T").replace(".",":")+"Z")
        return stwentyfourhour.replace(" ","T").replace(".",":")+"Z"
    }


    fun payed(){
        //create dialog from galleri  or take a picture
        //than upload like profile upload
        if (AuthManager.User?.usertype_txt == "Helfer")
            MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Bezahlt worden")
                    .setMessage("Ja oder Noch nicht")
                    .setNeutralButton("Cancel") { _, _ ->
                        // Respond to neutral button press
                    }.setNegativeButton("Noch nicht"){ _, _ ->
                      //maybe send alert to user
                    }
                    .setPositiveButton("Ja") { _, _ ->
                        val job: Job = GlobalScope.launch(context = Dispatchers.IO) {
                            requireAuthManager().SessionID().take(1).collect {
                                //do actions
                                val url =  RestPath.shopaddpayHF(it, model!!.id)
                                Log.d("URL: ", url)


                                val asyncupload = Fuel.upload(url, Method.PUT, listOf("payed" to true, "finished_date" to ParseDate(Date())))
                                        .responseString { _, _, result ->
                                            when (result) {
                                                is Result.Failure -> {
                                                    Log.d("error", result.getException().message!!)
                                                    Toast.makeText(this@ShopAdd.context, "Update Failed", Toast.LENGTH_LONG).show()
                                                }
                                                is Result.Success -> {
                                                    Toast.makeText(this@ShopAdd.context, "Update Sucess", Toast.LENGTH_LONG).show()
                                                    Log.d("Succsess", "Update")
                                                }
                                            }
                                        }
                                asyncupload.join();
                            }}
                        job.start()

                    }.show()
            else
        MaterialAlertDialogBuilder(requireContext())
                .setTitle("Bezahlungsbeleg")
                .setMessage("Upload From?")
                .setNeutralButton("Cancel") { _, _ ->
                    // Respond to neutral button press
                }.setNegativeButton("Camera"){ _, _ ->
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Test Subject")
                    val chooseIntent = Intent.createChooser(intent, "Select Picture")
                    mGetContentPayedCamera.launch(chooseIntent)
                }
                .setPositiveButton("Gallery") { _, _ ->
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Test Subject")
                    val chooseIntent = Intent.createChooser(intent, "Select Picture")
                    mGetContentPayedGallery.launch(chooseIntent)

                }.show()
    }





}