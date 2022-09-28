package com.ddroidapps.whatsappstatuses.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ddroidapps.whatsappstatuses.databinding.WhatsappImagesListItemBinding
import com.ddroidapps.whatsappstatuses.diffutil.StatusDiffUtil
import com.ddroidapps.whatsappstatuses.viewholder.StatusesVH

class StatusAdapter(private val onClickListener: OnClickListener) : RecyclerView.Adapter<StatusesVH>() {

    private var statusesItems = mutableListOf<Uri>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusesVH {
        val binding =
            WhatsappImagesListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StatusesVH(binding)
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

    override fun onBindViewHolder(holder: StatusesVH, position: Int) {
        val eachItem = statusesItems[position]
        holder.itemView.setOnClickListener {
            onClickListener.onClick(eachItem)
        }
        holder.bind(eachItem)
    }

    class OnClickListener(val clickListener: (uri: Uri) -> Unit) {
        fun onClick(uri: Uri) = clickListener(uri)
    }
}