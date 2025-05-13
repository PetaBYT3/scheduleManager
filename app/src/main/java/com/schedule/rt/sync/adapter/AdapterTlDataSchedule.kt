package com.schedule.rt.sync.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class AdapterTlDataSchedule(
    fragment: Fragment,
    private val fragmentList: List<Pair<String, Fragment>>? = null
): FragmentStateAdapter(fragment) {

    override fun createFragment(position: Int): Fragment {
        val (tag, fragment) = fragmentList!![position]
        fragment.arguments = Bundle().apply {
            putString("day", tag)
        }
        return fragment
    }

    override fun getItemCount(): Int {
        return fragmentList!!.size
    }

    fun getFragmentTag(position: Int): String {
        return fragmentList!![position].first
    }
}