package io.moxd.shopforme.ui.dialog


import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import io.moxd.shopforme.R


class CameraGalleryDialog
(var c: Activity) : Dialog(c), View.OnClickListener {

    lateinit var camera: Button
    lateinit var gallery: Button
    lateinit var cancel: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.camera_gallery_dialog)
        gallery = findViewById<View>(R.id.camera_gallery_dialog_gallery) as Button
        camera = findViewById<View>(R.id.camera_gallery_dialog_camera) as Button
        cancel = findViewById<View>(R.id.camera_gallery_dialog_cancel) as Button
        gallery.setOnClickListener(this)
        cancel.setOnClickListener(this)
        camera.setOnClickListener(this)
    }

    override fun onClick(v: View) {

        dismiss()
    }


}