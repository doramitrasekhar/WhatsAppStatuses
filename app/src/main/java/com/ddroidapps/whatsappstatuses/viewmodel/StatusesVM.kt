package com.ddroidapps.whatsappstatuses.viewmodel

import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddroidapps.domain.usecases.GetUriInfoUseCase
import com.ddroidapps.domain.usecases.SaveUriInfoUseCase
import com.ddroidapps.domain.usecases.ValidateUriInfoUseCase
import com.ddroidapps.whatsappstatuses.model.StatusesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class StatusesVM @Inject constructor(
    private val saveUriInfoUseCase: SaveUriInfoUseCase,
    private val getUriInfoUseCase: GetUriInfoUseCase,
    private val validateUriInfoUseCase: ValidateUriInfoUseCase,
) : ViewModel() {

    private val _statusInfo: MutableStateFlow<StatusesState> =
        MutableStateFlow(StatusesState.Init)
    val statusInfo: StateFlow<StatusesState> = _statusInfo

    fun handleDirectoryPermission() {
        viewModelScope.launch {
            val result: Boolean = validateUriInfoUseCase()
            if (!result) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    _statusInfo.value = StatusesState.OpenDirectory
                }
            } else {
                val resultUri = getUriInfoUseCase()
                _statusInfo.value = StatusesState.FetchUriInfo(resultUri)
            }
        }
    }

    fun saveWhatsAppStatusesUriInfo(uri: String) {
        viewModelScope.launch() {
            saveUriInfoUseCase(uri)
            _statusInfo.value = StatusesState.FetchUriInfo(uri)
        }
    }

    fun getWhatsAppFiles(fromTreeUri: DocumentFile) {
        val result = ArrayList<Uri>()
        val documentFiles: Array<out DocumentFile> = fromTreeUri.listFiles()
        for (documentFile in documentFiles) {
            documentFile.name?.let { documentName ->
                if (documentName.endsWith(".jpg")
                    || documentName.endsWith(".jpeg")
                    || documentName.endsWith(".png")
                ) {
                    result.add(documentFile.uri)
                }
            }
        }
        _statusInfo.value = StatusesState.Success(result)
    }

    fun getWhatsAppVideoFiles(fromTreeUri: DocumentFile) {
        val result = ArrayList<Uri>()
        val documentFiles: Array<out DocumentFile> = fromTreeUri.listFiles()
        for (documentFile in documentFiles) {
            documentFile.name?.let { documentName ->
                if (documentName.endsWith(".mp4")
                    || documentName.endsWith(".avi")
                    || documentName.endsWith(".mkv")
                    || documentName.endsWith(".mov")
                    || documentName.endsWith(".flv")
                    || documentName.endsWith(".3gp")
                ) {
                    result.add(documentFile.uri)
                }
            }
        }
        _statusInfo.value = StatusesState.Success(result)
    }
}