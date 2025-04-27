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
import com.schedule.rt.sync.adapter.AdapterDataMajor
import com.schedule.rt.sync.databinding.FragmentDataMajorBinding
import com.schedule.rt.sync.dataclass.DataClassMajor
import com.schedule.rt.sync.function.capitalizeAfterDot
import com.schedule.rt.sync.function.capitalizeEachWord
import com.schedule.rt.sync.objectsingleton.DialogUtil
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelAdministrator
import com.schedule.rt.sync.viewmodel.ViewModelMajor

class FragmentDataMajor : Fragment() {

    private lateinit var binding: FragmentDataMajorBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterRvMajor: AdapterDataMajor

    private val viewModelAdministrator: ViewModelAdministrator by activityViewModels()
    private val viewModelMajor: ViewModelMajor by activityViewModels()

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
        binding = FragmentDataMajorBinding.inflate(inflater, container, false)
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
        recyclerView = binding.rvMajor
        adapterRvMajor = AdapterDataMajor(viewModelAdministrator, viewLifecycleOwner)
        recyclerView.adapter = adapterRvMajor

        viewModelMajor.getMajors()
        viewModelMajor.dataMajor.observe(viewLifecycleOwner) {
            adapterRvMajor.updateRvMajor(it)
            if (it.isNotEmpty()) {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            } else {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            }
        }

        adapterRvMajor.setOnItemClickListener(object : AdapterDataMajor.onItemClickListener {
            override fun onItemClick(position: Int) {
                val uidMajor = adapterRvMajor.dataClassMajor[position].uidMajor
                viewModelAdministrator.uidMajor = uidMajor
                requireActivity().supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace(R.id.fragmentContainer, FragmentDataManager::class.java, null)
                    addToBackStack(null)
                }
            }

            override fun onEditClick(position: Int) {
                DialogUtil.showBottomSheet1Et(
                    requireActivity(),
                    "Add Major",
                    "Name Major",
                    R.drawable.edit,
                    "Edit"
                ) { bottomSheetBinding, bottomSheet ->

                    val uidMajor = adapterRvMajor.dataClassMajor[position].uidMajor
                    viewModelMajor.getMajorByUid(uidMajor).observe(viewLifecycleOwner) {
                        bottomSheetBinding.etFirst.setText(it?.nameMajor)
                    }

                    bottomSheetBinding.btnYes.setOnClickListener {
                        val nameMajor = bottomSheetBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                        val dataMajor = DataClassMajor(nameMajor = nameMajor, uidMajor = uidMajor)

                        if (nameMajor.isNotEmpty()) {
                            viewModelMajor.editMajor(dataMajor).observe(viewLifecycleOwner) {
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
                            DialogUtil.showToast(requireActivity(), "Fail", R.drawable.warning)
                        }
                    }
                }
            }

            override fun onDeleteClick(position: Int) {
                DialogUtil.showBottomSheetConfirmation(
                    requireActivity(),
                    "Delete Major",
                    R.drawable.delete,
                    "Delete",
                    { bottomSheetBinding, bottomSheet ->

                        val uidMajor = adapterRvMajor.dataClassMajor[position].uidMajor
                        viewModelMajor.getMajorByUid(uidMajor).observe(viewLifecycleOwner) {
                            bottomSheetBinding.tvTittle.text = it?.nameMajor
                        }
                        viewModelAdministrator.getManagerSize(uidMajor).observe(viewLifecycleOwner) {
                            bottomSheetBinding.tvData1.text = buildString {
                                append(it?.toString())
                                append(" Manager")
                            }
                        }

                        bottomSheetBinding.btnYes.setOnClickListener {
                            viewModelMajor.deleteMajor(uidMajor).observe(viewLifecycleOwner) {
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

    private fun addMajor() {
        DialogUtil.showBottomSheet1Et(
            requireActivity(),
            "Add Major",
            "Name Major",
            R.drawable.add,
            "Add"
        ) { bottomSheetBinding, bottomSheet ->

            bottomSheetBinding.btnYes.setOnClickListener {
                val nameMajor = bottomSheetBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                val dataMajor = DataClassMajor(nameMajor = nameMajor)

                if (nameMajor.isNotEmpty()) {
                    viewModelMajor.addMajor(dataMajor).observe(viewLifecycleOwner) {
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
                    DialogUtil.showToast(requireActivity(), "Fill All Field", R.drawable.warning)
                }
            }
        }
    }
}