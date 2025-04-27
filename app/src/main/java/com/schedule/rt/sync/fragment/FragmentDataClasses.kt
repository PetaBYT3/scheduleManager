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
import com.schedule.rt.sync.adapter.AdapterDataClasses
import com.schedule.rt.sync.databinding.FragmentDataClassesBinding
import com.schedule.rt.sync.dataclass.DataClassClasses
import com.schedule.rt.sync.function.capitalizeAfterDot
import com.schedule.rt.sync.function.capitalizeEachWord
import com.schedule.rt.sync.objectsingleton.DialogUtil
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelAdministrator
import com.schedule.rt.sync.viewmodel.ViewModelClasses
import com.schedule.rt.sync.viewmodel.ViewModelCourse
import com.schedule.rt.sync.viewmodel.ViewModelLevel
import com.schedule.rt.sync.viewmodel.ViewModelScheduleManager

class FragmentDataClasses() : Fragment() {

    private lateinit var binding: FragmentDataClassesBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterRvClasses: AdapterDataClasses

    private val viewModelScheduleManager : ViewModelScheduleManager by activityViewModels()
    private val viewModelAdministrator: ViewModelAdministrator by activityViewModels()

    private val vmLevel: ViewModelLevel by activityViewModels()
    private val vmClasses: ViewModelClasses by activityViewModels()
    private val vmCourse: ViewModelCourse by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = TransitionUtil.enterTransition()
        returnTransition = TransitionUtil.returnTransition()
        exitTransition = TransitionUtil.exitTransition()
        reenterTransition = TransitionUtil.reenterTransition()

        sharedElementEnterTransition = TransitionUtil.sharedElementEnterTransition(requireActivity())
        sharedElementReturnTransition = TransitionUtil.sharedElementReturnTransition(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDataClassesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        actionBar()

        rvClasses()

    }

    private fun actionBar() {
        val uidLevel = vmClasses.uidLevel
        vmLevel.getLevelByUid(uidLevel).observe(viewLifecycleOwner) {
            binding.clToolBar.title = buildString {
                append("Level ")
                append(it?.level)
            }
        }

        binding.toolBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnAdd.setOnClickListener {
            addClasses()
        }
    }

    private fun rvClasses() {
        recyclerView = binding.rvClasses
        adapterRvClasses = AdapterDataClasses(vmCourse, viewLifecycleOwner)
        recyclerView.adapter = adapterRvClasses

        vmClasses.getClasses()
        vmClasses.dataClasses.observe(viewLifecycleOwner) {
            adapterRvClasses.updateRvClasses(it)
            if (it.isNotEmpty()) {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            } else {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            }
        }

        adapterRvClasses.setOnItemClickListener(object : AdapterDataClasses.onItemClickListener {
            override fun onItemClick(position: Int) {
                val uidMajor = vmClasses.uidMajor
                val uidLevel = vmClasses.uidLevel
                val uidClasses = adapterRvClasses.dataClassClasses[position].uidClasses

                vmCourse.uidMajor = uidMajor
                vmCourse.uidLevel = uidLevel
                vmCourse.uidClasses = uidClasses

                requireActivity().supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace(R.id.fragmentContainer, FragmentDataCourse::class.java, null)
                    addToBackStack(null)
                }
            }

            override fun onEditClick(position: Int) {
                DialogUtil.showBottomSheet1Et(
                    requireActivity(),
                    "Edit Class",
                    "Class Name",
                    R.drawable.edit,
                    "Edit"
                ) { bottomSheetBinding, bottomSheet ->

                    val uidClasses = adapterRvClasses.dataClassClasses[position].uidClasses.toString()

                    vmClasses.getClassesByUid(uidClasses).observe(viewLifecycleOwner) {
                        bottomSheetBinding.etFirst.setText(it?.nameClasses.toString())
                    }

                    bottomSheetBinding.btnYes.setOnClickListener {
                        val etFirst = bottomSheetBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                        val dataClasses = DataClassClasses(
                            nameClasses = etFirst,
                            uidClasses = uidClasses
                        )

                        if (etFirst.isNotEmpty()) {
                            vmClasses.editClasses(dataClasses).observe(viewLifecycleOwner) {
                                when (it) {
                                    "Success" -> {
                                        DialogUtil.showToast(
                                            requireActivity(),
                                            "Edit Success",
                                            R.drawable.check
                                        )
                                        bottomSheet.dismiss()
                                    }

                                    "Exist" -> {
                                        DialogUtil.showToast(
                                            requireActivity(),
                                            "Class Exist",
                                            R.drawable.fail
                                        )
                                    }

                                    "Fail" -> {
                                        DialogUtil.showToast(
                                            requireActivity(),
                                            "Edit Failed",
                                            R.drawable.fail
                                        )
                                    }
                                }
                            }
                        } else {
                            DialogUtil.showToast(
                                requireActivity(),
                                "Fill All Field",
                                R.drawable.fail
                            )
                        }
                    }
                }
            }

            override fun onDeleteClick(position: Int) {
                DialogUtil.showBottomSheetConfirmation(
                    requireActivity(),
                    "Delete Class",
                    R.drawable.delete,
                    "Delete",
                    { bottomSheetBinding, bottomSheet ->

                        val uidClasses = adapterRvClasses.dataClassClasses[position].uidClasses.toString()

                        vmClasses.getClassesByUid(uidClasses).observe(viewLifecycleOwner) {
                            bottomSheetBinding.tvTittle.text = buildString {
                                append("Class ")
                                append(it?.nameClasses)
                            }
                        }
                        viewModelScheduleManager.getCourseSize(uidClasses).observe(viewLifecycleOwner) {
                            bottomSheetBinding.tvData1.text = buildString {
                                append(it?.toString())
                                append(" Course")
                            }
                        }

                        bottomSheetBinding.btnYes.setOnClickListener {
                            vmClasses.deleteClasses(uidClasses).observe(viewLifecycleOwner) {
                                when (it) {
                                    "Success" -> {
                                        DialogUtil.showToast(requireActivity(), "Delete Success", R.drawable.check)
                                        bottomSheet.dismiss()
                                    }
                                    "Fail" -> {
                                        DialogUtil.showToast(requireActivity(), "Delete Failed", R.drawable.fail)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        })
    }

    private fun addClasses() {
        DialogUtil.showBottomSheet1Et(
            requireActivity(),
            "Add Class",
            "Class Name",
            R.drawable.add,
            "Add"
        ) { bottomSheetBinding, bottomSheet ->

            bottomSheetBinding.btnYes.setOnClickListener {
                val etFirst = bottomSheetBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                val dataClasses = DataClassClasses(
                    nameClasses = etFirst
                )

                if (etFirst.isNotEmpty()) {
                    vmClasses.addClasses(dataClasses).observe(viewLifecycleOwner) {
                        when (it) {
                            "Success" -> {
                                DialogUtil.showToast(
                                    requireActivity(),
                                    "Add Success",
                                    R.drawable.check
                                )
                                bottomSheet.dismiss()
                            }

                            "Exist" -> {
                                DialogUtil.showToast(
                                    requireActivity(),
                                    "Class Exist",
                                    R.drawable.fail
                                )
                            }

                            "Fail" -> {
                                DialogUtil.showToast(
                                    requireActivity(),
                                    "Add Failed",
                                    R.drawable.fail
                                )
                            }
                        }
                    }
                } else {
                    DialogUtil.showToast(requireActivity(), "Fill All Field", R.drawable.fail)
                }
            }
        }
    }
}