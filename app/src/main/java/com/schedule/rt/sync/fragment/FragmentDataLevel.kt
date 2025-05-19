package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.activity.ActivityMain
import com.schedule.rt.sync.adapter.AdapterLevel
import com.schedule.rt.sync.databinding.FragmentDataLevelBinding
import com.schedule.rt.sync.dataclass.DataClassLevel
import com.schedule.rt.sync.function.capitalizeAfterDot
import com.schedule.rt.sync.function.capitalizeEachWord
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.showToastFragment
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelClasses
import com.schedule.rt.sync.viewmodel.ViewModelCourse
import com.schedule.rt.sync.viewmodel.ViewModelLevel
import com.schedule.rt.sync.viewmodel.ViewModelMajor


class FragmentDataLevel : Fragment() {

    private var _binding: FragmentDataLevelBinding? = null
    private val binding get() = _binding!!

    private val vmMajor: ViewModelMajor by activityViewModels()
    private val vmLevel: ViewModelLevel by activityViewModels()
    private val vmClasses: ViewModelClasses by activityViewModels()
    private val vmCourse: ViewModelCourse by activityViewModels()

    private val fragmentTag = "dataLevel"

    private var search = false

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
        _binding = FragmentDataLevelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        binding.toolBar.setNavigationOnClickListener {
            (requireActivity() as ActivityMain).btnDrawer()
        }

        vmLevel.uidMajorReference.observe(viewLifecycleOwner) {
            vmMajor.getMajorByUid(it).observe(viewLifecycleOwner) {
                binding.clToolBar.title = it?.nameMajor
            }
        }

        binding.btnAdd.setOnClickListener {
            addLevel()
        }

//        binding.btnSearch.setOnClickListener {
//            if (search) {
//                binding.layoutSearch.visibility = View.GONE
//                binding.ivSearch.setImageResource(R.drawable.search)
//                binding.etSearch.clearFocus()
//                binding.etSearch.text?.clear()
//                forceHideKeyboard()
//                search = false
//            } else {
//                binding.layoutSearch.visibility = View.VISIBLE
//                binding.ivSearch.setImageResource(R.drawable.close)
//                search = true
//                }
//        }
//
//        binding.etSearch.addTextChangedListener(
//            afterTextChanged = {
//                adapterRvLevel.filterList(it.toString())
//            }
//        )

        recyclerView()
    }

    private fun recyclerView() {
        val recyclerView: RecyclerView = binding.rvLevel
        val adapter = AdapterLevel(
            lifecycleOwner = viewLifecycleOwner,
            vmClasses = vmClasses,
            tvData1 = true,
            tvData2 = true,
            btnFirst = true,
            btnSecond = true,
            btnNext = true,
            onFirstClick = {
                val uidLevel = it.uidLevel
                val fragmentInput = FragmentInput().apply {
                    onViewCreated = { inputBinding ->

                        inputBinding.toolBar.setNavigationOnClickListener {
                            requireActivity().supportFragmentManager.popBackStack()
                        }

                        inputBinding.tiSecond.visibility = View.VISIBLE

                        inputBinding.toolBar.title = "Edit Level"
                        inputBinding.tiFirst.hint = "Level"
                        inputBinding.tiSecond.hint = "Semester"
                        inputBinding.ivYes.setImageResource(R.drawable.edit)
                        inputBinding.tvYes.text = "Edit"

                        inputBinding.etFirst.inputType = InputType.TYPE_CLASS_NUMBER
                        inputBinding.etSecond.inputType = InputType.TYPE_CLASS_NUMBER

                        vmLevel.getLevelByUid(uidLevel).observe(viewLifecycleOwner) {
                            inputBinding.etFirst.setText(it?.level)
                            inputBinding.etSecond.setText(it?.semester)
                        }

                        inputBinding.btnYes.setOnClickListener {
                            val etFirst = inputBinding.etFirst.text.toString()
                            val etSecond = inputBinding.etSecond.text.toString()
                            val dataLevel = DataClassLevel(
                                level = etFirst,
                                semester = etSecond,
                                uidLevel = uidLevel
                            )

                            if (etFirst.isNotEmpty() && etSecond.isNotEmpty()) {
                                vmLevel.editLevel(dataLevel).observe(viewLifecycleOwner) {
                                    when (it) {
                                        "Success" -> {
                                            showToastFragment(FragmentToast(R.drawable.check, "Edit Success"))
                                            requireActivity().supportFragmentManager.popBackStack()
                                        }
                                        "Exist" -> {
                                            showToastFragment(FragmentToast(R.drawable.copy, "Level Already Exist"))
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
                val uidLevel = it.uidLevel
                val fragmentCard = FragmentCard().apply {
                    onViewCreated = { cardBinding ->

                        cardBinding.toolBar.setNavigationOnClickListener {
                            requireActivity().supportFragmentManager.popBackStack()
                        }

                        cardBinding.toolBar.title = "Delete Level"
                        cardBinding.ivYes.setImageResource(R.drawable.delete)
                        cardBinding.tvYes.text = "Delete"

                        vmLevel.getLevelByUid(uidLevel).observe(viewLifecycleOwner) {
                            cardBinding.tvTitle.text = buildString {
                                append("Level ")
                                append(it?.level)
                            }

                            cardBinding.tvData1.text = buildString {
                                append("Semester ")
                                append(it?.semester)
                            }
                        }

                        cardBinding.btnYes.setOnClickListener {
                            vmLevel.deleteLevel(uidLevel).observe(viewLifecycleOwner) {
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
                val uidLevel = it.uidLevel
                vmClasses.uidLevelReference(uidLevel)
                vmCourse.uidLevelReference(uidLevel)
                requireActivity().supportFragmentManager.popBackStack(fragmentTag, POP_BACK_STACK_INCLUSIVE)
                replaceFragmentWithBackStack(R.id.mainFragmentContainer, FragmentDataClasses(), null)
            }
        )
        recyclerView.adapter = adapter

        vmLevel.getLevel().observe(viewLifecycleOwner) {
            adapter.updateData(it)
            if (it.isNullOrEmpty()) {
                binding.pbRvLevel.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            } else {
                binding.pbRvLevel.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            }
        }
    }

    private fun addLevel() {
        val fragmentInput = FragmentInput().apply {
            onViewCreated = { inputBinding ->

                inputBinding.toolBar.setNavigationOnClickListener {
                    requireActivity().supportFragmentManager.popBackStack()
                }

                inputBinding.tiSecond.visibility = View.VISIBLE

                inputBinding.toolBar.title = "Add Level"
                inputBinding.tiFirst.hint = "Level"
                inputBinding.tiSecond.hint = "Semester"
                inputBinding.ivYes.setImageResource(R.drawable.add)
                inputBinding.tvYes.text = "Add"

                inputBinding.etFirst.inputType = InputType.TYPE_CLASS_NUMBER
                inputBinding.etSecond.inputType = InputType.TYPE_CLASS_NUMBER

                inputBinding.btnYes.setOnClickListener {
                    val etFirst = inputBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                    val etSecond = inputBinding.etSecond.text.toString().capitalizeEachWord().capitalizeAfterDot()
                    if (etFirst.isNotEmpty() && etSecond.isNotEmpty()) {
                        val dataLevel = DataClassLevel(
                            level = etFirst,
                            semester = etSecond
                        )

                        vmLevel.addLevel(dataLevel).observe(viewLifecycleOwner) {
                            inputBinding.pbYes.visibility = View.VISIBLE
                            when (it) {
                                "Success" -> {
                                    showToastFragment(FragmentToast(R.drawable.check, "Add Success"))
                                    requireActivity().supportFragmentManager.popBackStack()
                                }
                                "Exist" -> {
                                    showToastFragment(FragmentToast(R.drawable.copy, "Level Already Exist"))
                                }
                                "Fail" -> {
                                    showToastFragment(FragmentToast(R.drawable.fail, "Add Failed"))
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
        requireActivity().supportFragmentManager.popBackStack(fragmentTag, POP_BACK_STACK_INCLUSIVE)
        replaceFragmentWithBackStack(R.id.mainBottomSheetContainer, fragmentInput, fragmentTag)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().supportFragmentManager.popBackStack(fragmentTag, POP_BACK_STACK_INCLUSIVE)
        _binding = null
    }
}