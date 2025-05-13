package com.schedule.rt.sync.objectsingleton

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.schedule.rt.sync.R


object DialogUtil {

    fun Fragment.showToastFragment(fragment: Fragment, durationMillis: Long = 3000L) {
        val activity = requireActivity()
        val containerId = R.id.mainToastContainer
        val fragmentManager = activity.supportFragmentManager

        // Hapus fragment toast sebelumnya jika ada
        fragmentManager.findFragmentById(containerId)?.let {
            fragmentManager.beginTransaction().remove(it).commitNow()
        }

        // Tambahkan fragment toast baru
        fragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .commit()

        // Hapus otomatis setelah beberapa detik
        Handler(Looper.getMainLooper()).postDelayed({
            fragmentManager.findFragmentById(containerId)?.let {
                fragmentManager.beginTransaction().remove(it).commit()
            }
        }, durationMillis)
    }

    fun Fragment.replaceBottomSheetFragmentWithBackStack(fragment: Fragment, tag: String?) {
        val activity = requireActivity()
        val containerId = R.id.mainBottomSheetContainer
        val fragmentManager = activity.supportFragmentManager

        val existingFragment = fragmentManager.findFragmentById(containerId)
        if (existingFragment != null) {
            fragmentManager.popBackStack()
            fragmentManager.beginTransaction()
                .remove(existingFragment)
                .commitNow()
        }

        fragmentManager.beginTransaction()
            .add(containerId, fragment)
            .addToBackStack(tag)
            .commit()
    }

    fun Fragment.addBottomSheetFragment(fragment: Fragment) {
        val activity = requireActivity()
        val containerId = R.id.mainBottomSheetContainer

        val fragmentManager = activity.supportFragmentManager
        fragmentManager.beginTransaction()
            .add(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun Fragment.removeAllBottomSheetFragment() {
        val activity = requireActivity()
        val containerId = R.id.mainBottomSheetContainer
        val fragmentManager = activity.supportFragmentManager

        val existingFragment = fragmentManager.findFragmentById(containerId)
        if (existingFragment != null) {
            fragmentManager.popBackStack()
            fragmentManager.beginTransaction()
                .remove(existingFragment)
                .commitNow()
        }
    }

    fun Fragment.replaceFragmentWithBackStack(container: Int, fragment: Fragment, tag: String?) {
        requireActivity().supportFragmentManager.commit {
            replace(container, fragment)
            addToBackStack(tag)
        }
    }

    fun Fragment.replaceFragmentWithoutBackStack(container: Int, fragment: Fragment) {
        requireActivity().supportFragmentManager.commit {
            replace(container, fragment)
        }
    }

    fun Fragment.addFragmentWithBackStack(container: Int, fragment: Fragment, tag: String?) {
        requireActivity().supportFragmentManager.commit {
            requireActivity().supportFragmentManager.findFragmentById(container)?.let { hide(it) }
            add(container, fragment)
            addToBackStack(tag)
        }
    }

    fun Fragment.addFragmentWithoutBackStack(fragment: Fragment) {
        val fragmentManager = requireActivity().supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(R.id.mainBottomSheetContainer)

        fragmentManager.commit {
            currentFragment?.let { hide(it) }
            add(R.id.mainBottomSheetContainer, fragment)
        }
    }

    fun Fragment.removeTopFragmentAndShowPrevious() {
        val fragmentManager = requireActivity().supportFragmentManager
        val topFragment = fragmentManager.findFragmentById(R.id.mainBottomSheetContainer)

        if (topFragment != null) {
            fragmentManager.commit {
                remove(topFragment)

                val remaining = fragmentManager.fragments.filter { it.isAdded && it != topFragment }

                if (remaining.isNotEmpty()) {
                    show(remaining.last())
                }
            }
        }
    }

    fun Fragment.removeFragmentFromContainer(container: Int) {
        val fragmentManager = requireActivity().supportFragmentManager

        val existingFragment = fragmentManager.findFragmentById(container)
        if (existingFragment != null) {
            fragmentManager.fragments
                .filter { it.id == container }
                .forEach {
                    fragmentManager.beginTransaction().remove(it).commit()
                }
        }
    }

    fun Fragment.replaceToastFragment(container: Int, fragment: Fragment) {
        val fragmentManager = requireActivity().supportFragmentManager

        fragmentManager.findFragmentById(container)?.let {
            fragmentManager.beginTransaction().remove(it).commit()
        }

        fragmentManager.commit {
            replace(container, fragment)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            fragmentManager.findFragmentById(container)?.let {
                fragmentManager.beginTransaction().remove(it).commit()
            }
        }, 3000L)
    }

    fun hideKeyboard(view: View) {
        val imm = getSystemService(view.context, InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()

}