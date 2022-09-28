package com.ddroidapps.whatsappstatuses.presentation

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ddroidapps.whatsappstatuses.R
import com.ddroidapps.whatsappstatuses.databinding.WhatsappVideoListItemDisplayBinding
import com.ddroidapps.whatsappstatuses.util.URIPathHelper
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class WhatsAppVideoViewer : AppCompatActivity() {
    private lateinit var _binding: WhatsappVideoListItemDisplayBinding
    private var videoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = WhatsappVideoListItemDisplayBinding.inflate(layoutInflater)
        val view = _binding.root
        setContentView(view)
        supportActionBar?.title = "Status Videos"
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,
            R.color.md_green_800)))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val uri = Uri.parse(intent.getStringExtra("uri_info").toString())
        initializePlayer(uri)
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


    private var permissionlistener: PermissionListener = object : PermissionListener {
        override fun onPermissionGranted() {
            if (!verifyPermissions()) {
                return;
            }
            val uri =  Uri.parse(intent.getStringExtra("uri_info").toString())
            uri.path?.let { File(it) }
            val mmyFile: String? = URIPathHelper().getPath(applicationContext,uri)
            mmyFile?.let {
                saveMediaIntoGallery(applicationContext,applicationContext.getString(R.string.app_name),uri)
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

    fun checkWritePermission() {
        TedPermission.create()
            .setPermissionListener(permissionlistener)
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            .check()
    }

    private fun buildMediaSource(uriInfo: Uri) {
        val mediaItem: MediaItem = MediaItem.fromUri(uriInfo)
        videoPlayer?.addMediaItem(mediaItem)
    }

    private fun initializePlayer(uriInfo: Uri) {
        videoPlayer = ExoPlayer.Builder(this).build()
        _binding.videoPlayerView.player = videoPlayer
        buildMediaSource(uriInfo).let {
            videoPlayer?.prepare()
        }
    }

    override fun onResume() {
        super.onResume()
        videoPlayer?.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        videoPlayer?.playWhenReady = false
        if (isFinishing) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        videoPlayer?.release()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun saveMediaIntoGallery(context: Context,folderName: String, fileUri: Uri) {
        val uriSavedVideo: Uri?
        var createdvideo: File?
        val values = ContentValues()
        val videoFileName = "video_" + System.currentTimeMillis() + ".mp4"
        if (Build.VERSION.SDK_INT >= 29) {
            values.put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/$folderName")
            values.put(MediaStore.Video.Media.TITLE, videoFileName)
            values.put(MediaStore.Video.Media.DISPLAY_NAME, videoFileName)
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            values.put(
                MediaStore.Video.Media.DATE_ADDED,
                System.currentTimeMillis() / 1000)
            val collection =
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            uriSavedVideo = context.contentResolver.insert(collection, values)
        } else {
            val directory = (Environment.getExternalStorageDirectory().absolutePath
                    + File.separator + Environment.DIRECTORY_MOVIES + "/" + "YourFolder")
            createdvideo = File(directory, videoFileName)
            val file = File(directory, videoFileName)
            values.put(MediaStore.Video.Media.TITLE, videoFileName)
            values.put(MediaStore.Video.Media.DISPLAY_NAME, videoFileName)
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            values.put(
                MediaStore.Video.Media.DATE_ADDED,
                System.currentTimeMillis() / 1000)
            values.put(MediaStore.Video.Media.DATA, createdvideo.absolutePath)
            uriSavedVideo = context.contentResolver.insert(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                values)
        }
        if (Build.VERSION.SDK_INT >= 29) {
            values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Video.Media.IS_PENDING, 1);
        }
        val pfd: ParcelFileDescriptor?
        try {
            pfd = contentResolver.openFileDescriptor(uriSavedVideo!!, "w")
            val out = FileOutputStream(pfd!!.fileDescriptor)
            val input = FileInputStream(context.contentResolver.openFileDescriptor(fileUri, "r", null)?.fileDescriptor)
            IOUtils.copy(input,out)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (Build.VERSION.SDK_INT >= 29) {
            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            uriSavedVideo?.let {
                context.contentResolver.update(uriSavedVideo, values, null, null);
            }
        }
    }
}