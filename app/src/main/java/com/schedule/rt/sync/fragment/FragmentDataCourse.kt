package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterDataCourse
import com.schedule.rt.sync.adapter.AdapterLecturer
import com.schedule.rt.sync.databinding.FragmentDataCourseBinding
import com.schedule.rt.sync.dataclass.DataClassCourse
import com.schedule.rt.sync.function.capitalizeAfterDot
import com.schedule.rt.sync.function.capitalizeEachWord
import com.schedule.rt.sync.objectsingleton.DialogUtil
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelClasses
import com.schedule.rt.sync.viewmodel.ViewModelCourse
import com.schedule.rt.sync.viewmodel.ViewModelLecturer
import com.schedule.rt.sync.viewmodel.ViewModelLevel

class FragmentDataCourse : Fragment() {

    private lateinit var binding: FragmentDataCourseBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterRvCourse: AdapterDataCourse

    private val vmLecturer: ViewModelLecturer by activityViewModels()

    private val vmLevel: ViewModelLevel by activityViewModels()
    private val vmClasses: ViewModelClasses by activityViewModels()
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
        binding = FragmentDataCourseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        actionBar()

        rvCourse()
    }

    private fun actionBar() {
        val uidLevel = vmCourse.uidLevel
        val uidClasses = vmCourse.uidClasses

        vmLevel.getLevelByUid(uidLevel).observe(viewLifecycleOwner) {
            val nameLevel = it?.level

            vmClasses.getClassesByUid(uidClasses).observe(viewLifecycleOwner) {
                val nameClasses = it?.nameClasses

                binding.clToolBar.title = buildString {
                    append("Level ")
                    append(nameLevel)
                    append(" / ")
                    append("Class ")
                    append(nameClasses)
                }
            }
        }

        binding.toolBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnEditSchedule.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fragmentContainer, FragmentBuilding::class.java, null)
                addToBackStack(null)
            }
        }

        binding.btnAdd.setOnClickListener {
            addCourse()
        }
    }

    private fun rvCourse() {
        recyclerView = binding.rvCourse
        adapterRvCourse = AdapterDataCourse(vmLecturer, viewLifecycleOwner)
        recyclerView.adapter = adapterRvCourse

        vmCourse.getCourse()
        vmCourse.dataCourse.observe(viewLifecycleOwner) {
            adapterRvCourse.updateRvCourse(it)
            if (it.isNotEmpty()) {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            } else {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            }
        }

        adapterRvCourse.setOnItemClickListener(object : AdapterDataCourse.onItemClickListener {
            override fun onEditClick(position: Int) {
                DialogUtil.showBottomSheet2Et(
                    requireActivity(),
                    "Edit Course",
                    "Course Name",
                    "SKS",
                    R.drawable.edit,
                    "Edit"
                ) { bottomSheetBinding, bottomSheet ->

                    bottomSheetBinding.etSecond.inputType = InputType.TYPE_CLASS_NUMBER

                    val uidCourse = adapterRvCourse.dataClassCourse[position].uidCourse.toString()

                    vmCourse.getCourseByUid(uidCourse).observe(viewLifecycleOwner) {
                        bottomSheetBinding.etFirst.setText(it?.nameCourse)
                        bottomSheetBinding.etSecond.setText(it?.sksCourse)
                    }

                    val currentUidLecturer = adapterRvCourse.dataClassCourse[position].uidLecturer.toString()
                    vmLecturer.getLecturerByUid(currentUidLecturer).observe(viewLifecycleOwner) {
                        bottomSheetBinding.etThird.setText(it?.nameLecturer)
                    }

                    var uidLecturer: String? = null

                    bottomSheetBinding.tiThird.visibility = View.VISIBLE
                    bottomSheetBinding.tiThird.hint = "Choose Lecturer"
                    bottomSheetBinding.etThird.setOnClickListener {
                        val bottomSheetLecturer = BottomSheetLecturer()

                        bottomSheetLecturer.show(parentFragmentManager, "BottomSheetLecturer")
                        bottomSheetLecturer.setOnClickListener(object : BottomSheetLecturer.setOnClickListener {
                            override fun onAddClick(
                                position: Int,
                                adapterRvLecturer: AdapterLecturer
                            ) {
                                val nameLecturer = adapterRvLecturer.dataClassLecturer[position].nameLecturer
                                uidLecturer = adapterRvLecturer.dataClassLecturer[position].uidLecturer

                                bottomSheetBinding.etThird.setText(nameLecturer)
                                bottomSheetLecturer.dismiss()
                            }
                        })
                    }

                    bottomSheetBinding.btnYes.setOnClickListener {
                        val etFirst = bottomSheetBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                        val etSecond = bottomSheetBinding.etSecond.text.toString()
                        val dataCourse = DataClassCourse(
                            nameCourse = etFirst,
                            sksCourse = etSecond,
                            uidCourse = uidCourse,
                            uidLecturer = uidLecturer
                        )

                        vmCourse.editCourse(dataCourse).observe(viewLifecycleOwner) {
                            when (it) {
                                "Success" -> {
                                    DialogUtil.showToast(
                                        requireActivity(),
                                        "Success",
                                        R.drawable.check
                                    )
                                    bottomSheet.dismiss()
                                }

                                "Exist" -> {
                                    DialogUtil.showToast(
                                        requireActivity(),
                                        "Course Name Already Exist",
                                        R.drawable.copy
                                    )
                                }

                                "Fail" -> {
                                    DialogUtil.showToast(
                                        requireActivity(),
                                        "Fail",
                                        R.drawable.close
                                    )
                                }
                            }
                        }
                    }
                }
            }

            override fun onDeleteClick(position: Int) {
                DialogUtil.showBottomSheetConfirmation(
                    requireActivity(),
                    "Delete Course",
                    R.drawable.delete,
                    "Delete",
                    { bottomSheetBinding, bottomSheet ->

                        val uidCourse = adapterRvCourse.dataClassCourse[position].uidCourse.toString()

                        vmCourse.getCourseByUid(uidCourse).observe(viewLifecycleOwner) {
                            bottomSheetBinding.tvTittle.text = buildString {
                                append(it?.nameCourse)
                            }
                            bottomSheetBinding.tvData1.text = buildString {
                                append(it?.sksCourse)
                            }
                        }

                        bottomSheetBinding.btnYes.setOnClickListener {
                            vmCourse.deleteCourse(uidCourse).observe(viewLifecycleOwner) {
                                when (it) {
                                    "Succes" -> {
                                        DialogUtil.showToast(requireActivity(), "Success", R.drawable.check)
                                        bottomSheet.dismiss()
                                    }

                                    "Fail" -> {
                                        DialogUtil.showToast(requireActivity(), "Fail", R.drawable.close)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        })
    }

    private fun addCourse() {
        DialogUtil.showBottomSheet2Et(
            requireActivity(),
            "Add Course",
            "Course Name",
            "SKS",
            R.drawable.add,
            "Add"
        ) { bottomSheetBinding, bottomSheet ->

            bottomSheetBinding.etSecond.inputType = InputType.TYPE_CLASS_NUMBER

            var uidLecturer: String? = null

            bottomSheetBinding.tiThird.visibility = View.VISIBLE
            bottomSheetBinding.tiThird.hint = "Choose Lecturer"
            bottomSheetBinding.etThird.setOnClickListener {
                val bottomSheetLecturer = BottomSheetLecturer()

                bottomSheetLecturer.show(parentFragmentManager, "BottomSheetLecturer")
                bottomSheetLecturer.setOnClickListener(object : BottomSheetLecturer.setOnClickListener {
                    override fun onAddClick(
                        position: Int,
                        adapterRvLecturer: AdapterLecturer
                    ) {
                        val nameLecturer = adapterRvLecturer.dataClassLecturer[position].nameLecturer
                        uidLecturer = adapterRvLecturer.dataClassLecturer[position].uidLecturer

                        bottomSheetBinding.etThird.setText(nameLecturer)
                        bottomSheetLecturer.dismiss()
                    }
                })
            }

            bottomSheetBinding.btnYes.setOnClickListener {
                val etFirst = bottomSheetBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                val etSecond = bottomSheetBinding.etSecond.text.toString()
                val dataCourse = DataClassCourse(
                    nameCourse = etFirst,
                    sksCourse = etSecond,
                    uidLecturer = uidLecturer
                )

                vmCourse.addCourse(dataCourse).observe(viewLifecycleOwner) {
                    when (it) {
                        "Success" -> {
                            DialogUtil.showToast(requireActivity(), "Success", R.drawable.check)
                            bottomSheet.dismiss()
                        }

                        "Exist" -> {
                            DialogUtil.showToast(
                                requireActivity(),
                                "Course Name Already Exist",
                                R.drawable.copy
                            )
                        }

                        "Fail" -> {
                            DialogUtil.showToast(requireActivity(), "Fail", R.drawable.close)
                        }
                    }
                }
            }
        }
    }
}