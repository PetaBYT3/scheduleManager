package com.schedule.rt.sync.fragment

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.activity.ActivityMain
import com.schedule.rt.sync.adapter.AdapterCourse
import com.schedule.rt.sync.databinding.FragmentHomeBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.removeFragmentFromContainer
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.service.PermissionManager
import com.schedule.rt.sync.userpreferences.SettingsPreferences
import com.schedule.rt.sync.viewmodel.ViewModelBuilding
import com.schedule.rt.sync.viewmodel.ViewModelClasses
import com.schedule.rt.sync.viewmodel.ViewModelCourse
import com.schedule.rt.sync.viewmodel.ViewModelLecturer
import com.schedule.rt.sync.viewmodel.ViewModelLevel
import com.schedule.rt.sync.viewmodel.ViewModelMajor
import com.schedule.rt.sync.viewmodel.ViewModelRoom
import com.schedule.rt.sync.viewmodel.ViewModelSchedule
import com.schedule.rt.sync.viewmodel.ViewModelUser

class FragmentHome : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val vmCourse: ViewModelCourse by activityViewModels()
    private val vmUser: ViewModelUser by activityViewModels()
    private val vmLecturer: ViewModelLecturer by activityViewModels()
    private val vmMajor: ViewModelMajor by activityViewModels()
    private val vmLevel: ViewModelLevel by activityViewModels()
    private val vmClasses: ViewModelClasses by activityViewModels()
    private val vmSchedule: ViewModelSchedule by activityViewModels()
    private val vmBuilding: ViewModelBuilding by activityViewModels()
    private val vmRoom: ViewModelRoom by activityViewModels()

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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        settingsPreferences = SettingsPreferences(requireContext().applicationContext)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        vmUser.getUser().observe(viewLifecycleOwner) { dataUser ->
            if (dataUser?.uidLecturer == null) {
                student()
            } else {
                lecturer()
            }
        }

        binding.toolBar.setNavigationOnClickListener {
            (requireActivity() as ActivityMain).btnDrawer()
        }

        val permissionManager = PermissionManager(
            this,
            onAllGranted = {
            },
            onDenied = {
            }
        )

        val permissionToRequest = mutableListOf(
            Manifest.permission.INTERNET,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissionToRequest.add(Manifest.permission.FOREGROUND_SERVICE)
            permissionToRequest.add(Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC)
        }

        permissionManager.requestPermissions(permissionToRequest)
    }

    private fun student() {
        vmUser.getUser().observe(viewLifecycleOwner) {
            binding.tvTitle.text = it?.nameUser ?: "-"

            val uidMajor = it?.uidMajor.toString()
            val uidLevel = it?.uidLevel.toString()
            val uidClasses = it?.uidClasses.toString()

            vmMajor.getMajorByUid(uidMajor).observe(viewLifecycleOwner) {
                binding.tvData1.text = it?.nameMajor ?: "-"
            }

            vmLevel.getLevelByUid(uidLevel).observe(viewLifecycleOwner) {
                binding.tvData2.text = it?.level ?: "-"
            }

            vmClasses.getClassesByUid(uidClasses).observe(viewLifecycleOwner) {
                binding.tvData3.text = it?.nameClasses ?: "-"
            }

            val recyclerView: RecyclerView = binding.todaySchedule
            val adapter = AdapterCourse(
                vmLecturer,
                vmMajor,
                vmLevel,
                vmClasses,
                vmCourse ,
                vmBuilding,
                vmRoom,
                viewLifecycleOwner,
                tvData1 = true,
                tvData3 = true,
                tvData4 = true,
                tvData5 = true,
                btnFirst = false,
                btnSecond = false,
                settingsPreferences = settingsPreferences,
                onCountDown = true,
            )

            recyclerView.adapter = adapter

            vmSchedule.getCurrentDay(requireActivity()).observe(viewLifecycleOwner) {
                vmSchedule.getScheduleForStudent(uidClasses, it).observe(viewLifecycleOwner) {
                    adapter.updateData(it)
                    if (it.isNullOrEmpty()) {
                        binding.pbRv.visibility = View.GONE
                        binding.layoutNoData.visibility = View.VISIBLE
                    } else {
                        binding.pbRv.visibility = View.GONE
                        binding.layoutNoData.visibility = View.GONE
                    }
                }
            }

            binding.btnAllSchedule.setOnClickListener {
                replaceFragmentWithBackStack(R.id.mainFragmentContainer, FragmentStudentSchedule(), null)
            }
        }
    }

    private fun lecturer() {
        vmUser.getUser().observe(viewLifecycleOwner) {
            val uidLecturer = it?.uidLecturer

            vmLecturer.getLecturerByUid(uidLecturer).observe(viewLifecycleOwner) {
                binding.tvTitle.text = it?.nameLecturer ?: "-"

                binding.tvData1.text = it?.nikLecturer ?: "-"

                val administratorAccess = it?.administratorAccess.toString()
                if (administratorAccess == "True") {
                    binding.tvData2.text = "Administrator"
                } else {
                    binding.tvData2.text = "Lecturer"
                }

                val uidMajor = it?.uidMajorManager
                vmMajor.getMajorByUid(uidMajor).observe(viewLifecycleOwner) {
                    if (it != null) {
                        binding.tvData3.text = buildString {
                            append(it.nameMajor)
                            append(" Manager")
                        }
                    } else {
                        binding.tvData3.text = "Not managing any major"
                    }
                }
            }

            val recyclerView: RecyclerView = binding.todaySchedule
            val adapter = AdapterCourse(
                vmLecturer,
                vmMajor,
                vmLevel,
                vmClasses,
                vmCourse,
                vmBuilding,
                vmRoom,
                viewLifecycleOwner,
                tvData1 = true,
                tvData2 = true,
                tvData3 = false,
                tvData4 = true,
                tvData5 = true,
                btnFirst = false,
                btnSecond = false,
                settingsPreferences = settingsPreferences,
                onCountDown = true
            )
            recyclerView.adapter = adapter

            vmSchedule.getCurrentDay(requireActivity()).observe(viewLifecycleOwner) {
                vmSchedule.getScheduleForLecturer(uidLecturer, it).observe(viewLifecycleOwner) {
                    adapter.updateData(it)
                    if (it.isNullOrEmpty()) {
                        binding.pbRv.visibility = View.GONE
                        binding.layoutNoData.visibility = View.VISIBLE
                    } else {
                        binding.pbRv.visibility = View.GONE
                        binding.layoutNoData.visibility = View.GONE
                    }
                }
            }

            binding.btnAllSchedule.setOnClickListener {
                replaceFragmentWithBackStack(R.id.mainFragmentContainer, FragmentLecturerSchedule(), null)
            }
        }
    }

    override fun onDestroyView() {
        removeFragmentFromContainer(R.id.mainBottomSheetContainer)
        super.onDestroyView()
        _binding = null
    }
}