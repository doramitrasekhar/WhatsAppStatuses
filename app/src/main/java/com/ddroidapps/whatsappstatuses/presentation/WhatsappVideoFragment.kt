package com.ddroidapps.whatsappstatuses.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.ddroidapps.whatsappstatuses.adapter.StatusVideoAdapter
import com.ddroidapps.whatsappstatuses.databinding.WhatsappVideoListBinding
import com.ddroidapps.whatsappstatuses.model.StatusesState
import com.ddroidapps.whatsappstatuses.util.AppUtil
import com.ddroidapps.whatsappstatuses.util.SpacingItemDecorator
import com.ddroidapps.whatsappstatuses.viewmodel.StatusesVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class WhatsappVideoFragment : Fragment() {

    private lateinit var _binding: WhatsappVideoListBinding
    private val statusesVM: StatusesVM by viewModels()
    private lateinit var statusVideoAdapter: StatusVideoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = WhatsappVideoListBinding.inflate(inflater, container, false)
        initializeRecyclerView(_binding)
        observeResultState()
        return _binding.root
    }

    private fun observeResultState() {
        viewLifecycleOwner.lifecycleScope.launch {
            statusesVM.statusInfo.collectLatest { infoState ->
                when (infoState) {
                    is StatusesState.FetchUriInfo -> {
                        getFiles(Uri.parse(infoState.uri))
                    }
                    StatusesState.OpenDirectory -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            openDirectory()
                        }
                    }
                    is StatusesState.Success -> {
                        val result = infoState.uriInfo
                        statusVideoAdapter.updateForecastItems(result)
                    }
                    StatusesState.Init -> {
                        statusesVM.handleDirectoryPermission()
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun openDirectory() {
        val storageManager = context?.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val intent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
        val whatsAppDirectoryPath = File(Environment.getExternalStorageDirectory().toString() +
                File.separator + AppUtil.WHATSAPP_BASE_PATH)
        if (whatsAppDirectoryPath.exists()) {
            var uri = if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra<Uri>(AppUtil.STORAGE_MANAGER_INTENT_PROVIDER_KEY) as Uri
            } else {
                intent.getParcelableExtra<Uri>(AppUtil.STORAGE_MANAGER_INTENT_KEY_DATA) as Uri
            }
            var scheme = uri.toString()
            scheme = scheme.replace(AppUtil.ROOT, AppUtil.DOCUMENT)
            val finalDirPath = "$scheme${AppUtil.PATH_SEPARATOR}${AppUtil.WHATSAPP_PERMISSION_PATH}"
            uri = Uri.parse(finalDirPath)
            intent.putExtra(AppUtil.STORAGE_MANAGER_INTENT_PROVIDER_KEY, uri)
            startForResult.launch(intent)
        }
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data?.data != null) {
                    val uri: Uri? = result.data?.data
                    if (uri?.path?.endsWith(AppUtil.STATUSES_FOLDER_NAME) == true) {
                        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        context?.contentResolver?.takePersistableUriPermission(uri, takeFlags)
                        statusesVM.saveWhatsAppStatusesUriInfo(uri.toString())
                    }
                }
            }
        }

    private fun getFiles(uri: Uri) {
        if (Build.VERSION.SDK_INT >= 29) {
            val fromTreeUri = DocumentFile.fromTreeUri(requireContext(), uri)
            fromTreeUri?.let { statusesVM.getWhatsAppVideoFiles(it) }
        }
    }

    private fun initializeRecyclerView(binding: WhatsappVideoListBinding) {
        statusVideoAdapter = StatusVideoAdapter(StatusVideoAdapter.OnClickListener {
            val intent = Intent(context, WhatsAppVideoViewer::class.java)
            intent.putExtra("uri_info", it.toString());
            startActivity(intent)
        })
        val x = (resources.displayMetrics.density * 3).toInt() //converting dp to pixels
        binding.apply {
            statusesListView.apply {
                layoutManager = GridLayoutManager(context, 2)
                itemAnimator = DefaultItemAnimator()
                adapter = statusVideoAdapter
                addItemDecoration(SpacingItemDecorator(x))
            }
        }
    }
}