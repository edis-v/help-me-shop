package io.moxd.shopforme.ui.dialog


import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import io.moxd.shopforme.R


class CameraGalleryDialog
(var c: Activity ,val title: String) : Dialog(c), View.OnClickListener {

    lateinit var camera: Button
    lateinit var gallery: Button
    lateinit var cancel: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.camera_gallery_dialog)
        val titletextview = findViewById<TextView>(R.id.camera_gallery_dialog_title)
        titletextview.text = title
        gallery = findViewById(R.id.camera_gallery_dialog_gallery)
        camera = findViewById(R.id.camera_gallery_dialog_camera)
        cancel = findViewById(R.id.camera_gallery_dialog_cancel)
        gallery.setOnClickListener(this)
        cancel.setOnClickListener(this)
        camera.setOnClickListener(this)
        this.window?.setBackgroundDrawableResource(android.R.color.transparent);
    }

    override fun onClick(v: View) {

        dismiss()
    }


}