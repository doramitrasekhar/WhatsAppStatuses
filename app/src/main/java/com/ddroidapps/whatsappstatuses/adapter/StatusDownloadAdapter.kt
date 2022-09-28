package com.ddroidapps.whatsappstatuses.adapter

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ddroidapps.whatsappstatuses.databinding.WhatsappDownloadListItemBinding
import com.ddroidapps.whatsappstatuses.diffutil.StatusDiffUtil
import com.ddroidapps.whatsappstatuses.viewholder.StatusDownloadVH
import java.io.File


class StatusDownloadAdapter(private val onClickListener: OnClickListener) : RecyclerView.Adapter<StatusDownloadVH>() {

    private var statusesItems = mutableListOf<Uri>()
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusDownloadVH {
        val binding =
            WhatsappDownloadListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        this.context = parent.context
        return StatusDownloadVH(binding)
    }

    override fun getItemCount(): Int {
        return statusesItems.size
    }

    fun updateForecastItems(items: ArrayList<Uri>) {
        val diffCallback = StatusDiffUtil(statusesItems, items)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        with(statusesItems) {
            clear()
            addAll(items)
        }
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onBindViewHolder(holder: StatusDownloadVH, position: Int) {
        val eachItem = statusesItems[position]
        holder.itemView.setOnClickListener {
            onClickListener.onClick(eachItem)
        }
        context.let {
            val mimeType = getMimeType(context,eachItem)
            mimeType?.let {
                if(isVideoFile(mimeType)){
                    holder.bind(eachItem,true)
                }else{
                    holder.bind(eachItem,false)
                }
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
            || mimeType ==("avi")
            || mimeType == ("mkv")
            || mimeType == ("mov")
            || mimeType == ("flv")
            || mimeType == ("3gp")
        ) {
            return true
        }
        return false
    }

    class OnClickListener(val clickListener: (uri: Uri) -> Unit) {
        fun onClick(uri: Uri) = clickListener(uri)
    }
}