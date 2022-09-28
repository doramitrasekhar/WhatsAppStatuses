package com.ddroidapps.whatsappstatuses.presentation

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ddroidapps.whatsappstatuses.R
import com.ddroidapps.whatsappstatuses.databinding.WhatsappImagesListItemDisplayBinding
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.indicator.progresspie.ProgressPieIndicator
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.File.separator
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream


class WhatsAppImageViewer : AppCompatActivity() {

    private lateinit var _binding: WhatsappImagesListItemDisplayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BigImageViewer.initialize(GlideImageLoader.with(applicationContext))
        _binding = WhatsappImagesListItemDisplayBinding.inflate(layoutInflater)
        val view = _binding.root
        setContentView(view)
        supportActionBar?.title = "Status Images"
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,
            R.color.md_green_800)))
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        _binding.mImageView.setProgressIndicator(ProgressPieIndicator())
        _binding.mImageView.showImage(Uri.parse(intent.getStringExtra("uri_info").toString()))
        supportActionBar?.setIcon(R.drawable.ic_baseline_arrow_downward_24);
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_download -> {
                checkWritePermission()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun checkWritePermission() {
        TedPermission.create()
            .setPermissionListener(permissionlistener)
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            .check()
    }

    private var permissionlistener: PermissionListener = object : PermissionListener {
        override fun onPermissionGranted() {
            if (!verifyPermissions()) {
                return;
            }
            val uri =  Uri.parse(intent.getStringExtra("uri_info").toString())
            val file: File? = uri.path?.let { File(it) }

            val imageURL = file?.name
            val fileName = imageURL?.substring(imageURL.lastIndexOf('/') + 1);
            fileName?.let {
                Glide.with(applicationContext)
                    .load(uri)
                    .into(object : CustomTarget<Drawable?>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            @Nullable transition: Transition<in Drawable?>?,
                        ) {
                            val bitmap = (resource as BitmapDrawable).bitmap
                            saveImage(bitmap,applicationContext,getString(R.string.app_name), uri)
                        }
                        override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                        override fun onLoadFailed(@Nullable errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            Toast.makeText(applicationContext,
                                "Failed to Download Image! Please try again later.",
                                Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }

        override fun onPermissionDenied(deniedPermissions: List<String>) {
            Toast.makeText(applicationContext,
                "Permission Denied\n$deniedPermissions",
                Toast.LENGTH_SHORT).show()
        }
    }

    fun verifyPermissions(): Boolean {
        // This will return the current Status
        val permissionExternalMemory =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionExternalMemory != PackageManager.PERMISSION_GRANTED) {
            val STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            // If permission not granted then ask for permission real time.
            ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS, 1)
            return false
        }
        return true
    }

    private fun saveImage(bitmap: Bitmap, context: Context, folderName: String, fileUri: Uri) {
        if (Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/$folderName")
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            val inputStream = FileInputStream(context.contentResolver.openFileDescriptor(fileUri, "r", null)?.fileDescriptor)
            if (uri != null) {
                val outputStream = context.contentResolver.openOutputStream(uri)
                IOUtils.copy(inputStream,outputStream)
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
            }
        } else {
            val directory = File(Environment.getExternalStorageDirectory().toString() + separator + folderName)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = System.currentTimeMillis().toString() + ".png"
            val file = File(directory, fileName)
            saveImageToStream(bitmap, FileOutputStream(file))
            if (file.absolutePath != null) {
                val values = contentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
        }
    }

    private fun contentValues() : ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}