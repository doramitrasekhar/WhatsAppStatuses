package com.ddroidapps.whatsappstatuses.viewholder

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ddroidapps.whatsappstatuses.databinding.WhatsappImagesListItemBinding

class StatusesVH(private val viewHolder: WhatsappImagesListItemBinding) :
    RecyclerView.ViewHolder(viewHolder.root) {
    fun bind(uri: Uri) {
        with(viewHolder) {
            Glide.with(viewHolder.root.context)
                .load(uri)
                .into(statusImageItem)
        }
    }
}