package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterCourse
import com.schedule.rt.sync.adapter.AdapterTlDataSchedule
import com.schedule.rt.sync.databinding.FragmentLecturerScheduleBinding
import com.schedule.rt.sync.fragment.day.FragmentDataFriday
import com.schedule.rt.sync.fragment.day.FragmentDataMonday
import com.schedule.rt.sync.fragment.day.FragmentDataSaturday
import com.schedule.rt.sync.fragment.day.FragmentDataSunday
import com.schedule.rt.sync.fragment.day.FragmentDataThursday
import com.schedule.rt.sync.fragment.day.FragmentDataTuesday
import com.schedule.rt.sync.fragment.day.FragmentDataWednesday
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelBuilding
import com.schedule.rt.sync.viewmodel.ViewModelClasses
import com.schedule.rt.sync.viewmodel.ViewModelCourse
import com.schedule.rt.sync.viewmodel.ViewModelLecturer
import com.schedule.rt.sync.viewmodel.ViewModelLevel
import com.schedule.rt.sync.viewmodel.ViewModelMajor
import com.schedule.rt.sync.viewmodel.ViewModelRoom
import com.schedule.rt.sync.viewmodel.ViewModelSchedule
import com.schedule.rt.sync.viewmodel.ViewModelUser

class FragmentLecturerSchedule : Fragment() {

    private var _binding : FragmentLecturerScheduleBinding? = null
    private val binding get() = _binding!!

    private val vmCourse: ViewModelCourse by activityViewModels()
    private val vmUser: ViewModelUser by activityViewModels()
    private val vmSchedule: ViewModelSchedule by activityViewModels()
    private val vmLecturer: ViewModelLecturer by activityViewModels()
    private val vmMajor: ViewModelMajor by activityViewModels()
    private val vmLevel: ViewModelLevel by activityViewModels()
    private val vmClasses: ViewModelClasses by activityViewModels()
    private val vmBuilding: ViewModelBuilding by activityViewModels()
    private val vmRoom: ViewModelRoom by activityViewModels()

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
        _binding = FragmentLecturerScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vpDay.post {
            TransitionUtil.slideUpTransition(binding.vpDay)
        }

        binding.toolBar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.btnEditSchedule.setOnClickListener {
            replaceFragmentWithBackStack(R.id.mainFragmentContainer, FragmentBuilding(btnEditSchedule = true), null)
        }

        viewPager2()

        vmCourse.uidClassesReference(null)
    }

    private fun viewPager2() {
        val tabLayout: TabLayout = binding.tlDay
        val viewPager2: ViewPager2 = binding.vpDay

        val fragmentList = listOf(
            Pair("monday", FragmentDataMonday().apply {
                onViewCreated = { mondayBinding ->
                    recyclerView(
                        mondayBinding.rvMonday,
                        "monday",
                        mondayBinding.pbRv,
                        mondayBinding.layoutNoData
                    )
                }
            }),
            Pair("tuesday", FragmentDataTuesday().apply {
                onViewCreated = { tuesdayBinding ->
                    recyclerView(
                        tuesdayBinding.rvTuesday,
                        "tuesday",
                        tuesdayBinding.pbRv,
                        tuesdayBinding.layoutNoData
                    )
                }
            }),
            Pair("wednesday", FragmentDataWednesday().apply {
                onViewCreated = { wednesdayBinding ->
                    recyclerView(
                        wednesdayBinding.rvWednesday,
                        "wednesday",
                        wednesdayBinding.pbRv,
                        wednesdayBinding.layoutNoData
                    )
                }
            }),
            Pair("thursday", FragmentDataThursday().apply {
                onViewCreated = { thursdayBinding ->
                    recyclerView(
                        thursdayBinding.rvThursday,
                        "thursday",
                        thursdayBinding.pbRv,
                        thursdayBinding.layoutNoData
                    )
                }
            }),
            Pair("friday", FragmentDataFriday().apply {
                onViewCreated = { fridayBinding ->
                    recyclerView(
                        fridayBinding.rvFriday,
                        "friday",
                        fridayBinding.pbRv,
                        fridayBinding.layoutNoData
                    )
                }
            }),
            Pair("saturday", FragmentDataSaturday().apply {
                onViewCreated = { saturdayBinding ->
                    recyclerView(
                        saturdayBinding.rvSaturday,
                        "saturday",
                        saturdayBinding.pbRv,
                        saturdayBinding.layoutNoData
                    )
                }
            }),
            Pair("sunday", FragmentDataSunday().apply {
                onViewCreated = { sundayBinding ->
                    recyclerView(
                        sundayBinding.rvSunday,
                        "sunday",
                        sundayBinding.pbRv,
                        sundayBinding.layoutNoData
                    )
                }
            })
        )

        val adapter = AdapterTlDataSchedule(this, fragmentList)
        viewPager2.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
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

    private fun recyclerView(
        recyclerView: RecyclerView,
        day: String,
        progressBar: View,
        layoutNoData: View
    ) {
        val recyclerView: RecyclerView = recyclerView
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
            marginToTopItem = true
        )

        recyclerView.adapter = adapter

        vmUser.getUser().observe(viewLifecycleOwner) {
            val uidLecturer = it?.uidLecturer.toString()

            vmSchedule.getScheduleForLecturer(uidLecturer, day).observe(viewLifecycleOwner) {
                adapter.updateData(it)
                if (it.isNullOrEmpty()) {
                    progressBar.visibility = View.GONE
                    layoutNoData.visibility = View.VISIBLE
                } else {
                    progressBar.visibility = View.GONE
                    layoutNoData.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}