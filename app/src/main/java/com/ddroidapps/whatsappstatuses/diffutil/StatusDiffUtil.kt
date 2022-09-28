package com.ddroidapps.whatsappstatuses.diffutil

import android.net.Uri
import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil

class StatusDiffUtil(
    private val oldList: List<Uri>,
    private val newList: List<Uri>,
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].toString() === newList[newItemPosition].toString()
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItemPosition.toString() == newItemPosition.toString()
    }


    @Nullable
    override fun getChangePayload(oldPosition: Int, newPosition: Int): Any? {
        return super.getChangePayload(oldPosition, newPosition)
    }
}