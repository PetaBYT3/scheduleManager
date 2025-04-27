package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterDataBuilding
import com.schedule.rt.sync.databinding.FragmentDataBuildingBinding
import com.schedule.rt.sync.dataclass.DataClassBuilding
import com.schedule.rt.sync.function.capitalizeAfterDot
import com.schedule.rt.sync.function.capitalizeEachWord
import com.schedule.rt.sync.objectsingleton.DialogUtil
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelAdministrator
import com.schedule.rt.sync.viewmodel.ViewModelBuilding
import com.schedule.rt.sync.viewmodel.ViewModelRoom

class FragmentDataBuilding : Fragment() {

    private lateinit var binding: FragmentDataBuildingBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterRvBuilding: AdapterDataBuilding

    private val viewModelAdministrator: ViewModelAdministrator by activityViewModels()
    private val viewModelBuilding : ViewModelBuilding by activityViewModels()
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
        binding = FragmentDataBuildingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        actionBar()

        rvBuilding()
    }

    private fun actionBar() {
        binding.toolBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnAdd.setOnClickListener {
            addBuilding()
        }
    }

    private fun rvBuilding() {
        recyclerView = binding.rvBuilding
        adapterRvBuilding = AdapterDataBuilding(viewModelBuilding, viewLifecycleOwner)
        recyclerView.adapter = adapterRvBuilding

        viewModelBuilding.getBuilding()
        viewModelBuilding.dataBuilding.observe(viewLifecycleOwner) {
            adapterRvBuilding.updateRvBuilding(it)
            if (it.isEmpty()) {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            } else {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            }
        }

        adapterRvBuilding.setOnItemClickListener(object : AdapterDataBuilding.onItemClickListener {
            override fun onItemClick(position: Int) {
                val uidBuilding = adapterRvBuilding.dataClassBuilding[position].uidBuilding
                viewModelAdministrator.uidBuilding = uidBuilding
                viewModelRoom.uidBuilding = uidBuilding
                requireActivity().supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace(R.id.fragmentContainer, FragmentDataRoom::class.java, null)
                    addToBackStack(null)
                }
            }

            override fun onEditClick(position: Int) {
                DialogUtil.showBottomSheet1Et(
                    requireActivity(),
                    "Edit Building",
                    "Name Building",
                    R.drawable.edit,
                    "Edit"
                ) { bottomSheetBinding, bottomSheet ->

                    val uidBuilding = adapterRvBuilding.dataClassBuilding[position].uidBuilding

                    viewModelBuilding.getBuildingByUid(uidBuilding).observe(viewLifecycleOwner) {
                        bottomSheetBinding.etFirst.setText(it?.nameBuilding)
                    }

                    bottomSheetBinding.btnYes.setOnClickListener {
                        val etFirst =
                            bottomSheetBinding.etFirst.text.toString().capitalizeEachWord()
                                .capitalizeAfterDot()
                        val dataBuilding =
                            DataClassBuilding(uidBuilding = uidBuilding, nameBuilding = etFirst)

                        if (etFirst.isNotEmpty()) {
                            viewModelBuilding.editBuilding(dataBuilding).observe(viewLifecycleOwner) {
                                if (it != null) {
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
                                                "Exist",
                                                R.drawable.warning
                                            )
                                        }

                                        "Fail" -> {
                                            DialogUtil.showToast(
                                                requireActivity(),
                                                "Fail",
                                                R.drawable.warning
                                            )
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
                    "Delete Building",
                    R.drawable.delete,
                    "Delete",
                    { bottomSheetBinding, bottomSheet ->

                        val uidBuilding = adapterRvBuilding.dataClassBuilding[position].uidBuilding
                        viewModelBuilding.getBuildingByUid(uidBuilding).observe(viewLifecycleOwner) {
                            bottomSheetBinding.tvTittle.text = buildString {
                                append("Building ")
                                append(it?.nameBuilding)
                            }
                        }

                        viewModelBuilding.getRoomSize(uidBuilding).observe(viewLifecycleOwner) {
                            bottomSheetBinding.tvData1.text = buildString {
                                append(it?.toString())
                                append(" Room")
                            }
                        }

                        bottomSheetBinding.btnYes.setOnClickListener {
                            viewModelBuilding.deleteBuilding(uidBuilding).observe(viewLifecycleOwner) {
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

    private fun addBuilding() {
        DialogUtil.showBottomSheet1Et(
            requireActivity(),
            "Add Building",
            "Name Building",
            R.drawable.add,
            "Add"
        ) { bottomSheetBinding, bottomSheet ->

            bottomSheetBinding.btnYes.setOnClickListener {
                val etFirst = bottomSheetBinding.etFirst.text.toString().capitalizeEachWord()
                    .capitalizeAfterDot()
                val dataBuilding = DataClassBuilding(nameBuilding = etFirst)

                if (etFirst.isNotEmpty()) {
                    viewModelBuilding.addBuilding(dataBuilding).observe(viewLifecycleOwner) {
                        if (it != null) {
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
                                        "Exist",
                                        R.drawable.warning
                                    )
                                }

                                "Fail" -> {
                                    DialogUtil.showToast(
                                        requireActivity(),
                                        "Fail",
                                        R.drawable.warning
                                    )
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
}