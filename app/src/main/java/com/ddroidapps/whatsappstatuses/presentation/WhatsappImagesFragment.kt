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
import com.ddroidapps.whatsappstatuses.adapter.StatusAdapter
import com.ddroidapps.whatsappstatuses.databinding.WhatsappImagesListBinding
import com.ddroidapps.whatsappstatuses.model.StatusesState
import com.ddroidapps.whatsappstatuses.util.AppUtil.DOCUMENT
import com.ddroidapps.whatsappstatuses.util.AppUtil.PATH_SEPARATOR
import com.ddroidapps.whatsappstatuses.util.AppUtil.ROOT
import com.ddroidapps.whatsappstatuses.util.AppUtil.STATUSES_FOLDER_NAME
import com.ddroidapps.whatsappstatuses.util.AppUtil.STORAGE_MANAGER_INTENT_KEY_DATA
import com.ddroidapps.whatsappstatuses.util.AppUtil.STORAGE_MANAGER_INTENT_PROVIDER_KEY
import com.ddroidapps.whatsappstatuses.util.AppUtil.WHATSAPP_BASE_PATH
import com.ddroidapps.whatsappstatuses.util.AppUtil.WHATSAPP_PERMISSION_PATH
import com.ddroidapps.whatsappstatuses.util.SpacingItemDecorator
import com.ddroidapps.whatsappstatuses.viewmodel.StatusesVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class WhatsappImagesFragment : Fragment() {
    private lateinit var _binding: WhatsappImagesListBinding
    private val statusesVM: StatusesVM by viewModels()
    private lateinit var statusAdapter: StatusAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = WhatsappImagesListBinding.inflate(inflater, container, false)
        observeResultState()
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeRecyclerView(_binding)
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
                        statusAdapter.updateForecastItems(result)
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
                File.separator + WHATSAPP_BASE_PATH)
        if (whatsAppDirectoryPath.exists()) {
            var uri = if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra<Uri>(STORAGE_MANAGER_INTENT_PROVIDER_KEY) as Uri
            } else {
                intent.getParcelableExtra<Uri>(STORAGE_MANAGER_INTENT_KEY_DATA) as Uri
            }
            var scheme = uri.toString()
            scheme = scheme.replace(ROOT, DOCUMENT)
            val finalDirPath = "$scheme$PATH_SEPARATOR$WHATSAPP_PERMISSION_PATH"
            uri = Uri.parse(finalDirPath)
            intent.putExtra(STORAGE_MANAGER_INTENT_PROVIDER_KEY, uri)
            startForResult.launch(intent)
        }
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data?.data != null) {
                    val uri: Uri? = result.data?.data
                    if (uri?.path?.endsWith(STATUSES_FOLDER_NAME) == true) {
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
            fromTreeUri?.let { statusesVM.getWhatsAppFiles(it) }
        }
    }

    private fun initializeRecyclerView(binding: WhatsappImagesListBinding) {
        statusAdapter = StatusAdapter(StatusAdapter.OnClickListener { uri ->
            val intent = Intent(context, WhatsAppImageViewer::class.java)
            intent.putExtra("uri_info", uri.toString());
            startActivity(intent)
        })
        val x = (resources.displayMetrics.density * 3).toInt() //converting dp to pixels
        binding.apply {
            statusesListView.apply {
                layoutManager = GridLayoutManager(context, 2)
                itemAnimator = DefaultItemAnimator()
                adapter = statusAdapter
                addItemDecoration(SpacingItemDecorator(x))
            }
        }
    }
}