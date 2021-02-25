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
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.result.Result
import com.squareup.picasso.Picasso
import io.moxd.shopforme.R
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.UserME
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class ProfileFragment : Fragment()  {

    companion object {
        private val PERMISSION_CODE = 1001;

    }
    private lateinit var profilepic : ImageView

    var usertypes_txt = arrayListOf<String>("Helfer", "Hilfesuchender")
    var usertypes = arrayListOf<String>("HF", "HFS")

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.auth_profile_fragment, container, false)

        val name = root.findViewById<EditText>(R.id.name_field)
        val firstname = root.findViewById<EditText>(R.id.firstname_field)
        val email = root.findViewById<EditText>(R.id.email_field)
        val street = root.findViewById<EditText>(R.id.Street_field)
        val phonenumber = root.findViewById<EditText>(R.id.phonenumber_field)
        val plz = root.findViewById<EditText>(R.id.plz_field)
        val city = root.findViewById<EditText>(R.id.City_field)
        val usertype = root.findViewById<Spinner>(R.id.usertype_field)
        profilepic = root.findViewById<ImageView>(R.id.ProfilePic_field)
        val updatebtn = root.findViewById<Button>(R.id.updateProfile_btn)
        val savebtn = root.findViewById<Button>(R.id.saveProfile_btn)
        val cancel = root.findViewById<Button>(R.id.cancelProfile_btn)
        val newpicbtn = root.findViewById<Button>(R.id.uploadnewpic)


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

                                Toast.makeText(root.context, "Update Failed", Toast.LENGTH_LONG).show()
                                Log.d("Update", result.getException().message.toString())
                            }
                            is Result.Success -> {


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
            newpicbtn.visibility = GONE
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
            newpicbtn.visibility = GONE
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
            newpicbtn.visibility = VISIBLE

        }

        val job: Job = GlobalScope.launch(context = Dispatchers.IO) {
            requireAuthManager().SessionID().take(1).collect {
                //do actions

                Fuel.get(
                        RestPath.user(it)
                ).responseString { _, _, result ->

                    when (result) {


                        is Result.Failure -> {
                            this@ProfileFragment.activity?.runOnUiThread() {
                                Log.d("Error", result.getException().message.toString())
                                Toast.makeText(root.context, "Login Failed", Toast.LENGTH_LONG).show()
                            }
                        }
                        is Result.Success -> {
                            val data = result.get()

                            Log.d("USerProfile", data)

                            this@ProfileFragment.activity?.runOnUiThread() {

                                val Profile = Json.decodeFromString<UserME>(data);
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




        return  root;
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
            Log.d("Picturerun",  getPath(uri)!!)



            val data = FileDataPart.from(getPath(uri)!!, name = "profile_pic")
            // from(data?.data?.encodedPath.toString() , name = "profile_pic")
        val job: Job = GlobalScope.launch(context = Dispatchers.IO) {
            requireAuthManager().SessionID().take(1).collect {
                //do actions

                val asyncupload = Fuel.upload(RestPath.userUpdate(it), Method.PUT)
                        .add(data)
                        .responseString { _, _, result ->
                            when (result) {
                                is Result.Failure -> {
                                    Toast.makeText(this@ProfileFragment.context, "Picture Update Failed", Toast.LENGTH_LONG).show()
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