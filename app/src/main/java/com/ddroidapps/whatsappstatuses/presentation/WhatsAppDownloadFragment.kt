package com.ddroidapps.whatsappstatuses.presentation

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ddroidapps.whatsappstatuses.R
import com.ddroidapps.whatsappstatuses.adapter.StatusDownloadAdapter
import com.ddroidapps.whatsappstatuses.databinding.WhatsappDownloadListBinding
import com.ddroidapps.whatsappstatuses.model.DownloadState
import com.ddroidapps.whatsappstatuses.util.SpacingItemDecorator
import com.ddroidapps.whatsappstatuses.viewmodel.StatusesDownloadVM
import com.gun0912.tedpermission.provider.TedPermissionProvider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File


class WhatsAppDownloadFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var _binding: WhatsappDownloadListBinding
    private val statusesDownloadVM: StatusesDownloadVM by viewModels()
    private lateinit var statusDownloadAdapter: StatusDownloadAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = WhatsappDownloadListBinding.inflate(inflater, container, false)
        initializeRecyclerView(_binding)
        observeResultState()
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding.swipeLayout.setOnRefreshListener(this)
        _binding.swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light)
    }

    private fun observeResultState() {
        viewLifecycleOwner.lifecycleScope.launch {
            statusesDownloadVM.statusInfo.collectLatest { infoState ->
                when (infoState) {
                    DownloadState.Init -> {
                        val dcim: File? =
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/" + TedPermissionProvider.context?.getString(
                                R.string.app_name))
                        if(dcim?.exists() == true) {
                            dcim.let {
                                statusesDownloadVM.getWhatsAppFiles(dcim)
                            }
                        }
                    }
                    is DownloadState.Success -> {
                        val result = infoState.uriInfo
                        statusDownloadAdapter.updateForecastItems(result)
                        if( _binding.swipeLayout.isRefreshing) {
                            _binding.swipeLayout.isRefreshing = false
                        }
                    }
                }
            }
        }
    }

    private fun initializeRecyclerView(binding: WhatsappDownloadListBinding) {
        statusDownloadAdapter = StatusDownloadAdapter(StatusDownloadAdapter.OnClickListener { uri ->
            context?.let { mContext ->
                val mimeType = getMimeType(mContext, uri)
                mimeType?.let { mimeType ->
                    if (isVideoFile(mimeType)) {
                        val intent = Intent(context, WhatsAppVideoViewer::class.java)
                        intent.putExtra("uri_info", uri.toString())
                        startActivity(intent)
                    } else {
                        val intent = Intent(context, WhatsAppImageViewer::class.java)
                        intent.putExtra("uri_info", uri.toString())
                        startActivity(intent)
                    }
                }
            }
        })
        val x = (resources.displayMetrics.density * 3).toInt() //converting dp to pixels
        binding.apply {
            downloadList.apply {
                layoutManager = GridLayoutManager(context, 2)
                itemAnimator = DefaultItemAnimator()
                adapter = statusDownloadAdapter
                addItemDecoration(SpacingItemDecorator(x))
            }
        }
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        val extension: String? = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
        } else {
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(uri.path?.let { File(it) }).toString())
        }
        return extension
    }

    fun isVideoFile(mimeType: String): Boolean {
        if (mimeType == "mp4"
            || mimeType == ("avi")
            || mimeType == ("mkv")
            || mimeType == ("mov")
            || mimeType == ("flv")
            || mimeType == ("3gp")
        ) {
            return true
        }
        return false
    }

    override fun onRefresh() {
        _binding.swipeLayout.isRefreshing = true
        val dcim: File? =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/" + TedPermissionProvider.context?.getString(
                R.string.app_name))
        if(dcim?.exists() == true) {
            dcim.let {
                statusesDownloadVM.getWhatsAppFiles(dcim)
            }
        }
    }
}