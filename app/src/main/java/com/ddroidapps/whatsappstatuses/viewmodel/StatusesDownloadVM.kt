package com.ddroidapps.whatsappstatuses.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.ddroidapps.whatsappstatuses.model.DownloadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class StatusesDownloadVM  @Inject constructor() : ViewModel() {

    private val _statusInfo: MutableStateFlow<DownloadState> =
        MutableStateFlow(DownloadState.Init)
    var statusInfo: StateFlow<DownloadState> = _statusInfo

    fun getWhatsAppFiles(dcim: File) {
        val result = ArrayList<Uri>()
        val files: Array<File> = dcim.listFiles() as Array<File>
        for (file in files) {
            if(file.exists() && !file.name.contains(".trashed")) {
                result.add(Uri.fromFile(file))
            }
        }
        _statusInfo.value = DownloadState.Success(result)
    }
}