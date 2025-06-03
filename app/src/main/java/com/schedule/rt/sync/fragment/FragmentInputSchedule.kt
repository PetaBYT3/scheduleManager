package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterCheckSchedule
import com.schedule.rt.sync.databinding.FragmentInputScheduleBinding
import com.schedule.rt.sync.function.capitalizeEachWord
import com.schedule.rt.sync.objectsingleton.DialogUtil.showToastFragment
import com.schedule.rt.sync.viewmodel.ViewModelClasses
import com.schedule.rt.sync.viewmodel.ViewModelCourse
import com.schedule.rt.sync.viewmodel.ViewModelData
import com.schedule.rt.sync.viewmodel.ViewModelLecturer
import com.schedule.rt.sync.viewmodel.ViewModelLevel
import com.schedule.rt.sync.viewmodel.ViewModelMajor
import com.schedule.rt.sync.viewmodel.ViewModelSchedule
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FragmentInputSchedule : Fragment() {

    private var _binding: FragmentInputScheduleBinding? = null
    private val binding get() = _binding!!

    private val vmData: ViewModelData by activityViewModels()
    private val vmCourse: ViewModelCourse by activityViewModels()
    private val vmMajor: ViewModelMajor by activityViewModels()
    private val vmLevel: ViewModelLevel by activityViewModels()
    private val vmClasses: ViewModelClasses by activityViewModels()
    private val vmLecturer: ViewModelLecturer by activityViewModels()
    private val vmSchedule: ViewModelSchedule by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentInputScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolBar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        actionbar()

        layoutView()

        scheduleLecturer()

        classSchedule()

        binding.btnYes.setOnClickListener {
            addSchedule()
        }
    }

    private fun actionbar() {
        vmSchedule.day.observe(viewLifecycleOwner) {
            val day = it?.capitalizeEachWord()

            binding.toolBar.title = buildString {
                append("Add Schedule To ")
                append(day)
            }
        }
    }

    private fun layoutView() {
        val uidCourse = vmCourse.uidCourse.value
        vmCourse.getCourseByUid(uidCourse.toString()).observe(viewLifecycleOwner) {
            binding.tvTitle.text = it?.nameCourse
            binding.tvData1.text = buildString {
                append(it?.sksCourse)
                append(" SKS")
            }

            val uidMajor = it?.uidMajor
            val uidLevel = it?.uidLevel
            val uidClasses = it?.uidClasses
            val uidLecturer = it?.uidLecturer

            val majorFlow = vmMajor.getMajorByUid(uidMajor).asFlow()
            val levelFlow = vmLevel.getLevelByUid(uidLevel).asFlow()
            val classesFlow = vmClasses.getClassesByUid(uidClasses).asFlow()

            var nameMajor: String? = null
            var nameLevel: String? = null
            var nameClasses: String? = null

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
                        binding.tvData2.text = buildString {
                            append("$nameMajor ")
                            append("$nameLevel ")
                            append(nameClasses)
                        }
                    }
                }
            }

            vmLecturer.getLecturerByUid(uidLecturer).observe(viewLifecycleOwner) {
                binding.tvData3.text = it?.nameLecturer
            }

            val hourValues = Array(24) { String.format("%02d", it) }
            val minuteValues = Array(60) { String.format("%02d", it) }

            binding.tpHour.minValue = 0
            binding.tpHour.maxValue = hourValues.size - 1
            binding.tpHour.displayedValues = hourValues
            binding.tpHour.value = 0 // default "00"

            binding.tpMinutes.minValue = 0
            binding.tpMinutes.maxValue = minuteValues.size - 1
            binding.tpMinutes.displayedValues = minuteValues
            binding.tpMinutes.value = 0 // default "00"

            val onTimeChangedListener = {
                val hourIndex = binding.tpHour.value
                val minuteIndex = binding.tpMinutes.value

                val hourString = hourValues[hourIndex]
                val minuteString = minuteValues[minuteIndex]

                val hour = hourString.toInt()
                val minute = minuteString.toInt()

                if (hour == 0 && minute == 0) {
                    vmSchedule.sendStartTime(null)
                    vmSchedule.sendEndTime(null)
                    binding.tvData4.text = "--:-- - --:--"
                } else {
                    val startTime = String.format("%02d:%02d", hour, minute)

                    val sks = it?.sksCourse?.toInt()
                    val duration = sks?.times(40) ?: 0
                    val endDuration = hour * 60 + minute + duration
                    val endHour = endDuration / 60
                    val endMinute = endDuration % 60

                    val endTime = String.format("%02d:%02d", endHour, endMinute)

                    if (endDuration > 1439) {
                        binding.tvData4.text = "$startTime - --:--"
                    } else {
                        binding.tvData4.text = "$startTime - $endTime"
                    }

                    vmSchedule.sendStartTime(startTime)
                    vmSchedule.sendEndTime(endTime)
                }
            }

            val onTimeWatcher = object : NumberPicker.OnValueChangeListener {
                override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
                    onTimeChangedListener()
                }
            }
            binding.tpHour.setOnValueChangedListener(onTimeWatcher)
            binding.tpMinutes.setOnValueChangedListener(onTimeWatcher)

        }
    }

    private fun addSchedule() {
        val uidCourse = vmCourse.uidCourse.value
        val uidLecturer = vmLecturer.uidLecturer.value
        val uidClasses = vmCourse.uidClassesReference.value.toString()
        val startTime = vmSchedule.startTime.value

        if (startTime != null) {
            vmSchedule.addSchedule(uidCourse, uidLecturer, uidClasses).observe(viewLifecycleOwner) {
                when (it) {
                    "Success" -> {
                        showToastFragment(FragmentToast(R.drawable.check, "Schedule Added"))
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                    "Fail" -> {
                        showToastFragment(FragmentToast(R.drawable.fail, "Schedule Failed"))
                    }
                    "ScheduleConflict" -> {
                        showToastFragment(FragmentToast(R.drawable.fail, "This Room Already Have Schedule In This Time"))
                    }
                    "LecturerConflict" -> {
                        showToastFragment(FragmentToast(R.drawable.profile, "This Lecturer Already Have Schedule In This Time"))
                    }
                    "ClassConflict" -> {
                        showToastFragment(FragmentToast(R.drawable.fail, "This Class Already Have Schedule In This Time"))
                    }
                    else -> {
                        showToastFragment(FragmentToast(R.drawable.fail, "Invalid Time"))
                    }
                }
            }
        } else {
            showToastFragment(FragmentToast(R.drawable.fail, "Invalid Time"))
        }
    }

    private fun scheduleLecturer() {
        val recyclerView: RecyclerView = binding.lecturerSchedule
        val adapter = AdapterCheckSchedule()
        recyclerView.adapter = adapter

        val uidLecturer = vmData.uidLecturer.value

        vmSchedule.day.observe(viewLifecycleOwner) {
            vmCourse.getCourseByLecturerSchedule(uidLecturer, it).observe(viewLifecycleOwner) {
                adapter.updateData(it)
                if (it.isNullOrEmpty()) {
                    binding.pbLecturer.visibility = View.GONE
                    binding.layoutNoDataLecturer.visibility = View.VISIBLE
                } else {
                    binding.pbLecturer.visibility = View.GONE
                    binding.layoutNoDataLecturer.visibility = View.GONE
                }
            }
        }
    }

    private fun classSchedule() {
        val recyclerView: RecyclerView = binding.classSchedule
        val adapter = AdapterCheckSchedule()
        recyclerView.adapter = adapter

        val uidClasses = vmCourse.uidClassesReference.value
        vmSchedule.day.observe(viewLifecycleOwner) {
            vmCourse.getCourseByClassSchedule(uidClasses, it).observe(viewLifecycleOwner) {
                adapter.updateData(it)
                if (it.isNullOrEmpty()) {
                    binding.pbClasses.visibility = View.GONE
                    binding.layoutNoDataClasses.visibility = View.VISIBLE
                } else {
                    binding.pbClasses.visibility = View.GONE
                    binding.layoutNoDataClasses.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vmSchedule.sendStartTime(null)
        vmSchedule.sendEndTime(null)
        _binding = null
    }
}