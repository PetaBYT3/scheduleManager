package com.schedule.rt.sync.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.schedule.rt.sync.fragment.day.FragmentDataFriday
import com.schedule.rt.sync.fragment.day.FragmentDataMonday
import com.schedule.rt.sync.fragment.day.FragmentDataSaturday
import com.schedule.rt.sync.fragment.day.FragmentDataSunday
import com.schedule.rt.sync.fragment.day.FragmentDataThursday
import com.schedule.rt.sync.fragment.day.FragmentDataTuesday
import com.schedule.rt.sync.fragment.day.FragmentDataWednesday

class AdapterTlDataDay(fragment: Fragment): FragmentStateAdapter(fragment) {

    private val fragmentList = listOf(
        Pair("monday", FragmentDataMonday()),
        Pair("tuesday", FragmentDataTuesday()),
        Pair("wednesday", FragmentDataWednesday()),
        Pair("thursday", FragmentDataThursday()),
        Pair("friday", FragmentDataFriday()),
        Pair("saturday", FragmentDataSaturday()),
        Pair("sunday", FragmentDataSunday())
    )

    override fun createFragment(position: Int): Fragment {
        val (tag, fragment) = fragmentList[position]
        fragment.arguments = Bundle().apply {
            putString("day", tag)
        }
        return fragment
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    fun getFragmentTag(position: Int): String {
        return fragmentList[position].first
    }
}