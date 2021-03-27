package io.moxd.shopforme.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.moxd.shopforme.getRealPathFromURI_API19
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ProfileLifecycleObserver(private val registry : ActivityResultRegistry, private  val viewModel: ProfileViewModel, private  val context: Context)
    : DefaultLifecycleObserver {

        lateinit var requestPermissionGallery : ActivityResultLauncher<String>
        lateinit var requestPermissionCamera : ActivityResultLauncher<String>
        lateinit var getContentGallery : ActivityResultLauncher<Intent>
        lateinit var getContentCamera : ActivityResultLauncher<Intent>

    override fun onCreate(owner: LifecycleOwner) {
        requestPermissionGallery = registry.register("key",owner, ActivityResultContracts.RequestPermission() )
         {
            if(it){
                GalleryGet()
            }
        }
        getContentGallery = registry.register("key2",owner,ActivityResultContracts.StartActivityForResult()){
            if(  it.resultCode == Activity.RESULT_OK  ) {
                val uri = it.data?.data!!
                Timber.d( uri.path.toString())
                viewModel.uploadimg(getRealPathFromURI_API19(context,uri)!!)

            }
        }

        requestPermissionCamera = registry.register("key2",owner,ActivityResultContracts.RequestPermission() ,
        ) {
            if(it){
                CameraGet()
            }
        }

        getContentCamera = registry.register("key2",owner,ActivityResultContracts.StartActivityForResult()){
            if(  it.resultCode == Activity.RESULT_OK  ) {

                viewModel.uploadimg(currentPhotoPath)
            }
        }

    }

    fun GalleryAction(){

        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                GalleryGet()
            }
             shouldShowRequestPermissionRationale(context as Activity,Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                //
                MaterialAlertDialogBuilder(context)
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

    private fun GalleryGet(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Test Subject")
        val chooseIntent = Intent.createChooser(intent, "Select Picture")
        getContentGallery.launch(chooseIntent)
    }
    lateinit var currentPhotoPath: String
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = (context as Activity).getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    fun CameraAction(){

        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                CameraGet()
            }
            shouldShowRequestPermissionRationale(context as Activity,Manifest.permission.CAMERA) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                //
                MaterialAlertDialogBuilder(context)
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

    private fun CameraGet(){

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity((context as Activity).packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {

                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                       context,
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

        getContentCamera.launch(chooseIntent)
    }

}