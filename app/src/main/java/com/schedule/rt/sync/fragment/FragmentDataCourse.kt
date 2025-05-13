package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterCourse
import com.schedule.rt.sync.databinding.FragmentDataCourseBinding
import com.schedule.rt.sync.dataclass.DataClassBuilding
import com.schedule.rt.sync.dataclass.DataClassClasses
import com.schedule.rt.sync.dataclass.DataClassCourse
import com.schedule.rt.sync.dataclass.DataClassLevel
import com.schedule.rt.sync.dataclass.DataClassMajor
import com.schedule.rt.sync.dataclass.DataClassRoom
import com.schedule.rt.sync.function.capitalizeAfterDot
import com.schedule.rt.sync.function.capitalizeEachWord
import com.schedule.rt.sync.objectsingleton.DialogUtil.addFragmentWithoutBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.hideKeyboard
import com.schedule.rt.sync.objectsingleton.DialogUtil.removeFragmentFromContainer
import com.schedule.rt.sync.objectsingleton.DialogUtil.removeTopFragmentAndShowPrevious
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FragmentDataCourse : Fragment() {

    private var _binding: FragmentDataCourseBinding? = null
    private val binding get() = _binding!!

    private val vmSchedule: ViewModelSchedule by activityViewModels()
    private val vmData: ViewModelData by activityViewModels()
    private val vmLecturer: ViewModelLecturer by activityViewModels()
    private val vmMajor: ViewModelMajor by activityViewModels()
    private val vmLevel: ViewModelLevel by activityViewModels()
    private val vmClasses: ViewModelClasses by activityViewModels()
    private val vmCourse: ViewModelCourse by activityViewModels()
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
        _binding = FragmentDataCourseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        val uidLevel = vmClasses.uidLevelReference.value
        val uidClasses = vmCourse.uidClassesReference.value
        vmLevel.getLevelByUid(uidLevel).observe(viewLifecycleOwner) {
            val nameLevel = it?.level
            vmClasses.getClassesByUid(uidClasses).observe(viewLifecycleOwner) {
                val nameClasses = it?.nameClasses

                binding.clToolBar.title = buildString {
                    append("Level $nameLevel, ")
                    append("Class $nameClasses")
                }
            }
        }

        binding.toolBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnEditSchedule.setOnClickListener {
            replaceFragmentWithBackStack(R.id.mainFragmentContainer, FragmentBuilding(btnAddSchedule = true), "mainContainer")
        }

        binding.btnAdd.setOnClickListener {
            addCourse()
        }

        recyclerView()
    }

    private fun recyclerView() {
        val recyclerView: RecyclerView = binding.rvCourse
        val adapter = AdapterCourse(
            vmLecturer, vmMajor, vmLevel, vmClasses, vmCourse, vmBuilding, vmRoom, viewLifecycleOwner,
            tvData1 = true,
            tvData3 = true,
            tvData4 = true,
            tvData5 = true,
            deleteSchedule = true,
            btnFirst = true,
            btnSecond = true,
            onFirstClick = {
                val uidCourse = it.uidCourse
                val day = it.day
                val building = it.uidBuilding
                val room = it.uidRoom
                val roomDay = it.uidRoomDay
                val startTime = it.startTime
                val endTime = it.endTime
                val uidLecturer = it.uidLecturer

                if (day != null && building != null && room != null && roomDay != null && startTime != null && endTime != null) {
                    val fragmentCard = FragmentCard().apply {
                        onViewCreated = { cardBinding ->

                            cardBinding.toolBar.setNavigationOnClickListener {
                                removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                            }

                            cardBinding.layoutMessage.visibility = View.VISIBLE

                            cardBinding.toolBar.title = "Course Already Have Schedule"
                            cardBinding.ivYes.setImageResource(R.drawable.close)
                            cardBinding.tvYes.text = "Ok"
                            cardBinding.tvMessage.text = "This Course Already Have A Schedule, Delete Schedule First To Edit This Coure"

                            vmCourse.getCourseByUid(uidCourse.toString()).observe(viewLifecycleOwner) {
                                cardBinding.tvTitle.text = buildString {
                                    append(it?.nameCourse)
                                }
                                cardBinding.tvData1.text = buildString {
                                    append(it?.sksCourse)
                                    append(" SKS")
                                }

                                val getMajor = vmMajor.getMajorByUid(it?.uidMajor.toString())
                                val getLevel = vmLevel.getLevelByUid(it?.uidLevel.toString())
                                val getClasses = vmClasses.getClassesByUid(it?.uidClasses.toString())
                                val combinedMajor = MediatorLiveData<Triple<DataClassMajor?, DataClassLevel?, DataClassClasses?>>()

                                fun dataMajorLevelClass() {
                                    val dataMajor = getMajor.value
                                    val dataLevel = getLevel.value
                                    val dataClasses = getClasses.value
                                    combinedMajor.value = Triple(dataMajor, dataLevel, dataClasses)
                                }

                                combinedMajor.addSource(getMajor) { dataMajorLevelClass() }
                                combinedMajor.addSource(getLevel) { dataMajorLevelClass() }
                                combinedMajor.addSource(getClasses) { dataMajorLevelClass() }

                                combinedMajor.observe(viewLifecycleOwner) { (dataMajor, dataLevel, dataClasses) ->
                                    cardBinding.tvData2.text = buildString {
                                        append("${dataMajor?.nameMajor}, ")
                                        append("Level ${dataLevel?.level}, ")
                                        append("Class ${dataClasses?.nameClasses}")
                                    }
                                }

                                val uidLecturer = it?.uidLecturer.toString()
                                vmLecturer.getLecturerByUid(uidLecturer).observe(viewLifecycleOwner) {
                                    cardBinding.tvData3.text = buildString {
                                        append(it?.nameLecturer)
                                    }
                                }

                                val getBuilding = vmBuilding.getBuildingByUid(it?.uidBuilding.toString())
                                val getRoom = vmRoom.getRoomByUid(it?.uidRoom.toString())
                                val combinedBuilding = MediatorLiveData<Pair<DataClassBuilding?, DataClassRoom?>>()

                                fun dataBuildingRoom() {
                                    val dataBuilding = getBuilding.value
                                    val dataRoom = getRoom.value
                                    combinedBuilding.value = Pair(dataBuilding, dataRoom)
                                }

                                combinedBuilding.addSource(getBuilding) { dataBuildingRoom() }
                                combinedBuilding.addSource(getRoom) { dataBuildingRoom() }

                                combinedBuilding.observe(viewLifecycleOwner) { (dataBuilding, dataRoom) ->
                                    if (dataBuilding != null && dataRoom != null) {
                                        cardBinding.tvData4.text = buildString {
                                            append("Building ${dataBuilding?.nameBuilding}, ")
                                            append("Room ${dataRoom?.nameRoom}")
                                        }
                                    }
                                }

                                if (it?.startTime != null && it.endTime != null) {
                                    cardBinding.tvData5.text = buildString {
                                        append(it.startTime)
                                        append(" - ")
                                        append(it.endTime)
                                    }
                                }
                            }

                            cardBinding.btnYes.setOnClickListener {
                                removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                            }
                        }
                    }
                    addFragmentWithoutBackStack(fragmentCard)
                } else {
                    val fragmentInput = FragmentInput().apply {
                        onViewCreated = { inputBinding ->

                            inputBinding.toolBar.setNavigationOnClickListener {
                                removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                            }

                            inputBinding.tiSecond.visibility = View.VISIBLE
                            inputBinding.layoutDropDown.visibility = View.VISIBLE

                            inputBinding.toolBar.title = "Edit Course"
                            inputBinding.tiFirst.hint = "Course Name"
                            inputBinding.tiSecond.hint = "SKS"
                            inputBinding.etSecond.inputType = InputType.TYPE_CLASS_NUMBER
                            inputBinding.tiDropDown.hint = "Choose Lecturer"
                            inputBinding.ivYes.setImageResource(R.drawable.edit)
                            inputBinding.tvYes.text = "Edit"

                            vmCourse.getCourseByUid(uidCourse.toString()).observe(viewLifecycleOwner) {
                                inputBinding.etFirst.setText(it?.nameCourse)
                                inputBinding.etSecond.setText(it?.sksCourse)
                            }

                            vmData.sendUidLecturer(uidLecturer)

                            vmData.uidLecturer.observe(viewLifecycleOwner) {
                                vmLecturer.getLecturerByUid(it?.toString()).observe(viewLifecycleOwner) {
                                    inputBinding.etDropDown.setText(it?.nameLecturer)
                                }
                            }

                            inputBinding.btnDropDown.setOnClickListener {
                                val fragmentLecturer = FragmentSelectLecturer().apply {
                                    onViewCreated = { fragmentSelect ->
                                        fragmentSelect.toolBar.setNavigationOnClickListener {
                                            removeTopFragmentAndShowPrevious()
                                        }
                                    }
                                    onAddClick = {
                                        val uidLecturer = it.uidLecturer
                                        vmData.sendUidLecturer(uidLecturer)
                                        removeTopFragmentAndShowPrevious()
                                    }
                                }
                                addFragmentWithoutBackStack(fragmentLecturer)
                            }

                            inputBinding.btnYes.setOnClickListener {
                                val etFirst = inputBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                                val etSecond = inputBinding.etSecond.text.toString()
                                val dataCourse = DataClassCourse(
                                    nameCourse = etFirst,
                                    sksCourse = etSecond,
                                    uidCourse = uidCourse,
                                    uidLecturer = vmData.uidLecturer.value
                                )

                                if (etFirst.isNotEmpty() && etSecond.isNotEmpty()) {
                                    vmCourse.editCourse(dataCourse).observe(viewLifecycleOwner) {
                                        when (it) {
                                            "Success" -> {
                                                showToastFragment(FragmentToast(R.drawable.check, "Success"))
                                                removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                                            }
                                            "Exist" -> {
                                                showToastFragment(FragmentToast(R.drawable.copy, "Course Name Already Exist"))
                                            }
                                            "Fail" -> {
                                                showToastFragment(FragmentToast(R.drawable.fail, "Fail"))
                                            }
                                        }
                                    }
                                } else {
                                    showToastFragment(FragmentToast(R.drawable.fail, "Fill All Data"))
                                }
                            }
                        }

                        onDestroyView = {
                            vmData.sendUidLecturer(null)
                        }
                    }
                    addFragmentWithoutBackStack(fragmentInput)
                }
            },
            onSecondClick = {
                val uidCourse = it.uidCourse
                val fragmentCard = FragmentCard().apply {
                    onViewCreated = { cardBinding ->

                        cardBinding.toolBar.setNavigationOnClickListener {
                            removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                        }

                        cardBinding.toolBar.title = "Delete Course"
                        cardBinding.ivYes.setImageResource(R.drawable.delete)
                        cardBinding.tvYes.text = "Delete"

                        vmCourse.getCourseByUid(uidCourse.toString()).observe(viewLifecycleOwner) {
                            cardBinding.tvTitle.text = buildString {
                                append(it?.nameCourse)
                            }
                            cardBinding.tvData1.text = buildString {
                                append(it?.sksCourse)
                                append(" SKS")
                            }

                            val uidLecturer = it?.uidLecturer.toString()
                            vmLecturer.getLecturerByUid(uidLecturer).observe(viewLifecycleOwner) {
                                cardBinding.tvData2.text = buildString {
                                    append(it?.nameLecturer)
                                }
                            }

                            val getBuilding = vmBuilding.getBuildingByUid(it?.uidBuilding.toString())
                            val getRoom = vmRoom.getRoomByUid(it?.uidRoom.toString())
                            val combinedBuilding = MediatorLiveData<Pair<DataClassBuilding?, DataClassRoom?>>()

                            fun dataBuildingRoom() {
                                val dataBuilding = getBuilding.value
                                val dataRoom = getRoom.value
                                combinedBuilding.value = Pair(dataBuilding, dataRoom)
                            }

                            combinedBuilding.addSource(getBuilding) { dataBuildingRoom() }
                            combinedBuilding.addSource(getRoom) { dataBuildingRoom() }

                            combinedBuilding.observe(viewLifecycleOwner) { (dataBuilding, dataRoom) ->
                                if (dataBuilding != null && dataRoom != null) {
                                    cardBinding.tvData3.text = buildString {
                                        append("Building ${dataBuilding?.nameBuilding}, ")
                                        append("Room ${dataRoom?.nameRoom}")
                                    }
                                }
                            }

                            if (it?.startTime != null && it.endTime != null) {
                                cardBinding.tvData4.text = buildString {
                                    append(it.startTime)
                                    append(" - ")
                                    append(it.endTime)
                                }
                            }
                        }

                        cardBinding.btnYes.setOnClickListener {
                            vmCourse.deleteCourse(uidCourse.toString()).observe(viewLifecycleOwner) {
                                when (it) {
                                    "Succes" -> {
                                        showToastFragment(FragmentToast(R.drawable.check, "Success"))
                                        removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                                    }
                                    "Fail" -> {
                                        showToastFragment(FragmentToast(R.drawable.fail, "Fail"))
                                    }
                                }
                            }
                        }
                    }
                }
                addFragmentWithoutBackStack(fragmentCard)
            },
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
                                append("SKS : ")
                                append(it?.sksCourse)
                            }
                            cardBinding.tvData4.text = buildString {
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
                                        append(nameMajor)
                                        append(" | ")
                                        append("Level $nameLevel Class $nameClasses")
                                    }
                                }
                            }
                        }

                        vmLecturer.getLecturerByUid(uidLecturer).observe(viewLifecycleOwner) {
                            cardBinding.tvData3.text = buildString {
                                append("Lecturer : ")
                                append(it?.nameLecturer)
                            }
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

        vmCourse.getCourse().observe(viewLifecycleOwner) {
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

    private fun addCourse() {
        val fragmentInput = FragmentInput().apply {
            onViewCreated = { inputBinding ->

                inputBinding.toolBar.setNavigationOnClickListener {
                    removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                }

                inputBinding.tiSecond.visibility = View.VISIBLE
                inputBinding.layoutDropDown.visibility = View.VISIBLE

                inputBinding.toolBar.title = "Add Course"
                inputBinding.tiFirst.hint = "Course Name"
                inputBinding.tiSecond.hint = "SKS"
                inputBinding.etSecond.inputType = InputType.TYPE_CLASS_NUMBER
                inputBinding.tiDropDown.hint = "Choose Lecturer"
                inputBinding.ivYes.setImageResource(R.drawable.add)
                inputBinding.tvYes.text = "Add"

                vmData.uidLecturer.observe(viewLifecycleOwner) {
                    vmLecturer.getLecturerByUid(it?.toString()).observe(viewLifecycleOwner) {
                        inputBinding.etDropDown.setText(it?.nameLecturer)
                    }
                }

                inputBinding.btnDropDown.setOnClickListener {
                    hideKeyboard(inputBinding.root)
                    val fragmentLecturer = FragmentSelectLecturer().apply {
                        onViewCreated = { fragmentSelect ->

                            fragmentSelect.toolBar.setNavigationOnClickListener {
                                removeTopFragmentAndShowPrevious()
                            }

                            recyclerView(
                                tvData1 = true,
                                tvData2 = false,
                                tvData3 = false,
                                tvData4 = false,
                                tvData5 = false
                            )
                        }
                        onAddClick = {
                            val uidLecturer = it.uidLecturer
                            vmData.sendUidLecturer(uidLecturer)
                            removeTopFragmentAndShowPrevious()
                        }
                    }
                    addFragmentWithoutBackStack(fragmentLecturer)
                }

                inputBinding.btnYes.setOnClickListener {
                    val etFirst = inputBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                    val etSecond = inputBinding.etSecond.text.toString()
                    val dataCourse = DataClassCourse(
                        nameCourse = etFirst,
                        sksCourse = etSecond,
                        uidLecturer = vmData.uidLecturer.value
                    )

                    if (etFirst.isNotEmpty() && etSecond.isNotEmpty() && vmData.uidLecturer.value != null) {
                        vmCourse.addCourse(dataCourse).observe(viewLifecycleOwner) {
                            when (it) {
                                "Success" -> {
                                    showToastFragment(FragmentToast(R.drawable.check, "Success"))
                                    removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                                }
                                "Exist" -> {
                                    showToastFragment(FragmentToast(R.drawable.copy, "Course Name Already Exist"))
                                }
                                "Fail" -> {
                                    showToastFragment(FragmentToast(R.drawable.fail, "Fail"))
                                }
                            }
                        }
                    } else {
                        showToastFragment(FragmentToast(R.drawable.fail, "Fill All Data"))
                    }
                }
            }

            onDestroyView = {
                vmData.sendUidLecturer(null)
            }
        }
        addFragmentWithoutBackStack(fragmentInput)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vmData.btnAddSchedule.value = null
        removeFragmentFromContainer(R.id.mainBottomSheetContainer)
        _binding = null
    }
}