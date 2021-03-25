package io.moxd.shopforme.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.result.Result
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import io.moxd.shopforme.*
import io.moxd.shopforme.data.AuthManager
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.UserME
import io.moxd.shopforme.ui.profile_list.ProfileListFragment
import io.moxd.shopforme.ui.splashscreen.SplashScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString


class ProfileFragment : Fragment()  {

    companion object {
        private val PERMISSION_CODE = 1001;

    }
    private lateinit var profilepic : ImageView
    private  lateinit var name : EditText
    private  lateinit var firstname : EditText
    private  lateinit var email : EditText
    private  lateinit var street : EditText
    private  lateinit var phonenumber : EditText
    private  lateinit var plz : EditText
    private  lateinit var city : EditText
    private  lateinit var usertype : Spinner
    private  lateinit var updatebtn : FloatingActionButton
    private  lateinit var savebtn : FloatingActionButton
    private  lateinit var cancel : FloatingActionButton
    private  lateinit var newpicbtn : FloatingActionButton
    var usertypes_txt = arrayListOf<String>("Helfer", "Hilfesuchender")
    var usertypes = arrayListOf<String>("HF", "HFS")

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.auth_profile_fragment, container, false)

        name = root.findViewById<EditText>(R.id.name_field)
        firstname = root.findViewById<EditText>(R.id.firstname_field)
        email = root.findViewById<EditText>(R.id.email_field)
        street = root.findViewById<EditText>(R.id.Street_field)
        phonenumber = root.findViewById<EditText>(R.id.phonenumber_field)
        plz = root.findViewById<EditText>(R.id.plz_field)
        city = root.findViewById<EditText>(R.id.City_field)
        usertype = root.findViewById<Spinner>(R.id.usertype_field)
        profilepic = root.findViewById<ImageView>(R.id.ProfilePic_field)
        updatebtn = root.findViewById<FloatingActionButton>(R.id.updateProfile_btn)
        savebtn = root.findViewById<FloatingActionButton>(R.id.saveProfile_btn)
        cancel = root.findViewById<FloatingActionButton>(R.id.cancelProfile_btn)
        newpicbtn = root.findViewById<FloatingActionButton>(R.id.uploadnewpic)

        root.findViewById<ImageButton>(R.id.profile_buttonBack).setOnClickListener{
            val ft = (requireActivity() as MainActivity).getSupportFragmentManager().beginTransaction()
            ft.replace(R.id.mainframe, ProfileListFragment())
            ft.commit()
        }


        val ad = ArrayAdapter<String>(root.context, R.layout.support_simple_spinner_dropdown_item, usertypes_txt)
        usertype.adapter = ad
        usertype.isClickable = false
        usertype.isEnabled = false
        newpicbtn.setOnClickListener {



            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(root.context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, PERMISSION_CODE)
                } else{
                    chooseImageGallery();
                }
            }else{
                chooseImageGallery();
            }




        }


        savebtn.setOnClickListener {

            val job2: Job = GlobalScope.launch(context = Dispatchers.IO) {
                requireAuthManager().SessionID().take(1).collect {
                    //do actions





                    val asyncupload = Fuel.upload(RestPath.userUpdate(it), Method.PUT, //"public" to "${name.text}" ,
                            listOf("name" to "${name.text}", "firstname" to "${firstname.text}", "phone_number" to "${phonenumber.text}", "Street" to "${street.text}", "plz" to "${plz.text}", "City" to "${city.text}", "usertype" to usertypes[usertype.selectedItemPosition])

                    ).responseString { request, response, result ->
                        when (result) {
                            is Result.Failure -> {
                                getUser()
                                Toast.makeText(root.context, getError(response), Toast.LENGTH_LONG).show()

                                Log.d("Update", getError(response))
                            }
                            is Result.Success -> {
                                if (AuthManager.User?.usertype_txt != usertypes_txt[usertype.selectedItemPosition])
                                    MaterialAlertDialogBuilder(requireContext())
                                            .setTitle("Usertype Geändert")
                                            .setMessage("App muss neustarten")

                                            .setPositiveButton("OK") { _, _ ->

                                                val intent = Intent(requireContext(), SplashScreen::class.java)
                                                requireActivity().finish()
                                                requireActivity().startActivity(intent)

                                            }.show()

                                Toast.makeText(root.context, "Update Sucess", Toast.LENGTH_LONG).show()
                            }
                        }
                    }


                    asyncupload.join()

                }
            }
            job2.start()




            firstname.isEnabled = false
            name.isEnabled = false
            street.isEnabled = false
            phonenumber.isEnabled = false
            email.isEnabled = false
            street.isEnabled = false
            usertype.isEnabled = false
            usertype.isClickable = false
            plz.isEnabled  =false
            city.isEnabled = false
            cancel.visibility = GONE
            savebtn.visibility = GONE
       //     newpicbtn.visibility = GONE
            updatebtn.visibility = VISIBLE

        }

        cancel.setOnClickListener{
            firstname.isEnabled = false
            name.isEnabled = false
            street.isEnabled = false
            phonenumber.isEnabled = false
            email.isEnabled = false
            street.isEnabled = false
            usertype.isEnabled = false
            plz.isEnabled  =false
            usertype.isClickable = false
            city.isEnabled = false
            cancel.visibility = GONE
            savebtn.visibility = GONE
       //     newpicbtn.visibility = GONE
            updatebtn.visibility = VISIBLE
        }

        updatebtn.setOnClickListener{
            firstname.isEnabled = true
            name.isEnabled = true
            street.isEnabled = true
            phonenumber.isEnabled = true
            email.isEnabled = true
            street.isEnabled = true
            usertype.isEnabled = true
            usertype.isClickable = true
            plz.isEnabled  =true
            city.isEnabled = true
            updatebtn.visibility = GONE
            cancel.visibility = VISIBLE
            savebtn.visibility = VISIBLE
      //      newpicbtn.visibility = VISIBLE

        }

        getUser()




        return  root;
    }

    fun getUser(){
        val job: Job = GlobalScope.launch(context = Dispatchers.IO) {
            requireAuthManager().SessionID().take(1).collect {
                //do actions

                Fuel.get(
                        RestPath.user(it)
                ).responseString { _, response, result ->

                    when (result) {


                        is Result.Failure -> {
                            this@ProfileFragment.activity?.runOnUiThread() {
                                Log.d("Error", getError(response))
                                Toast.makeText(requireContext(), getError(response), Toast.LENGTH_LONG).show()

                            }
                        }
                        is Result.Success -> {
                            val data = result.get()

                            Log.d("USerProfile", data)

                            this@ProfileFragment.activity?.runOnUiThread() {

                                val Profile = JsonDeserializer.decodeFromString<UserME>(data);
                                Picasso.get().load(Profile.profile_pic).into(profilepic)
                                //does actions on Ui-Thread u neeed it because Ui-elements can only be edited in Main/Ui-Thread

                                name.setText(Profile.name)
                                firstname.setText(Profile.firstname)
                                email.setText(Profile.email)
                                street.setText(Profile.Street)
                                phonenumber.setText(Profile.phone_number)
                                plz.setText(Profile.plz.toString())
                                city.setText(Profile.City)
                                if (Profile.usertype_txt == "Helfer")
                                    usertype.setSelection(0)
                                else
                                    usertype.setSelection(1)

                            }
                        }
                    }
                }.join()


            }

        }
        job.start()
    }
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImageGallery()
                } else {
                    Toast.makeText(this.context, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }




    var mGetContent = registerForActivityResult(StartActivityForResult()
    ) {
        Log.d("Picturerun", "Running")



        Log.d("Picturerun", "Running")
        if(  it.resultCode == Activity.RESULT_OK  ){

            val uri = it.data!!.data

            profilepic.setImageURI(uri)
            Log.d("Picturerun", getPath(uri)!!)



            val data = FileDataPart.from(getPath(uri)!!, name = "profile_pic")
            // from(data?.data?.encodedPath.toString() , name = "profile_pic")
            val job: Job = GlobalScope.launch(context = Dispatchers.IO) {
                requireAuthManager().SessionID().take(1).collect {
                    //do actions

                    val asyncupload = Fuel.upload(RestPath.userUpdate(it), Method.PUT)
                            .add(data)
                            .responseString { _, response, result ->
                                when (result) {
                                    is Result.Failure -> {
                                        Toast.makeText(this@ProfileFragment.context, getError(response), Toast.LENGTH_LONG).show()
                                        getUser()
                                    }
                                    is Result.Success -> {
                                        Toast.makeText(this@ProfileFragment.context, "Picture Update Sucess", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                    asyncupload.join();
                }}
            job.start()


        }}

    fun chooseImageGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Test Subject")
        val chooseIntent = Intent.createChooser(intent, "Select Picture")
        mGetContent.launch(chooseIntent)

    }

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




}