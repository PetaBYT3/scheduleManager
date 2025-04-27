package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterDataRoom
import com.schedule.rt.sync.databinding.FragmentDataRoomBinding
import com.schedule.rt.sync.dataclass.DataClassRoom
import com.schedule.rt.sync.function.capitalizeAfterDot
import com.schedule.rt.sync.function.capitalizeEachWord
import com.schedule.rt.sync.objectsingleton.DialogUtil
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelBuilding
import com.schedule.rt.sync.viewmodel.ViewModelRoom

class FragmentDataRoom : Fragment() {

    private lateinit var binding: FragmentDataRoomBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterRvRoom: AdapterDataRoom
    private val viewModelBuilding: ViewModelBuilding by activityViewModels()
    private val viewModelRoom : ViewModelRoom by activityViewModels()

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
        binding = FragmentDataRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        actionBar()

        rvRoom()

    }

    private fun actionBar() {
        binding.toolBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val uidBuilding = viewModelRoom.uidBuilding
        viewModelBuilding.getBuildingByUid(uidBuilding).observe(viewLifecycleOwner) {
            binding.clToolBar.title = buildString {
                append("Building ")
                append(it?.nameBuilding)
            }
        }

        binding.btnAdd.setOnClickListener {
            addRoom()
        }
    }

    private fun rvRoom() {
        recyclerView = binding.rvRoom
        adapterRvRoom = AdapterDataRoom()
        recyclerView.adapter = adapterRvRoom

        viewModelRoom.getRoom()
        viewModelRoom.dataRoom.observe(viewLifecycleOwner) {
            adapterRvRoom.updateRvRoom(it)
            if (it.isNotEmpty()) {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            } else {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            }
        }

        adapterRvRoom.setOnItemClickListener(object : AdapterDataRoom.onItemClickListener {
            override fun onEditClick(position: Int) {
                DialogUtil.showBottomSheet1Et(
                    requireActivity(),
                    "Edit Room",
                    "Name Room",
                    R.drawable.edit,
                    "Edit"
                ) { bottomSheetBinding, bottomSheet ->

                    val uidRoom = adapterRvRoom.dataClassRoom[position].uidRoom
                    viewModelRoom.getRoomByUid(uidRoom).observe(viewLifecycleOwner) {
                        bottomSheetBinding.etFirst.setText(it?.nameRoom)
                    }

                    bottomSheetBinding.btnYes.setOnClickListener {
                        val etFirst =
                            bottomSheetBinding.etFirst.text.toString().capitalizeEachWord()
                                .capitalizeAfterDot()
                        val dataRoom = DataClassRoom(uidRoom = uidRoom, nameRoom = etFirst)

                        if (etFirst.isNotEmpty()) {
                            viewModelRoom.editRoom(dataRoom).observe(viewLifecycleOwner) {
                                if (it != null) {
                                    when (it) {
                                        "Success" -> {
                                            DialogUtil.showToast(requireActivity(), "Success", R.drawable.check)
                                            bottomSheet.dismiss()
                                        }

                                        "Exist" -> {
                                            DialogUtil.showToast(requireActivity(), "Exist", R.drawable.warning)
                                        }

                                        "Fail" -> {
                                            DialogUtil.showToast(requireActivity(), "Fail", R.drawable.warning)
                                        }
                                    }
                                }
                            }
                        } else {
                            DialogUtil.showToast(requireActivity(), "Empty", R.drawable.warning)
                        }
                    }
                }
            }

            override fun onDeleteClick(position: Int) {
                DialogUtil.showBottomSheetConfirmation(
                    requireActivity(),
                    "Delete Room",
                    R.drawable.delete,
                    "Delete",
                    { bottomSheetBinding, bottomSheet ->

                        val uidRoom = adapterRvRoom.dataClassRoom[position].uidRoom
                        viewModelRoom.getRoomByUid(uidRoom).observe(viewLifecycleOwner) {
                            bottomSheetBinding.tvTittle.text = buildString {
                                append("Room ")
                                append(it?.nameRoom)
                            }
                        }

                        bottomSheetBinding.btnYes.setOnClickListener {
                            viewModelRoom.deleteRoom(uidRoom).observe(viewLifecycleOwner) {
                                if (it != null) {
                                    when (it) {
                                        "Success" -> {
                                            DialogUtil.showToast(requireActivity(), "Success", R.drawable.check)
                                            bottomSheet.dismiss()
                                        }
                                        "Fail" -> {
                                            DialogUtil.showToast(requireActivity(), "Fail", R.drawable.warning)
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }
        })
    }

    private fun addRoom() {
        DialogUtil.showBottomSheet1Et(
            requireActivity(),
            "Add Room",
            "Name Room",
            R.drawable.add,
            "Add"
        ) { bottomSheetBinding, bottomSheet ->

            bottomSheetBinding.btnYes.setOnClickListener {
                val etFirst = bottomSheetBinding.etFirst.text.toString().capitalizeEachWord()
                    .capitalizeAfterDot()
                val dataRoom = DataClassRoom(nameRoom = etFirst)

                if (etFirst.isNotEmpty()) {
                    viewModelRoom.addRoom(dataRoom).observe(viewLifecycleOwner) {
                        if (it != null) {
                            when (it) {
                                "Success" -> {
                                    DialogUtil.showToast(requireActivity(), "Success", R.drawable.check)
                                    bottomSheet.dismiss()
                                }

                                "Exist" -> {
                                    DialogUtil.showToast(requireActivity(), "Exist", R.drawable.warning)
                                }

                                "Fail" -> {
                                    DialogUtil.showToast(requireActivity(), "Fail", R.drawable.warning)
                                }
                            }
                        }
                    }
                } else {
                    DialogUtil.showToast(requireActivity(), "Empty", R.drawable.warning)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModelRoom.uidBuilding = null
    }
}