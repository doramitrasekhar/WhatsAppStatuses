package com.ddroidapps.whatsappstatuses.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ddroidapps.whatsappstatuses.presentation.WhatsAppDownloadFragment
import com.ddroidapps.whatsappstatuses.presentation.WhatsappImagesFragment
import com.ddroidapps.whatsappstatuses.presentation.WhatsappVideoFragment

private const val NUM_TABS = 3

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return WhatsappImagesFragment()
            1 -> return WhatsappVideoFragment()
            2 -> return WhatsAppDownloadFragment()
        }
        return WhatsappImagesFragment()
    }
}