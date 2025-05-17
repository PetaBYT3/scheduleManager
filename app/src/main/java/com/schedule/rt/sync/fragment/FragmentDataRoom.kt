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
import com.schedule.rt.sync.adapter.AdapterRoom
import com.schedule.rt.sync.databinding.FragmentDataRoomBinding
import com.schedule.rt.sync.dataclass.DataClassRoom
import com.schedule.rt.sync.function.capitalizeAfterDot
import com.schedule.rt.sync.function.capitalizeEachWord
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.showToastFragment
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelBuilding
import com.schedule.rt.sync.viewmodel.ViewModelRoom

class FragmentDataRoom : Fragment() {

    private var _binding: FragmentDataRoomBinding? = null
    private val binding get() = _binding!!

    private val vmBuilding: ViewModelBuilding by activityViewModels()
    private val vmRoom : ViewModelRoom by activityViewModels()

    private val fragmentTag = "dataRoom"

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
        _binding = FragmentDataRoomBinding.inflate(inflater, container, false)
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

        val uidBuilding = vmRoom.uidBuildingReference.value
        vmBuilding.getBuildingByUid(uidBuilding).observe(viewLifecycleOwner) {
            binding.clToolBar.title = buildString {
                append("Building ")
                append(it?.nameBuilding)
            }
        }

        binding.btnAdd.setOnClickListener {
            addRoom()
        }

        recyclerView()

    }

    private fun recyclerView() {
        val recyclerView: RecyclerView = binding.rvRoom
        val adapter = AdapterRoom(
            btnFirst = true,
            btnSecond = true,
            btnNext = false,
            onFirstClick = {
                val uidRoom = it.uidRoom
                val fragmentInput = FragmentInput().apply {
                    onViewCreated = { inputBinding ->

                        inputBinding.toolBar.setNavigationOnClickListener {
                            requireActivity().supportFragmentManager.popBackStack()
                        }

                        inputBinding.toolBar.title = "Edit Room"
                        inputBinding.ivYes.setImageResource(R.drawable.edit)
                        inputBinding.tvYes.text = "Edit"

                        vmRoom.getRoomByUid(uidRoom).observe(viewLifecycleOwner) {
                            inputBinding.etFirst.setText(it?.nameRoom)
                        }

                        inputBinding.btnYes.setOnClickListener {
                            val etFirst = inputBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                            val dataRoom = DataClassRoom(
                                nameRoom = etFirst,
                                uidRoom = uidRoom
                            )

                            vmRoom.editRoom(dataRoom).observe(viewLifecycleOwner) {
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
                val uidRoom = it.uidRoom
                val fragmentCard = FragmentCard().apply {
                    onViewCreated = { cardBinding ->

                        cardBinding.toolBar.setNavigationOnClickListener {
                            requireActivity().supportFragmentManager.popBackStack()
                        }

                        cardBinding.toolBar.title = "Delete Room"
                        cardBinding.ivYes.setImageResource(R.drawable.delete)
                        cardBinding.tvYes.text = "Delete"

                        vmRoom.getRoomByUid(uidRoom).observe(viewLifecycleOwner) {
                            cardBinding.tvTitle.text = buildString {
                                append("Room ")
                                append(it?.nameRoom)
                            }
                        }

                        cardBinding.btnYes.setOnClickListener {
                            vmRoom.deleteRoom(uidRoom).observe(viewLifecycleOwner) {
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
            }
        )

        recyclerView.adapter = adapter

        vmRoom.getRoom().observe(viewLifecycleOwner) {
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

    private fun addRoom() {
        val fragmentInput = FragmentInput().apply {
            onViewCreated = { inputBinding ->

                inputBinding.toolBar.setNavigationOnClickListener {
                    requireActivity().supportFragmentManager.popBackStack()
                }

                inputBinding.toolBar.title = "Add Room"
                inputBinding.tiFirst.hint = "Room Name"
                inputBinding.ivYes.setImageResource(R.drawable.add)
                inputBinding.tvYes.text = "Add"

                inputBinding.btnYes.setOnClickListener {
                    val etFirst = inputBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                    val dataRoom = DataClassRoom(
                        nameRoom = etFirst
                    )

                    if (etFirst.isNotEmpty()) {
                        vmRoom.addRoom(dataRoom).observe(viewLifecycleOwner) {
                            when (it) {
                                "Success" -> {
                                    showToastFragment(FragmentToast(R.drawable.check, "Add Success"))
                                    requireActivity().supportFragmentManager.popBackStack()
                                }
                                "Exist" -> {
                                    showToastFragment(FragmentToast(R.drawable.copy, "Class Exist"))
                                }
                                else -> {
                                    showToastFragment(FragmentToast(R.drawable.fail, "Add Failed"))
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

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().supportFragmentManager.popBackStack(fragmentTag, POP_BACK_STACK_INCLUSIVE)
        _binding = null
    }
}