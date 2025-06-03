package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterBuilding
import com.schedule.rt.sync.databinding.FragmentDataBuildingBinding
import com.schedule.rt.sync.dataclass.DataClassBuilding
import com.schedule.rt.sync.function.capitalizeAfterDot
import com.schedule.rt.sync.function.capitalizeEachWord
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.showToastFragment
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelBuilding
import com.schedule.rt.sync.viewmodel.ViewModelCourse
import com.schedule.rt.sync.viewmodel.ViewModelRoom

class FragmentDataBuilding : Fragment() {

    private var _binding: FragmentDataBuildingBinding? = null
    private val binding get() = _binding!!

    private val vmBuilding : ViewModelBuilding by activityViewModels()
    private val vmRoom : ViewModelRoom by activityViewModels()
    private val vmCourse: ViewModelCourse by activityViewModels()

    private val fragmentTag = "dataBuilding"

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
            lifecycleOwner = viewLifecycleOwner,
            vmRoom = vmRoom,
            tvData1 = true,
            btnFirst = true,
            btnSecond = true,
            btnNext = true,
            onFirstClick = {
                val uidBuilding = it.uidBuilding
                val fragmentInput = FragmentInput().apply {
                    onViewCreated = { inputBinding ->

                        inputBinding.toolBar.setNavigationOnClickListener {
                            requireActivity().supportFragmentManager.popBackStack()
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
                                uidBuilding = uidBuilding,
                                nameBuilding = etFirst
                            )

                            vmBuilding.editBuilding(dataBuilding).observe(viewLifecycleOwner) {
                                when (it) {
                                    "Success" -> {
                                        showToastFragment(FragmentToast(R.drawable.check, "Add Success"))
                                        requireActivity().supportFragmentManager.popBackStack()
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
                requireActivity().supportFragmentManager.popBackStack(fragmentTag, POP_BACK_STACK_INCLUSIVE)
                replaceFragmentWithBackStack(R.id.mainBottomSheetContainer, fragmentInput, fragmentTag)
            },
            onSecondClick = {
                val uidBuilding = it.uidBuilding
                val fragmentCard = FragmentCard().apply {
                    onViewCreated = { cardBinding ->

                        cardBinding.layoutMessage.visibility = View.VISIBLE
                        cardBinding.tvData3.visibility = View.GONE
                        cardBinding.tvData4.visibility = View.GONE
                        cardBinding.tvData5.visibility = View.GONE

                        cardBinding.toolBar.setNavigationOnClickListener {
                            requireActivity().supportFragmentManager.popBackStack()
                        }

                        cardBinding.ivCard.setImageResource(R.drawable.building)
                        cardBinding.toolBar.title = "Delete Building"
                        cardBinding.ivYes.setImageResource(R.drawable.delete)
                        cardBinding.tvYes.text = "Delete"
                        cardBinding.tvMessage.text = "This Building Will Be Deleted, All Rooms And Schedule That Related To This Building Will Be Deleted"

                        vmBuilding.getBuildingByUid(uidBuilding).observe(viewLifecycleOwner) {
                            cardBinding.tvTitle.text = buildString {
                                append("Building ")
                                append(it?.nameBuilding)
                            }
                        }

                        vmRoom.getRoomSizeByBuilding(uidBuilding).observe(viewLifecycleOwner) {
                            cardBinding.tvData1.text = buildString {
                                append(it)
                                append(" Rooms")
                            }
                        }

                        vmCourse.getCourseSizeByBuilding(uidBuilding).observe(viewLifecycleOwner) {
                            cardBinding.tvData2.text = buildString {
                                append(it)
                                append(" Schedule")
                            }
                        }

                        cardBinding.btnYes.setOnClickListener {
                            vmBuilding.deleteBuilding(uidBuilding).observe(viewLifecycleOwner) {
                                when (it) {
                                    "Success" -> {
                                        showToastFragment(FragmentToast(R.drawable.check, "Add Success"))
                                        requireActivity().supportFragmentManager.popBackStack()
                                    }
                                    else -> {
                                        showToastFragment(FragmentToast(R.drawable.fail, "Something Went Wrong"))
                                    }
                                }
                            }
                        }
                    }
                }
                requireActivity().supportFragmentManager.popBackStack(fragmentTag, POP_BACK_STACK_INCLUSIVE)
                replaceFragmentWithBackStack(R.id.mainBottomSheetContainer, fragmentCard, fragmentTag)
            },
            onNextClick = {
                val uidBuilding = it.uidBuilding
                vmRoom.uidBuildingReference(uidBuilding)
                requireActivity().supportFragmentManager.popBackStack(fragmentTag, POP_BACK_STACK_INCLUSIVE)
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
                    requireActivity().supportFragmentManager.popBackStack()
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
                                    requireActivity().supportFragmentManager.popBackStack()
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
        requireActivity().supportFragmentManager.popBackStack(fragmentTag, POP_BACK_STACK_INCLUSIVE)
        replaceFragmentWithBackStack(R.id.mainBottomSheetContainer, fragmentInput, fragmentTag)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().supportFragmentManager.popBackStack(fragmentTag, POP_BACK_STACK_INCLUSIVE)
        _binding = null
    }
}