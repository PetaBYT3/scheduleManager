package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterMajor
import com.schedule.rt.sync.databinding.FragmentDataMajorBinding
import com.schedule.rt.sync.dataclass.DataClassMajor
import com.schedule.rt.sync.function.capitalizeAfterDot
import com.schedule.rt.sync.function.capitalizeEachWord
import com.schedule.rt.sync.objectsingleton.DialogUtil.addFragmentWithoutBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.removeFragmentFromContainer
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.showToastFragment
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelData
import com.schedule.rt.sync.viewmodel.ViewModelMajor

class FragmentDataMajor : Fragment() {

    private var _binding: FragmentDataMajorBinding? = null
    private val binding get() = _binding!!

    private val vmData: ViewModelData by activityViewModels()
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
        _binding = FragmentDataMajorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        actionBar()

        rvMajor()

    }

    private fun actionBar() {
        binding.toolBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnAdd.setOnClickListener {
            addMajor()
        }
    }

    private fun rvMajor() {
        val recyclerView: RecyclerView = binding.rvMajor
        val adapter = AdapterMajor(
            btnFirst = true,
            btnSecond = true,
            btnNext = true,
            onFirstClick = {
                val uidMajor = it.uidMajor
                val fragmentInput = FragmentInput().apply {
                    onViewCreated = { inputBinding ->

                        inputBinding.toolBar.setNavigationOnClickListener {
                            removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                        }

                        inputBinding.toolBar.title = "Edit Major"
                        inputBinding.tiFirst.hint = "Major Name"
                        inputBinding.ivYes.setImageResource(R.drawable.edit)
                        inputBinding.tvYes.text = "Edit"

                        vmMajor.getMajorByUid(uidMajor).observe(viewLifecycleOwner) {
                            inputBinding.etFirst.setText(it?.nameMajor)
                        }

                        inputBinding.btnYes.setOnClickListener {
                            val etFirst = inputBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                            val dataMajor = DataClassMajor(
                                nameMajor = etFirst,
                                uidMajor = uidMajor
                            )

                            if (etFirst.isNotEmpty()) {
                                vmMajor.editMajor(dataMajor).observe(viewLifecycleOwner) {
                                    when (it) {
                                        "Success" -> {
                                            showToastFragment(FragmentToast(R.drawable.check, "Edit Success"))
                                            removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                                        }
                                        "Exist" -> {
                                            showToastFragment(FragmentToast(R.drawable.copy, "Class Exist"))
                                        }
                                        "Fail" -> {
                                            showToastFragment(FragmentToast(R.drawable.fail, "Edit Failed"))
                                        }
                                        "Error" -> {
                                            showToastFragment(FragmentToast(R.drawable.fail, "Something Went Wrong"))
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
                val uidMajor = it.uidMajor
                val fragmentCard = FragmentCard().apply {
                    onViewCreated = { cardBinding ->

                        cardBinding.toolBar.setNavigationOnClickListener {
                            removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                        }

                        cardBinding.toolBar.title = "Delete Major"
                        cardBinding.ivYes.setImageResource(R.drawable.delete)
                        cardBinding.tvYes.text = "Delete"

                        vmMajor.getMajorByUid(uidMajor).observe(viewLifecycleOwner) {
                            cardBinding.tvTitle.text = it?.nameMajor
                        }

                        cardBinding.btnYes.setOnClickListener {
                            vmMajor.deleteMajor(uidMajor).observe(viewLifecycleOwner) {
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
            },
            onNextClick = {
                val uidMajor = it.uidMajor
                vmData.sendUidMajor(uidMajor.toString())
                replaceFragmentWithBackStack(R.id.mainFragmentContainer, FragmentDataManager(), null)
            }
        )

        recyclerView.adapter = adapter

        vmMajor.getMajors().observe(viewLifecycleOwner) {
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

    private fun addMajor() {
        val fragmentInput = FragmentInput().apply {
            onViewCreated = { inputBinding ->

                inputBinding.toolBar.setNavigationOnClickListener {
                    removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                }

                inputBinding.toolBar.title = "Add Major"
                inputBinding.tiFirst.hint = "Major Name"
                inputBinding.ivYes.setImageResource(R.drawable.add)
                inputBinding.tvYes.text = "Add"

                inputBinding.btnYes.setOnClickListener {
                    val etFirst = inputBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                    val dataMajor = DataClassMajor(
                        nameMajor = etFirst
                    )

                    if (etFirst.isNotEmpty()) {
                        vmMajor.addMajor(dataMajor).observe(viewLifecycleOwner) {
                            when (it) {
                                "Success" -> {
                                    showToastFragment(FragmentToast(R.drawable.check, "Add Success"))
                                    removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                                }
                                "Exist" -> {
                                    showToastFragment(FragmentToast(R.drawable.copy, "Class Exist"))
                                }
                                "Fail" -> {
                                    showToastFragment(FragmentToast(R.drawable.fail, "Add Failed"))
                                }
                                "Error" -> {
                                    showToastFragment(FragmentToast(R.drawable.fail, "Something Went Wrong"))
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeFragmentFromContainer(R.id.mainBottomSheetContainer)
        _binding = null
    }
}