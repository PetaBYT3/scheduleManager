package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterBuilding
import com.schedule.rt.sync.databinding.FragmentDataBuildingBinding
import com.schedule.rt.sync.dataclass.DataClassBuilding
import com.schedule.rt.sync.function.capitalizeAfterDot
import com.schedule.rt.sync.function.capitalizeEachWord
import com.schedule.rt.sync.objectsingleton.DialogUtil.addFragmentWithoutBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.removeFragmentFromContainer
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.showToastFragment
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelBuilding
import com.schedule.rt.sync.viewmodel.ViewModelRoom

class FragmentDataBuilding : Fragment() {

    private var _binding: FragmentDataBuildingBinding? = null
    private val binding get() = _binding!!

    private val vmBuilding : ViewModelBuilding by activityViewModels()
    private val vmRoom : ViewModelRoom by activityViewModels()

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
        _binding = FragmentDataBuildingBinding.inflate(inflater, container, false)
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
            addBuilding()
        }

        rvBuilding()
    }

    private fun rvBuilding() {
        val recyclerView: RecyclerView = binding.rvBuilding
        val adapter = AdapterBuilding(
            btnFirst = true,
            btnSecond = true,
            btnNext = true,
            onFirstClick = {
                val uidBuilding = it.uidBuilding
                val fragmentInput = FragmentInput().apply {
                    onViewCreated = { inputBinding ->

                        inputBinding.toolBar.setNavigationOnClickListener {
                            removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                        }

                        inputBinding.toolBar.title = "Edit Building"
                        inputBinding.ivYes.setImageResource(R.drawable.edit)
                        inputBinding.tvYes.text = "Edit"

                        vmBuilding.getBuildingByUid(uidBuilding).observe(viewLifecycleOwner) {
                            inputBinding.etFirst.setText(it?.nameBuilding)
                        }

                        inputBinding.btnYes.setOnClickListener {
                            val etFirst = inputBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                            val dataBuilding = DataClassBuilding(
                                nameBuilding = etFirst
                            )

                            vmBuilding.editBuilding(dataBuilding).observe(viewLifecycleOwner) {
                                when (it) {
                                    "Success" -> {
                                        showToastFragment(FragmentToast(R.drawable.check, "Add Success"))
                                        removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                                    }
                                    "Exist" -> {
                                        showToastFragment(FragmentToast(R.drawable.copy, "Building Exist"))
                                    }
                                    else -> {
                                        showToastFragment(FragmentToast(R.drawable.fail, "Something Went Wrong"))
                                    }
                                }
                            }
                        }
                    }
                }
                addFragmentWithoutBackStack(fragmentInput)
            },
            onSecondClick = {
                val uidBuilding = it.uidBuilding
                val fragmentCard = FragmentCard().apply {
                    onViewCreated = { cardBinding ->

                        cardBinding.toolBar.setNavigationOnClickListener {
                            removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                        }

                        cardBinding.toolBar.title = "Delete Building"
                        cardBinding.ivYes.setImageResource(R.drawable.delete)
                        cardBinding.tvYes.text = "Delete"

                        vmBuilding.getBuildingByUid(uidBuilding).observe(viewLifecycleOwner) {
                            cardBinding.tvTitle.text = buildString {
                                append("Building ")
                                append(it?.nameBuilding)
                            }
                        }

                        cardBinding.btnYes.setOnClickListener {
                            vmBuilding.deleteBuilding(uidBuilding).observe(viewLifecycleOwner) {
                                when (it) {
                                    "Success" -> {
                                        showToastFragment(FragmentToast(R.drawable.check, "Add Success"))
                                        removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                                    }
                                    else -> {
                                        showToastFragment(FragmentToast(R.drawable.fail, "Something Went Wrong"))
                                    }
                                }
                            }
                        }
                    }
                }
                addFragmentWithoutBackStack(fragmentCard)
            },
            onNextClick = {
                val uidBuilding = it.uidBuilding
                vmRoom.uidBuildingReference(uidBuilding)
                replaceFragmentWithBackStack(R.id.mainFragmentContainer, FragmentDataRoom(), null)
            }
        )

        recyclerView.adapter = adapter

        vmBuilding.getBuilding().observe(viewLifecycleOwner) {
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

    private fun addBuilding() {
        val fragmentInput = FragmentInput().apply {
            onViewCreated = { inputBinding ->

                inputBinding.toolBar.setNavigationOnClickListener {
                    removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                }

                inputBinding.toolBar.title = "Add Building"
                inputBinding.tiFirst.hint = "Building Name"
                inputBinding.ivYes.setImageResource(R.drawable.add)
                inputBinding.tvYes.text = "Add"

                inputBinding.btnYes.setOnClickListener {
                    val etFirst = inputBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                    val dataBuilding = DataClassBuilding(
                        nameBuilding = etFirst
                    )

                    if (etFirst.isNotEmpty()) {
                        vmBuilding.addBuilding(dataBuilding).observe(viewLifecycleOwner) {
                            when (it) {
                                "Success" -> {
                                    showToastFragment(FragmentToast(R.drawable.check, "Add Success"))
                                    removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                                }
                                "Exist" -> {
                                    showToastFragment(FragmentToast(R.drawable.copy, "Building Already Exist"))
                                }
                                else -> {
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

    override fun onDestroy() {
        super.onDestroy()
        removeFragmentFromContainer(R.id.mainBottomSheetContainer)
        _binding = null
    }
}