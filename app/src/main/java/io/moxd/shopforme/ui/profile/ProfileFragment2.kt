package io.moxd.shopforme.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import io.moxd.shopforme.*

import io.moxd.shopforme.databinding.AuthProfileFragmentBinding
import io.moxd.shopforme.ui.dialog.CameraGalleryDialog
import io.moxd.shopforme.ui.profile_list.ProfileListFragment
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment2 : Fragment()    {

    private val viewModel : ProfileViewModel by viewModels {
        ProfileViewModelFactory(this, arguments)
    }
    lateinit var observer : ProfileLifecycleObserver
    lateinit var binding: AuthProfileFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        observer = ProfileLifecycleObserver(requireActivity().activityResultRegistry,viewModel,requireContext())
        lifecycle.addObserver(observer)
        return inflater.inflate(R.layout.auth_profile_fragment,container,false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = AuthProfileFragmentBinding.bind(view)

        //implement onClick
        binding.apply {

            uploadnewpic.setOnClickListener {

                val popup = CameraGalleryDialog(requireActivity())



                popup.show()
                popup.gallery.setOnClickListener { observer.GalleryAction()
                popup.dismiss()
                }

                popup.camera.setOnClickListener { observer.CameraAction()
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







}