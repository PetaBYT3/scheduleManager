package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterLecturer
import com.schedule.rt.sync.databinding.FragmentDataLecturerBinding
import com.schedule.rt.sync.dataclass.DataClassLecturer
import com.schedule.rt.sync.function.capitalizeAfterDot
import com.schedule.rt.sync.function.capitalizeEachWord
import com.schedule.rt.sync.objectsingleton.DialogUtil.addFragmentWithoutBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.removeFragmentFromContainer
import com.schedule.rt.sync.objectsingleton.DialogUtil.showToastFragment
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelLecturer
import com.schedule.rt.sync.viewmodel.ViewModelMajor

class FragmentDataLecturer : Fragment() {

    private var _binding: FragmentDataLecturerBinding? = null
    private val binding get() = _binding!!

    private val vmLecturer: ViewModelLecturer by activityViewModels()
    private val vmMajor: ViewModelMajor by activityViewModels()

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
        _binding = FragmentDataLecturerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        binding.toolBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnAdd.setOnClickListener {
            addLecturer()
        }

        rvLecturer()

    }

    private fun rvLecturer() {
        val recyclerView: RecyclerView = binding.rvLecturer
        val adapter = AdapterLecturer(
            vmMajor, viewLifecycleOwner,
            tvData1 = true,
            tvData2 = true,
            tvData3 = false,
            tvData4 = false,
            tvData5 = false,
            btnFirst = true,
            btnSecond = true,
            btnNext = false,
            onFirstClick = {
                val uidLecturer = it.uidLecturer
                val fragmentInput = FragmentInput().apply {
                    onViewCreated = { inputBinding ->

                        inputBinding.toolBar.setNavigationOnClickListener {
                            removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                        }

                        inputBinding.tiSecond.visibility = View.VISIBLE
                        inputBinding.layoutAdministratorAccess.visibility = View.VISIBLE

                        inputBinding.toolBar.title = "Edit Lecturer"
                        inputBinding.tiFirst.hint = "Lecturer Name"
                        inputBinding.tiSecond.hint = "NIK"
                        inputBinding.ivYes.setImageResource(R.drawable.edit)
                        inputBinding.tvYes.text = "Edit"

                        inputBinding.etSecond.keyListener = DigitsKeyListener.getInstance("0123456789.")

                        vmLecturer.getLecturerByUid(uidLecturer).observe(viewLifecycleOwner) {
                            inputBinding.etFirst.setText(it?.nameLecturer)
                            inputBinding.etSecond.setText(it?.nikLecturer)

                            val administratorAccess = it?.administratorAccess
                            if (administratorAccess == "True") {
                                inputBinding.swAdministratorAccess.isChecked = true
                            } else {
                                inputBinding.swAdministratorAccess.isChecked = false
                            }
                        }

                        inputBinding.btnYes.setOnClickListener {
                            val etFirst = inputBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                            val etSecond = inputBinding.etSecond.text.toString()
                            val swAdministratorAccess = if (inputBinding.swAdministratorAccess.isChecked) "True" else "False"
                            val dataLecturer = DataClassLecturer(
                                uidLecturer = uidLecturer,
                                nameLecturer = etFirst,
                                nikLecturer = etSecond,
                                administratorAccess = swAdministratorAccess
                            )

                            if (etFirst.isNotEmpty() && etSecond.isNotEmpty()) {
                                vmLecturer.editLecturer(dataLecturer).observe(viewLifecycleOwner) {
                                    when (it) {
                                        "Success" -> {
                                            showToastFragment(FragmentToast(R.drawable.check, "Edit Success"))
                                            removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                                        }
                                        "Exist" -> {
                                            showToastFragment(FragmentToast(R.drawable.copy, "NIK Already Exist"))
                                        }
                                        "Fail" -> {
                                            showToastFragment(FragmentToast(R.drawable.fail, "Edit Failed"))
                                        }
                                        "Error" -> {
                                            showToastFragment(FragmentToast(R.drawable.fail, "Something Went Wrong")
                                            )
                                        }
                                    }
                                }
                            } else {
                                showToastFragment(FragmentToast(R.drawable.fail, "Fill All Field"))
                            }
                        }
                    }
                }
                addFragmentWithoutBackStack(fragmentInput)
            },
            onSecondClick = {
                val uidLecturer = it.uidLecturer
                val fragmentCard = FragmentCard().apply {
                    onViewCreated = { cardBinding ->

                        cardBinding.toolBar.setNavigationOnClickListener {
                            removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                        }

                        cardBinding.toolBar.title = "Delete Lecturer"
                        cardBinding.ivYes.setImageResource(R.drawable.delete)
                        cardBinding.tvYes.text = "Delete"

                        vmLecturer.getLecturerByUid(uidLecturer).observe(viewLifecycleOwner) {
                            cardBinding.tvTitle.text = it?.nameLecturer
                            cardBinding.tvData1.text = it?.nikLecturer
                            cardBinding.tvData2.text = buildString {
                                append("Administrator Access : ")
                                append(it?.administratorAccess)
                            }
                        }

                        cardBinding.tvData3.visibility = View.GONE
                        cardBinding.tvData4.visibility = View.GONE
                        cardBinding.tvData5.visibility = View.GONE

                        cardBinding.btnYes.setOnClickListener {
                            vmLecturer.deleteLecturer(uidLecturer).observe(viewLifecycleOwner) {
                                when (it) {
                                    "Success" -> {
                                        showToastFragment(FragmentToast(R.drawable.check, "Delete Success"))
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

        vmLecturer.getLecturer().observe(viewLifecycleOwner) {
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

    private fun addLecturer() {
        val fragmentInput = FragmentInput().apply {
            onViewCreated = { inputBinding ->

                inputBinding.toolBar.setNavigationOnClickListener {
                    removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                }

                inputBinding.tiSecond.visibility = View.VISIBLE
                inputBinding.layoutAdministratorAccess.visibility = View.VISIBLE

                inputBinding.toolBar.title = "Add Lecturer"
                inputBinding.tiFirst.hint = "Lecturer Name"
                inputBinding.tiSecond.hint = "NIK"
                inputBinding.ivYes.setImageResource(R.drawable.add)
                inputBinding.tvYes.text = "Add"

                inputBinding.etSecond.keyListener = DigitsKeyListener.getInstance("0123456789.")

                inputBinding.btnYes.setOnClickListener {
                    val etFirst = inputBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                    val etSecond = inputBinding.etSecond.text.toString().capitalizeEachWord().capitalizeAfterDot()
                    val swAdministratorAccess = if (inputBinding.swAdministratorAccess.isChecked) "True" else "False"
                    if (etFirst.isNotEmpty() && etSecond.isNotEmpty()) {
                        val dataLecturer = DataClassLecturer(
                            nameLecturer = etFirst,
                            nikLecturer = etSecond,
                            administratorAccess = swAdministratorAccess
                        )

                        vmLecturer.addLecturer(dataLecturer).observe(viewLifecycleOwner) {
                            inputBinding.pbYes.visibility = View.VISIBLE
                            when (it) {
                                "Success" -> {
                                    showToastFragment(FragmentToast(R.drawable.check, "Edit Success"))
                                    removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                                }
                                "Exist" -> {
                                    showToastFragment(FragmentToast(R.drawable.copy, "NIK Already Exist"))
                                }
                                "Fail" -> {
                                    showToastFragment(FragmentToast(R.drawable.fail, "Edit Failed"))
                                }
                                "Error" -> {
                                    showToastFragment(FragmentToast(R.drawable.fail, "Something Went Wrong")
                                    )
                                }
                            }
                            inputBinding.pbYes.visibility = View.INVISIBLE
                        }
                    } else {
                        showToastFragment(FragmentToast(R.drawable.fail, "Fill All Field"))
                    }
                }
            }
        }
        addFragmentWithoutBackStack(fragmentInput)
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFragmentFromContainer(R.id.mainBottomSheetContainer)
        _binding = null
    }
}