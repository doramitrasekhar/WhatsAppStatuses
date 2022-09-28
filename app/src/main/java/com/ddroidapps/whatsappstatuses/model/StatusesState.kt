package com.ddroidapps.whatsappstatuses.model

import android.net.Uri

sealed class StatusesState {
    object OpenDirectory : StatusesState()
    object Init : StatusesState()
    class FetchUriInfo(var uri: String) : StatusesState()
    class Success(val uriInfo: ArrayList<Uri>) : StatusesState()
}