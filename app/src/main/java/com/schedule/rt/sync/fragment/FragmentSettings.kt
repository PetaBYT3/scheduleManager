package com.schedule.rt.sync.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.lifecycle.lifecycleScope
import com.schedule.rt.sync.R
import com.schedule.rt.sync.activity.ActivityMain
import com.schedule.rt.sync.databinding.FragmentSettingsBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.showToastFragment
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.userpreferences.SettingsPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentSettings : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var settingsPreferences: SettingsPreferences

    private val fragmentTag = "settings"

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

        binding.btnNotificationPermission.setOnClickListener {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            startActivity(intent)
        }

        foregroundServices()

        alarmDelay()

        binding.btnBatteryOptimization.setOnClickListener {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = "package:${requireContext().packageName}".toUri()
            startActivity(intent)
        }

        updateUi()
    }

    private fun updateUi() {
        val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
        } else {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_NOTIFICATION_POLICY)
        }
        if (notificationPermission == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            binding.ivNotificationPermission.setImageResource(R.drawable.check)
        } else {
            binding.ivNotificationPermission.setImageResource(R.drawable.close)
        }

        val powerManager = requireActivity().getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoring = powerManager.isIgnoringBatteryOptimizations(requireContext().packageName)
        if (isIgnoring) {
            binding.ivBatteryOptimization.setImageResource(R.drawable.check)
        } else {
            binding.ivBatteryOptimization.setImageResource(R.drawable.fail)
        }
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
                        if (etFirst > 120) {
                            showToastFragment(FragmentToast(R.drawable.fail, "Alarm delay cant be more than 120 minutes"))
                        } else {
                            lifecycleScope.launch {
                                settingsPreferences.setAlarmDelay(etFirst)
                            }.invokeOnCompletion {
                                requireActivity().supportFragmentManager.popBackStack()
                            }
                        }
                    }
                }
            }
            requireActivity().supportFragmentManager.popBackStack(fragmentTag, POP_BACK_STACK_INCLUSIVE)
            replaceFragmentWithBackStack(R.id.mainBottomSheetContainer, fragmentInput, fragmentTag)
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            delay(1000)
            updateUi()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().supportFragmentManager.popBackStack(fragmentTag, POP_BACK_STACK_INCLUSIVE)
        _binding = null
    }
}