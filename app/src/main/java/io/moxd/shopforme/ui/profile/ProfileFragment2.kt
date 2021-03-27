package io.moxd.shopforme.ui.profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import io.moxd.shopforme.*

import io.moxd.shopforme.data.model.UserGSON
import io.moxd.shopforme.data.model.UserME
import io.moxd.shopforme.databinding.AuthProfileFragmentBinding
import io.moxd.shopforme.ui.dialog.CameraGalleryDialog
import io.moxd.shopforme.ui.login.LoginViewModelFactory
import io.moxd.shopforme.ui.profile_list.ProfileListFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment2 : Fragment()    {

    private val viewModel : ProfileViewModel by viewModels {
        ProfileViewModelFactory(this, arguments)
    }
    lateinit var binding: AuthProfileFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.auth_profile_fragment,container,false)
    }

    fun GalleryAction(){

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                GalleryGet()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
                //
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Berechtigung")
                    .setMessage("Um ein Bild aus der Gallery zu wählen benötigen wir die Berechtigung ")
                    .setNeutralButton("Nein Danke") { _, _ ->
                        // Respond to neutral button press
                    }
                    .setPositiveButton("Ja") { _, _ ->

                        requestPermissionGallery.launch(
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
        }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionGallery.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }


    }


    fun CameraAction(){

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                CameraGet()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                //
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Berechtigung")
                    .setMessage("Um ein Bild von der Kamera zu erstellen benötigen wir die Berechtigung ")
                    .setNeutralButton("Nein Danke") { _, _ ->
                        // Respond to neutral button press
                    }
                    .setPositiveButton("Ja") { _, _ ->

                        requestPermissionCamera.launch(
                            Manifest.permission.CAMERA)
                    }.show()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionCamera.launch(
                    Manifest.permission.CAMERA)
            }
        }


    }

    fun GalleryGet(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Test Subject")
        val chooseIntent = Intent.createChooser(intent, "Select Picture")
        getContent.launch(chooseIntent)
    }


    fun CameraGet(){

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {

                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "io.moxd.shopforme.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    takePictureIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Test Subject")
                    takePictureIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                }
            }
        }


        val chooseIntent = Intent.createChooser(intent, "Select Picture")

        getImageContent.launch(chooseIntent)
    }

    val requestPermissionGallery =
        registerForActivityResult(ActivityResultContracts.RequestPermission() ,
        ) {
            if(it){
            GalleryGet()
            }
        }

    val requestPermissionCamera =
        registerForActivityResult(ActivityResultContracts.RequestPermission() ,
        ) {
            if(it){
                CameraGet()
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = AuthProfileFragmentBinding.bind(view)

        //implement onClick
        binding.apply {

            uploadnewpic.setOnClickListener {

                val popup = CameraGalleryDialog(requireActivity())



                popup.show()
                popup.gallery.setOnClickListener { GalleryAction()
                popup.dismiss()
                }

                popup.camera.setOnClickListener { CameraAction()
                    popup.dismiss()
                }

            }
            updateProfileBtn.setOnClickListener { viewModel.OnEditClicked() }
            cancelProfileBtn.setOnClickListener {  viewModel.OnCancelClicked()}
            saveProfileBtn.setOnClickListener {


                //update Viewmodel Data

                viewModel.OnUpdateClicked( nameField.text.toString(),
                    firstnameField.text.toString(),
                    phonenumberField.text.toString(),
                    StreetField.text.toString(),
                    plzField.text.toString(),
                    CityField.text.toString(),
                    if(usertypeField.selectedItem.toString() == "Helfer") "HF" else "HFS") }
            profileButtonBack.setOnClickListener {
                //TODO Nav Graph
                val ft = (requireActivity() as MainActivity).supportFragmentManager.beginTransaction()
                ft.replace(R.id.mainframe, ProfileListFragment())
                ft.commit()
            }
        }



        viewModel.user?.observe(viewLifecycleOwner){
            //update UI

                if (it.isSuccessful) {

                    binding.apply {
                        val Profile = it.body()!!

                        var usertypes_txt = arrayListOf<String>("Helfer", "HilfeSuchender")
                        val ad = ArrayAdapter<String>(
                            root.context,
                            R.layout.support_simple_spinner_dropdown_item,
                            usertypes_txt
                        )
                        usertypeField.adapter = ad
                        nameField.setText(Profile.name)
                        firstnameField.setText(Profile.firstname)
                        emailField.setText(Profile.email)
                        StreetField.setText(Profile.Street)
                        phonenumberField.setText(Profile.phone_number)
                        plzField.setText(Profile.plz.toString())
                        CityField.setText(Profile.City)
                        Picasso.get().load(Profile.profile_pic).into(ProfilePicField)
                        if (Profile.usertype_txt == "Helfer")
                            usertypeField.setSelection(0)
                        else
                            usertypeField.setSelection(1)

                    }
                }else {

                    viewModel.getProfile()

                    Snackbar.make(view,getErrorRetro(it.errorBody()),Snackbar.LENGTH_LONG).show()

                    Timber.d(getErrorRetro(it.errorBody()))
                }





        }


        viewModel.edit.observe(viewLifecycleOwner){

    binding.apply {
            firstnameField.isEnabled = it
            nameField.isEnabled = it
            StreetField.isEnabled = it
            phonenumberField.isEnabled = it
            emailField.isEnabled = false
            StreetField.isEnabled = it
            usertypeField.isEnabled = it
            plzField.isEnabled  =it
            usertypeField.isClickable = it
            CityField.isEnabled = it

        cancelProfileBtn.visibility = if(it) View.VISIBLE else View.GONE
        saveProfileBtn.visibility = if(it) View.VISIBLE else View.GONE

        updateProfileBtn.visibility = if(it) View.GONE else View.VISIBLE




    }}
    }


    val getImageContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(  it.resultCode == Activity.RESULT_OK  ) {

                viewModel.uploadimg(currentPhotoPath)
        }
    }

    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }


    val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Handle the returned Uri
        if(  it.resultCode == Activity.RESULT_OK  ) {
            val uri = it.data?.data!!
            Timber.d( uri.path.toString())
                viewModel.uploadimg(getRealPathFromURI_API19(requireContext(),uri)!!)

        }

    }



}