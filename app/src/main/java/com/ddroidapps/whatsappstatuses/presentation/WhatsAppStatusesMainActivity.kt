package com.ddroidapps.whatsappstatuses.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ddroidapps.whatsappstatuses.adapter.ViewPagerAdapter
import com.ddroidapps.whatsappstatuses.databinding.WhatsappStatusesMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WhatsAppStatusesMainActivity : AppCompatActivity() {

    private lateinit var binding: WhatsappStatusesMainBinding

    val tabItems = arrayOf(
        "IMAGES",
        "VIDEOS",
        "DOWNLOADED"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WhatsappStatusesMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout
        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabItems[position]
        }.attach()
    }
}