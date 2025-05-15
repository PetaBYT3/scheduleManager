package com.schedule.rt.sync.fragment

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.lifecycle.lifecycleScope
import com.schedule.rt.sync.R
import com.schedule.rt.sync.activity.ActivityMain
import com.schedule.rt.sync.databinding.FragmentSettingsBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
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
        settingsPreferences = SettingsPreferences(requireContext().applicationContext)
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

        notificationPermission()

        foregroundServices()

        alarmDelay()
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
            settingsPreferences.getForegroundServiceStatus.collect {
                binding.swForegroundServices.isChecked = it
            }
        }

        binding.swForegroundServices.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                settingsPreferences.setForegroundService(isChecked)
            }
        }
    }

    private fun alarmDelay() {
        this.lifecycleScope.launch {
            settingsPreferences.getAlarmDelayMinutes.collect {
                binding.tvCountDown.text = it.toString()
            }
        }

        binding.btnDelayAlarm.setOnClickListener {
            val fragmentInput = FragmentInput().apply {
                onViewCreated = { inputBinding ->

                    inputBinding.toolBar.setNavigationOnClickListener {
                        requireActivity().supportFragmentManager.popBackStack()
                    }

                    inputBinding.toolBar.title = "Edit Alarm Delay"
                    inputBinding.tiFirst.hint = "Minutes"
                    inputBinding.ivYes.setImageResource(R.drawable.edit)
                    inputBinding.tvYes.text = "Edit"

                    inputBinding.etFirst.inputType = android.text.InputType.TYPE_CLASS_NUMBER

                    lifecycleScope.launch {
                        settingsPreferences.getAlarmDelayMinutes.collect {
                            inputBinding.etFirst.setText(it.toString())
                        }
                    }

                    inputBinding.btnYes.setOnClickListener {
                        val etFirst = inputBinding.etFirst.text.toString().toInt()
                        lifecycleScope.launch {
                            settingsPreferences.setAlarmDelay(etFirst)
                        }.invokeOnCompletion {
                            requireActivity().supportFragmentManager.popBackStack()
                        }
                    }
                }
            }
            replaceFragmentWithBackStack(R.id.mainBottomSheetContainer, fragmentInput, "settings")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().supportFragmentManager.popBackStack("settings", POP_BACK_STACK_INCLUSIVE)
        _binding = null
    }
}