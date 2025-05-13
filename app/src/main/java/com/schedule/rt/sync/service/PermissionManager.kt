package com.schedule.rt.sync.service

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class PermissionManager(
    private val fragment: Fragment,
    private val onAllGranted: () -> Unit,
    private val onDenied: (deniedPermissions: List<String>) -> Unit
) {
    private val permissionLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val denied = result.filter { !it.value }.map { it.key }
        if (denied.isEmpty()) {
            onAllGranted()
        } else {
            onDenied(denied)
        }
    }

    fun requestPermissions(permissions: List<String>) {
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(
                fragment.requireContext(), it
            ) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            onAllGranted()
        } else {
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }
}
