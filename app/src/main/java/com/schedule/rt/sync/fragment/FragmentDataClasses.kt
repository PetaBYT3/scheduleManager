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
import com.schedule.rt.sync.adapter.AdapterClasses
import com.schedule.rt.sync.databinding.FragmentDataClassesBinding
import com.schedule.rt.sync.dataclass.DataClassClasses
import com.schedule.rt.sync.function.capitalizeAfterDot
import com.schedule.rt.sync.function.capitalizeEachWord
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.showToastFragment
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelClasses
import com.schedule.rt.sync.viewmodel.ViewModelCourse
import com.schedule.rt.sync.viewmodel.ViewModelLevel

class FragmentDataClasses() : Fragment() {

    private var _binding: FragmentDataClassesBinding? = null
    private val binding get() = _binding!!

    private val vmLevel: ViewModelLevel by activityViewModels()
    private val vmClasses: ViewModelClasses by activityViewModels()
    private val vmCourse: ViewModelCourse by activityViewModels()

    private val fragmentTag = "dataClasses"

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
        _binding = FragmentDataClassesBinding.inflate(inflater, container, false)
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

        vmClasses.uidLevelReference.observe(viewLifecycleOwner) {
            vmLevel.getLevelByUid(it).observe(viewLifecycleOwner) {
                binding.clToolBar.title = buildString {
                    append(it?.level)
                }
            }
        }

        binding.btnAdd.setOnClickListener {
            addClasses()
        }

        recyclerView()

    }

    private fun recyclerView() {
        val recyclerView: RecyclerView = binding.rvClasses
        val adapter = AdapterClasses(
            lifecycleOwner = viewLifecycleOwner,
            vmCourse = vmCourse,
            tvData1 = true,
            btnFirst = true,
            btnSecond = true,
            btnNext = true,
            onFirstClick = {
                val uidClasses = it.uidClasses
                val fragmentInput = FragmentInput().apply {
                    onViewCreated = { inputBinding ->

                        inputBinding.toolBar.setNavigationOnClickListener {
                            requireActivity().supportFragmentManager.popBackStack()
                        }

                        inputBinding.toolBar.title = "Edit Class"
                        inputBinding.tiFirst.hint = "Class Name"
                        inputBinding.ivYes.setImageResource(R.drawable.edit)
                        inputBinding.tvYes.text = "Edit"

                        vmClasses.getClassesByUid(uidClasses).observe(viewLifecycleOwner) {
                            inputBinding.etFirst.setText(it?.nameClasses.toString())
                        }

                        inputBinding.btnYes.setOnClickListener {
                            val etFirst = inputBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                            val dataClasses = DataClassClasses(
                                nameClasses = etFirst,
                                uidClasses = uidClasses
                            )

                            if (etFirst.isNotEmpty()) {
                                vmClasses.editClasses(dataClasses).observe(viewLifecycleOwner) {
                                    when (it) {
                                        "Success" -> {
                                            showToastFragment(FragmentToast(R.drawable.check, "Edit Success"))
                                            requireActivity().supportFragmentManager.popBackStack()
                                        }

                                        "Exist" -> {
                                            showToastFragment(FragmentToast(R.drawable.copy, "Class Exist"))
                                        }

                                        "Fail" -> {
                                            showToastFragment(FragmentToast(R.drawable.fail, "Edit Failed"))
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
            },
            onSecondClick = {
                val uidClasses = it.uidClasses
                val fragmentCard = FragmentCard().apply {
                    onViewCreated = { cardBinding ->

                        cardBinding.toolBar.setNavigationOnClickListener {
                            requireActivity().supportFragmentManager.popBackStack()
                        }

                        cardBinding.toolBar.title = "Delete Class"
                        cardBinding.ivYes.setImageResource(R.drawable.delete)
                        cardBinding.tvYes.text = "Delete"

                        vmClasses.getClassesByUid(uidClasses).observe(viewLifecycleOwner) {
                            cardBinding.tvTitle.text = buildString {
                                append("Class ")
                                append(it?.nameClasses)
                            }
                        }

                        cardBinding.btnYes.setOnClickListener {
                            vmClasses.deleteClasses(uidClasses).observe(viewLifecycleOwner) {
                                when (it) {
                                    "Success" -> {
                                        showToastFragment(FragmentToast(R.drawable.check, "Delete Success"))
                                        requireActivity().supportFragmentManager.popBackStack()
                                    }
                                    "Fail" -> {
                                        showToastFragment(FragmentToast(R.drawable.fail, "Delete Failed"))
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
                val uidClasses = it.uidClasses
                vmCourse.uidClassesReference(uidClasses)
                requireActivity().supportFragmentManager.popBackStack(fragmentTag, POP_BACK_STACK_INCLUSIVE)
                replaceFragmentWithBackStack(R.id.mainFragmentContainer, FragmentDataCourse(), null)
            }
        )

        recyclerView.adapter = adapter

        vmClasses.getClasses().observe(viewLifecycleOwner) {
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

    private fun addClasses() {
        val fragmentInput = FragmentInput().apply {
            onViewCreated = { inputBinding ->

                inputBinding.toolBar.setNavigationOnClickListener {
                    requireActivity().supportFragmentManager.popBackStack()
                }

                inputBinding.toolBar.title = "Add Class"
                inputBinding.tiFirst.hint = "Class Name"
                inputBinding.ivYes.setImageResource(R.drawable.add)
                inputBinding.tvYes.text = "Add"

                inputBinding.btnYes.setOnClickListener {
                    val etFirst = inputBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                    val dataClasses = DataClassClasses(
                        nameClasses = etFirst
                    )

                    if (etFirst.isNotEmpty()) {
                        vmClasses.addClasses(dataClasses).observe(viewLifecycleOwner) {
                            when (it) {
                                "Success" -> {
                                    showToastFragment(FragmentToast(R.drawable.check, "Add Success"))
                                    requireActivity().supportFragmentManager.popBackStack()
                                }

                                "Exist" -> {
                                    showToastFragment(FragmentToast(R.drawable.copy, "Class Exist"))
                                }

                                "Fail" -> {
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