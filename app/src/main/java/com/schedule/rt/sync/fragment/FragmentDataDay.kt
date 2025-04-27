package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterCourse
import com.schedule.rt.sync.adapter.AdapterTlDataDay
import com.schedule.rt.sync.databinding.FragmentDataDayBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelAdministrator
import com.schedule.rt.sync.viewmodel.ViewModelBuilding
import com.schedule.rt.sync.viewmodel.ViewModelCourse
import com.schedule.rt.sync.viewmodel.ViewModelRoom
import com.schedule.rt.sync.viewmodel.ViewModelSchedule

class FragmentDataDay : Fragment() {

    private lateinit var binding: FragmentDataDayBinding

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    private val viewModelAdministrator: ViewModelAdministrator by activityViewModels()

    private val vmBuilding: ViewModelBuilding by activityViewModels()
    private val vmRoom: ViewModelRoom by activityViewModels()
    private val vmSchedule: ViewModelSchedule by activityViewModels()

    private val vmCourse: ViewModelCourse by activityViewModels()

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
        binding = FragmentDataDayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vpDay.post {
            TransitionUtil.slideUpTransition(binding.vpDay)
        }

        actionBar()

        tabLayout()
    }

    private fun actionBar() {
        val uidBuilding = vmRoom.uidBuilding
        val uidRoom = vmRoom.uidRoom

        vmBuilding.getBuildingByUid(uidBuilding).observe(viewLifecycleOwner) {
            val nameBuilding = it?.nameBuilding

            vmRoom.getRoomByUid(uidRoom).observe(viewLifecycleOwner) {
                val nameRoom = it?.nameRoom

                binding.clToolBar.title = buildString {
                    append("Building ")
                    append(nameBuilding)
                    append(" > ")
                    append("Room ")
                    append(nameRoom)
                }
            }
        }

        binding.toolBar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.btnAdd.setOnClickListener {
            addSchedule()
        }
    }

    private fun tabLayout() {
        tabLayout = binding.tlDay
        viewPager = binding.vpDay

        val adapterTlDataDay = AdapterTlDataDay(this)
        viewPager.adapter = adapterTlDataDay

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "M"
                1 -> "T"
                2 -> "W"
                3 -> "T"
                4 -> "F"
                5 -> "S"
                6 -> "S"
                else -> "Tab ${position + 1}"
            }
        }.attach()
    }

    private fun addSchedule() {
        val adapterTlDataDay = AdapterTlDataDay(this)
        val currentFragmentTag = adapterTlDataDay.getFragmentTag(viewPager.currentItem)
        vmSchedule.day = currentFragmentTag

        val bottomSheetCourse = BottomSheetCourse()
        bottomSheetCourse.show(parentFragmentManager, "BottomSheetCourse")
        bottomSheetCourse.setOnClickListener(object : BottomSheetCourse.onClickListener {
            override fun onAddClick(
                position: Int,
                adapterRvCourse: AdapterCourse
            ) {
                bottomSheetCourse.dismiss()
                DialogUtil.showBottomSheetSchedule(
                    requireActivity(),
                    "Add Schedule",
                    R.drawable.add,
                    "Add",
                    { bottomSheetBinding, bottomSheet ->

                        val uidCourse = adapterRvCourse.dataClassCourse[position].uidCourse.toString()
                        val uidLecturer = adapterRvCourse.dataClassCourse[position].uidLecturer.toString()

                        vmCourse.getCourseByUid(uidCourse).observe(viewLifecycleOwner) {
                            bottomSheetBinding.tvTittle.text = it?.nameCourse
                            bottomSheetBinding.tvData1.text = it?.sksCourse

                            bottomSheetBinding.tpStart.setOnTimeChangedListener { _, hour, minute ->
                                val startTime = String.format("%02d:%02d", hour, minute)



                                val sks = it?.sksCourse?.toInt()
                                val duration = sks?.times(45)
                                val endDuration = hour * 60 + minute + duration!!
                                val endHour = endDuration / 60
                                val endMinute = endDuration % 60

                                val endTime = String.format("%02d:%02d", endHour, endMinute)

                                bottomSheetBinding.tvData2.text = buildString {
                                    append(startTime)
                                    append(" Until ")
                                    append(endTime)
                                }

                                vmSchedule.startTime = startTime
                                vmSchedule.endTime = endTime
                            }
                        }

                        bottomSheetBinding.btnYes.setOnClickListener {
                            vmSchedule.addSchedule(uidCourse, uidLecturer).observe(viewLifecycleOwner) {
                                when (it) {
                                    "Success" -> {
                                        DialogUtil.showToast(requireActivity(), "Success", R.drawable.check)
                                        bottomSheet.dismiss()
                                    }
                                    "Fail" -> {
                                        DialogUtil.showToast(requireActivity(), "Fail", R.drawable.fail)
                                    }
                                    "ScheduleConflict" -> {
                                        DialogUtil.showToast(requireActivity(), "Schedule Conflict", R.drawable.fail)
                                    }
                                    "LecturerConflict" -> {
                                        DialogUtil.showToast(requireActivity(), "Lecturer Conflict", R.drawable.fail)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        })
    }
}