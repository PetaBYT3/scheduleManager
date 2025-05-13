package com.schedule.rt.sync.fragment

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.schedule.rt.sync.R
import com.schedule.rt.sync.activity.ActivityMain
import com.schedule.rt.sync.databinding.FragmentSettingsBinding
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.service.PermissionManager
import com.schedule.rt.sync.userpreferences.SettingsPreferences
import kotlinx.coroutines.launch

class FragmentSettings : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var settingsPreferences: SettingsPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = TransitionUtil.enterTransition()
        returnTransition = TransitionUtil.returnTransition()
        exitTransition = TransitionUtil.exitTransition()
        reenterTransition = TransitionUtil.reenterTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        binding.toolBar.setNavigationOnClickListener {
            (requireActivity() as ActivityMain).btnDrawer()
        }

        settingsPreferences = SettingsPreferences(requireContext())

        notificationPermission()

        foregroundServices()
    }

    private fun notificationPermission() {
        val notificationPermission = PermissionManager(
            this,
            onAllGranted = {
                binding.ivNotificationPermission.setImageResource(R.drawable.check)
            },
            onDenied = {
                binding.ivNotificationPermission.setImageResource(R.drawable.close)
            }
        )

        val notificationPermissionToRequest = mutableListOf(
            Manifest.permission.POST_NOTIFICATIONS
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        notificationPermission.requestPermissions(notificationPermissionToRequest)
    }

    private fun foregroundServices() {
        lifecycleScope.launch {
            settingsPreferences.foregroundServiceStatus.collect {
                binding.swForegroundServices.isChecked = it
            }
        }

        binding.swForegroundServices.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                settingsPreferences.setForegroundService(isChecked)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}