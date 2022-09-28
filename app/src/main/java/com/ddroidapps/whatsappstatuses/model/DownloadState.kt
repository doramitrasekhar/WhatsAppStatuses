package com.ddroidapps.whatsappstatuses.model

import android.net.Uri

sealed class DownloadState {
    object Init : DownloadState()
    class Success(val uriInfo: ArrayList<Uri>) : DownloadState()
}