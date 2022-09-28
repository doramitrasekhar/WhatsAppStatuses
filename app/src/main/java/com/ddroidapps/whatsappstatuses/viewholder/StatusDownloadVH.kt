package com.ddroidapps.whatsappstatuses.viewholder

import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ddroidapps.whatsappstatuses.databinding.WhatsappDownloadListItemBinding

class StatusDownloadVH(private val viewHolder: WhatsappDownloadListItemBinding) :
    RecyclerView.ViewHolder(viewHolder.root) {

    fun bind(uri: Uri, isVideo: Boolean) {
        with(viewHolder) {
            Glide.with(viewHolder.root.context)
                .load(uri)
                .into(statusVideoItem)
        }
        if(isVideo){
            viewHolder.videoThumbnail.visibility = View.VISIBLE
        }else{
            viewHolder.videoThumbnail.visibility = View.GONE
        }
    }
}