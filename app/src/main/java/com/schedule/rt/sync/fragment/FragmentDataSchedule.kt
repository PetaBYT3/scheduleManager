package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterCourse
import com.schedule.rt.sync.adapter.AdapterTlDataSchedule
import com.schedule.rt.sync.databinding.FragmentDataScheduleBinding
import com.schedule.rt.sync.fragment.day.FragmentDataFriday
import com.schedule.rt.sync.fragment.day.FragmentDataMonday
import com.schedule.rt.sync.fragment.day.FragmentDataSaturday
import com.schedule.rt.sync.fragment.day.FragmentDataSunday
import com.schedule.rt.sync.fragment.day.FragmentDataThursday
import com.schedule.rt.sync.fragment.day.FragmentDataTuesday
import com.schedule.rt.sync.fragment.day.FragmentDataWednesday
import com.schedule.rt.sync.objectsingleton.DialogUtil.addFragmentWithoutBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.removeFragmentFromContainer
import com.schedule.rt.sync.objectsingleton.DialogUtil.removeTopFragmentAndShowPrevious
import com.schedule.rt.sync.objectsingleton.DialogUtil.showToastFragment
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelBuilding
import com.schedule.rt.sync.viewmodel.ViewModelClasses
import com.schedule.rt.sync.viewmodel.ViewModelCourse
import com.schedule.rt.sync.viewmodel.ViewModelData
import com.schedule.rt.sync.viewmodel.ViewModelLecturer
import com.schedule.rt.sync.viewmodel.ViewModelLevel
import com.schedule.rt.sync.viewmodel.ViewModelMajor
import com.schedule.rt.sync.viewmodel.ViewModelRoom
import com.schedule.rt.sync.viewmodel.ViewModelSchedule
import com.schedule.rt.sync.viewmodel.ViewModelUser
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FragmentDataSchedule(
    var btnAddSchedule: Boolean? = null,
    var btnEditSchedule: Boolean? = null
): Fragment() {

    private var _binding: FragmentDataScheduleBinding? = null
    private val binding get() = _binding!!

    private val vmUser: ViewModelUser by activityViewModels()
    private val vmData: ViewModelData by activityViewModels()
    private val vmBuilding: ViewModelBuilding by activityViewModels()
    private val vmRoom: ViewModelRoom by activityViewModels()
    private val vmSchedule: ViewModelSchedule by activityViewModels()
    private val vmCourse: ViewModelCourse by activityViewModels()
    private val vmLecturer: ViewModelLecturer by activityViewModels()
    private val vmMajor: ViewModelMajor by activityViewModels()
    private val vmLevel: ViewModelLevel by activityViewModels()
    private val vmClasses: ViewModelClasses by activityViewModels()

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
        _binding = FragmentDataScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vpDay.post {
            TransitionUtil.slideUpTransition(binding.vpDay)
        }

        val uidBuilding = vmSchedule.uidBuildingReference.value
        val uidRoom = vmSchedule.uidRoomReference.value

        vmBuilding.getBuildingByUid(uidBuilding).observe(viewLifecycleOwner) {
            val nameBuilding = it?.nameBuilding

            vmRoom.getRoomByUid(uidRoom).observe(viewLifecycleOwner) {
                val nameRoom = it?.nameRoom

                binding.clToolBar.title = buildString {
                    append("Building ")
                    append(nameBuilding)
                    append(", ")
                    append("Room ")
                    append(nameRoom)
                }
            }
        }

        binding.toolBar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        if (btnAddSchedule == true) {
            binding.btnAdd.visibility = View.VISIBLE
        } else {
            binding.btnAdd.visibility = View.GONE
        }

        if (btnEditSchedule == true) {
            binding.btnEditSchedule.visibility = View.VISIBLE
        } else {
            binding.btnEditSchedule.visibility = View.GONE
        }

        binding.btnAdd.setOnClickListener {
            addSchedule()
        }

        vmUser.getUser().observe(viewLifecycleOwner) { dataUser ->
            binding.btnEditSchedule.setOnClickListener {
                editSchedule(dataUser?.uidLecturer)
            }
        }

        tabLayout()
    }

    private fun tabLayout() {
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

        val adapterTlDataDay = AdapterTlDataSchedule(this, fragmentList)
        viewPager2.adapter = adapterTlDataDay

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

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val currentFragmentTag = adapterTlDataDay.getFragmentTag(position)
                vmSchedule.sendDay(currentFragmentTag)
            }
        })
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
            tvData3 = true,
            tvData5 = true,
            btnFirst = false,
            btnSecond = false,
            marginToTopItem = true,
            deleteScheduleByClasses = true,
            onNextClick = {
                val fragmentCard = FragmentCard().apply {
                    onViewCreated = { cardBinding ->

                        cardBinding.toolBar.setNavigationOnClickListener {
                            removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                        }

                        cardBinding.toolBar.title = "Delete Schedule"
                        cardBinding.ivYes.setImageResource(R.drawable.delete_schedule)
                        cardBinding.tvYes.text = "Delete Schedule"

                        val uidCourse = it.uidCourse.toString()
                        val uidMajor = it.uidMajor.toString()
                        val uidLevel = it.uidLevel.toString()
                        val uidClasses = it.uidClasses.toString()
                        val uidLecturer = it.uidLecturer.toString()

                        vmCourse.getCourseByUid(uidCourse).observe(viewLifecycleOwner) {
                            cardBinding.tvTitle.text = it?.nameCourse
                            cardBinding.tvData1.text = buildString {
                                append(it?.sksCourse)
                                append(" SKS")
                            }
                            cardBinding.tvData4.text = buildString {
                                append(it?.startTime)
                                append(" - ")
                                append(it?.endTime)
                            }
                        }

                        var nameMajor: String? = null
                        var nameLevel: String? = null
                        var nameClasses: String? = null

                        val majorFlow = vmMajor.getMajorByUid(uidMajor).asFlow()
                        val levelFlow = vmLevel.getLevelByUid(uidLevel).asFlow()
                        val classesFlow = vmClasses.getClassesByUid(uidClasses).asFlow()

                        lifecycleScope.launch {
                            combine(
                                majorFlow,
                                levelFlow,
                                classesFlow
                            ) { major, level, classes ->
                                nameMajor = major?.nameMajor
                                nameLevel = level?.level
                                nameClasses = classes?.nameClasses
                            }.collect {
                                if (nameMajor != null && nameLevel != null && nameClasses != null) {
                                    cardBinding.tvData2.text = buildString {
                                        append("$nameMajor, ")
                                        append("Level $nameLevel, ")
                                        append("Class $nameClasses")
                                    }
                                }
                            }
                        }

                        vmLecturer.getLecturerByUid(uidLecturer).observe(viewLifecycleOwner) {
                            cardBinding.tvData3.text = it?.nameLecturer
                        }

                        cardBinding.btnYes.setOnClickListener {
                            vmSchedule.deleteSchedule(uidCourse).observe(viewLifecycleOwner) {
                                when (it) {
                                    "Success" -> {
                                        showToastFragment(FragmentToast(R.drawable.check, "Schedule Deleted"))
                                        removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                                    }
                                    "Fail" -> {
                                        showToastFragment(FragmentToast(R.drawable.fail, "Delete Failed"))
                                    }
                                }
                            }
                        }
                    }
                }
                addFragmentWithoutBackStack(fragmentCard)
            }
        )

        recyclerView.adapter = adapter

        vmSchedule.getSchedule(day).observe(viewLifecycleOwner) {
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

    private fun addSchedule() {
        val fragmentCourse = FragmentSelectCourse().apply {
            onViewCreated = { selectCourse ->

                selectCourse.toolBar.setNavigationOnClickListener {
                    removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                }

                val uidLevel = vmClasses.uidLevelReference.value
                val uidClasses = vmCourse.uidClassesReference.value
                vmLevel.getLevelByUid(uidLevel).observe(viewLifecycleOwner) {
                    val nameLevel = it?.level
                    vmClasses.getClassesByUid(uidClasses).observe(viewLifecycleOwner) {
                        val nameClasses = it?.nameClasses

                        selectCourse.toolBar.title = buildString {
                            append("Level $nameLevel, ")
                            append("Class $nameClasses")
                        }
                    }
                }

                rvCourseByClasses()

            }
            onAddClick = {
                val uidCourse = it.uidCourse
                val uidMajor = it.uidMajor
                val uidLevel = it.uidLevel
                val uidClasses = it.uidClasses
                val uidLecturer = it.uidLecturer
                val uidBuilding = it.uidBuilding
                val uidRoom = it.uidRoom
                val startTime = it.startTime
                val endTime = it.endTime
                vmData.sendUidLecturer(uidLecturer)

                if (uidBuilding != null && uidRoom != null && startTime != null && endTime != null) {
                    val fragmentCard = FragmentCard().apply {
                        onViewCreated = { cardBinding ->
                            cardBinding.toolBar.setNavigationOnClickListener {
                                removeTopFragmentAndShowPrevious()
                            }

                            cardBinding.layoutMessage.visibility = View.VISIBLE
                            cardBinding.toolBar.title = "Reschedule"
                            cardBinding.ivYes.setImageResource(R.drawable.next)
                            cardBinding.tvYes.text = "Yes, Reschedule"

                            vmCourse.getCourseByUid(uidCourse.toString()).observe(viewLifecycleOwner) {
                                cardBinding.tvTitle.text = it?.nameCourse
                                cardBinding.tvData1.text = buildString {
                                    append(it?.sksCourse)
                                    append(" SKS")
                                }
                                cardBinding.tvData5.text = buildString {
                                    append(it?.startTime)
                                    append(" - ")
                                    append(it?.endTime)
                                }
                            }

                            var nameMajor: String? = null
                            var nameLevel: String? = null
                            var nameClasses: String? = null

                            val majorFlow = vmMajor.getMajorByUid(uidMajor).asFlow()
                            val levelFlow = vmLevel.getLevelByUid(uidLevel).asFlow()
                            val classesFlow = vmClasses.getClassesByUid(uidClasses).asFlow()

                            lifecycleScope.launch {
                                combine(
                                    majorFlow,
                                    levelFlow,
                                    classesFlow
                                ) { major, level, classes ->
                                    nameMajor = major?.nameMajor
                                    nameLevel = level?.level
                                    nameClasses = classes?.nameClasses
                                }.collect {
                                    if (nameMajor != null && nameLevel != null && nameClasses != null) {
                                        cardBinding.tvData2.text = buildString {
                                            append("$nameMajor, ")
                                            append("Level $nameLevel, ")
                                            append("Class $nameClasses")
                                        }
                                    }
                                }
                            }

                            vmLecturer.getLecturerByUid(uidLecturer).observe(viewLifecycleOwner) {
                                cardBinding.tvData3.text = buildString {
                                    append(it?.nameLecturer)
                                }
                            }

                            var nameBuilding: String? = null
                            var nameRoom: String? = null

                            val flowBuilding = vmBuilding.getBuildingByUid(uidBuilding).asFlow()
                            val flowRoom = vmRoom.getRoomByUid(uidRoom).asFlow()

                            lifecycleScope.launch {
                                combine(
                                    flowBuilding,
                                    flowRoom
                                ) { building, room ->
                                    nameBuilding = building?.nameBuilding
                                    nameRoom = room?.nameRoom
                                }.collect {
                                    if (nameBuilding != null && nameRoom != null) {
                                        cardBinding.tvData4.text = buildString {
                                            append("Building $nameBuilding, ")
                                            append("Room $nameRoom")
                                        }
                                    }
                                }
                            }

                            cardBinding.tvMessage.text = buildString {
                                append("This Course Already Have A Schedule, ")
                                append("Do You Want To Reschedule This Course?")

                            }

                            cardBinding.btnYes.setOnClickListener {
                                vmCourse.sendCourseUid(uidCourse)
                                vmLecturer.sendLecturerUid(uidLecturer)
                                val fragmentInputSchedule = FragmentInputSchedule()
                                addFragmentWithoutBackStack(fragmentInputSchedule)
                            }
                        }
                    }
                    addFragmentWithoutBackStack(fragmentCard)
                } else if (uidLecturer == null) {
                    showToastFragment(FragmentToast(R.drawable.fail, "This Course Does Not Have Lecturer"))
                } else {
                    vmCourse.sendCourseUid(uidCourse)
                    vmLecturer.sendLecturerUid(uidLecturer)
                    val fragmentInputSchedule = FragmentInputSchedule()
                    addFragmentWithoutBackStack(fragmentInputSchedule)
                }
            }
        }
        addFragmentWithoutBackStack(fragmentCourse)
    }

    private fun editSchedule(uidLecturer: String?) {
        val fragmentCourse = FragmentSelectCourse().apply {
            onViewCreated = { selectCourse ->
                selectCourse.toolBar.setNavigationOnClickListener {
                    removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                }

                rvCourseByLecturer(uidLecturer)

            }
            onAddClick = {
                val uidCourse = it.uidCourse
                val uidMajor = it.uidMajor
                val uidLevel = it.uidLevel
                val uidClasses = it.uidClasses
                val uidLecturer = it.uidLecturer
                val uidBuilding = it.uidBuilding
                val uidRoom = it.uidRoom
                val startTime = it.startTime
                val endTime = it.endTime
                vmData.sendUidLecturer(uidLecturer)

                if (uidBuilding != null && uidRoom != null && startTime != null && endTime != null) {
                    val fragmentCard = FragmentCard().apply {
                        onViewCreated = { cardBinding ->
                            cardBinding.toolBar.setNavigationOnClickListener {
                                removeTopFragmentAndShowPrevious()
                            }

                            cardBinding.layoutMessage.visibility = View.VISIBLE
                            cardBinding.toolBar.title = "Reschedule"
                            cardBinding.ivYes.setImageResource(R.drawable.next)
                            cardBinding.tvYes.text = "Yes, Reschedule"

                            vmCourse.getCourseByUid(uidCourse.toString()).observe(viewLifecycleOwner) {
                                cardBinding.tvTitle.text = it?.nameCourse
                                cardBinding.tvData1.text = buildString {
                                    append(it?.sksCourse)
                                    append(" SKS")
                                }
                                cardBinding.tvData5.text = buildString {
                                    append(it?.startTime)
                                    append(" : ")
                                    append(it?.endTime)
                                }
                            }

                            var nameMajor: String? = null
                            var nameLevel: String? = null
                            var nameClasses: String? = null

                            val majorFlow = vmMajor.getMajorByUid(uidMajor).asFlow()
                            val levelFlow = vmLevel.getLevelByUid(uidLevel).asFlow()
                            val classesFlow = vmClasses.getClassesByUid(uidClasses).asFlow()

                            lifecycleScope.launch {
                                combine(
                                    majorFlow,
                                    levelFlow,
                                    classesFlow
                                ) { major, level, classes ->
                                    nameMajor = major?.nameMajor
                                    nameLevel = level?.level
                                    nameClasses = classes?.nameClasses
                                }.collect {
                                    if (nameMajor != null && nameLevel != null && nameClasses != null) {
                                        cardBinding.tvData2.text = buildString {
                                            append("$nameMajor, ")
                                            append("Level $nameLevel, ")
                                            append("Class $nameClasses")
                                        }
                                    }
                                }
                            }

                            vmLecturer.getLecturerByUid(uidLecturer).observe(viewLifecycleOwner) {
                                cardBinding.tvData3.text = buildString {
                                    append(it?.nameLecturer)
                                }
                            }

                            var nameBuilding: String? = null
                            var nameRoom: String? = null

                            val flowBuilding = vmBuilding.getBuildingByUid(uidBuilding).asFlow()
                            val flowRoom = vmRoom.getRoomByUid(uidRoom).asFlow()

                            lifecycleScope.launch {
                                combine(
                                    flowBuilding,
                                    flowRoom
                                ) { building, room ->
                                    nameBuilding = building?.nameBuilding
                                    nameRoom = room?.nameRoom
                                }.collect {
                                    if (nameBuilding != null && nameRoom != null) {
                                        cardBinding.tvData4.text = buildString {
                                            append("Building $nameBuilding, ")
                                            append("Room $nameRoom")
                                        }
                                    }
                                }
                            }

                            cardBinding.tvMessage.text = buildString {
                                append("This Course Already Have A Schedule, ")
                                append("Do You Want To Reschedule This Course?")

                            }

                            cardBinding.btnYes.setOnClickListener {
                                vmCourse.sendCourseUid(uidCourse)
                                vmLecturer.sendLecturerUid(uidLecturer)
                                val fragmentInputSchedule = FragmentInputSchedule()
                                addFragmentWithoutBackStack(fragmentInputSchedule)
                            }
                        }
                    }
                    addFragmentWithoutBackStack(fragmentCard)
                } else if (uidLecturer == null) {
                    showToastFragment(FragmentToast(R.drawable.fail, "This Course Does Not Have Lecturer"))
                } else {
                    vmCourse.sendCourseUid(uidCourse)
                    vmLecturer.sendLecturerUid(uidLecturer)
                    val fragmentInputSchedule = FragmentInputSchedule()
                    addFragmentWithoutBackStack(fragmentInputSchedule)
                }
            }
        }
        addFragmentWithoutBackStack(fragmentCourse)
    }

    override fun onDestroyView() {
        removeFragmentFromContainer(R.id.mainBottomSheetContainer)
        super.onDestroyView()
        _binding = null
    }
}