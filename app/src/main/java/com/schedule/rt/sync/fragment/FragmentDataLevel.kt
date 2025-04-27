package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.activity.ActivityMain
import com.schedule.rt.sync.adapter.AdapterDataLevel
import com.schedule.rt.sync.databinding.FragmentDataLevelBinding
import com.schedule.rt.sync.dataclass.DataClassLevel
import com.schedule.rt.sync.objectsingleton.DialogUtil
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelAdministrator
import com.schedule.rt.sync.viewmodel.ViewModelClasses
import com.schedule.rt.sync.viewmodel.ViewModelLevel
import com.schedule.rt.sync.viewmodel.ViewModelScheduleManager


class FragmentDataLevel : Fragment() {

    private lateinit var binding: FragmentDataLevelBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterRvLevel: AdapterDataLevel

    private val viewModelScheduleManager : ViewModelScheduleManager by activityViewModels()
    private val viewModelAdministrator: ViewModelAdministrator by activityViewModels()

    private val vmLevel: ViewModelLevel by activityViewModels()
    private val vmClasses: ViewModelClasses by activityViewModels()

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
        binding = FragmentDataLevelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        actionBar()

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

        rvLevel()
    }

    private fun actionBar() {
        binding.toolBar.setNavigationOnClickListener {
            (requireActivity() as ActivityMain).btnDrawer()
        }

        viewModelScheduleManager.getMajorByUid().observe(viewLifecycleOwner) {
            binding.clToolBar.title = it?.nameMajor
        }

        binding.btnAdd.setOnClickListener {
            addLevel()
        }
    }

//    fun Fragment.forceHideKeyboard() {
//        val view = view?.rootView ?: View(requireContext())
//
//        // Cara 1: InputMethodManager
//        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(view.windowToken, 0)
//
//        // Cara 2: WindowInsetsController (Android 11+)
//        ViewCompat.getWindowInsetsController(view)?.hide(WindowInsetsCompat.Type.ime())
//    }

    private fun rvLevel() {
        binding.rvLevel.post {
            TransitionUtil.slideUpTransition(binding.rvLevel)
        }

        recyclerView = binding.rvLevel
        adapterRvLevel = AdapterDataLevel(vmClasses, viewLifecycleOwner)
        recyclerView.adapter = adapterRvLevel

        vmLevel.getLevel()
        vmLevel.dataLevel.observe(viewLifecycleOwner) {
            adapterRvLevel.updateRvLevel(it)
            if (it.isNotEmpty()) {
                binding.pbRvLevel.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            } else {
                binding.pbRvLevel.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            }
        }

        adapterRvLevel.setOnItemClickListener(object : AdapterDataLevel.onItemClickListener {
            override fun onItemClick(position: Int) {
                val uidMajor = vmLevel.uidMajor
                val uidLevel = adapterRvLevel.dataClassLevel[position].uidLevel

                vmClasses.uidMajor = uidMajor
                vmClasses.uidLevel = uidLevel

                requireActivity().supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace(R.id.fragmentContainer, FragmentDataClasses::class.java, null)
                    addToBackStack(null)
                }
            }

            override fun onEditClick(position: Int) {
                DialogUtil.showBottomSheet2Et(
                    requireActivity(),
                    "Edit Level",
                    "Level",
                    "Semester",
                    R.drawable.edit,
                    "Edit"
                ) { bottomSheetBinding, bottomSheet ->

                    val uidLevel = adapterRvLevel.dataClassLevel[position].uidLevel

                    bottomSheetBinding.etFirst.inputType = InputType.TYPE_CLASS_NUMBER
                    bottomSheetBinding.etSecond.inputType = InputType.TYPE_CLASS_NUMBER

                    vmLevel.getLevelByUid(uidLevel).observe(viewLifecycleOwner) {
                        bottomSheetBinding.etFirst.setText(it?.level.toString())
                        bottomSheetBinding.etSecond.setText(it?.semester.toString())
                    }

                    bottomSheetBinding.btnYes.setOnClickListener {
                        val etFirst = bottomSheetBinding.etFirst.text.toString()
                        val etSecond = bottomSheetBinding.etSecond.text.toString()
                        val dataLevel = DataClassLevel(
                            level = etFirst,
                            semester = etSecond,
                            uidLevel = uidLevel
                        )

                        if (etFirst.isNotEmpty() && etSecond.isNotEmpty()) {
                            vmLevel.editLevel(dataLevel).observe(viewLifecycleOwner) {
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
                                        DialogUtil.showPopUp(
                                            requireActivity(),
                                            "Level Already Exist",
                                            bottomSheetBinding.root
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
                    "Delete Level",
                    R.drawable.delete,
                    "Delete",
                    { bottomSheetBinding, bottomSheet ->

                        val uidLevel = adapterRvLevel.dataClassLevel[position].uidLevel

                        vmLevel.getLevelByUid(uidLevel).observe(viewLifecycleOwner) {
                            bottomSheetBinding.tvTittle.text = buildString {
                                append("Level ")
                                append(it?.level)
                            }
                            bottomSheetBinding.tvData1.text = buildString {
                                append("Semester ")
                                append(it?.semester)
                            }
                        }

                        bottomSheetBinding.tvData2.visibility = View.VISIBLE
                        viewModelScheduleManager.getClassesSize(uidLevel).observe(viewLifecycleOwner) {
                            bottomSheetBinding.tvData2.text = buildString {
                                append(it?.toString())
                                append(" Classes")
                            }
                        }

                        bottomSheetBinding.btnYes.setOnClickListener {
                            vmLevel.deleteLevel(uidLevel).observe(viewLifecycleOwner) {
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

    private fun addLevel() {
        DialogUtil.showBottomSheet2Et(
            requireActivity(),
            "Add Level",
            "Level",
            "Semester",
            R.drawable.add,
            "Add"
        ) { bottomSheetBinding, bottomSheet ->

            bottomSheetBinding.etFirst.inputType = InputType.TYPE_CLASS_NUMBER
            bottomSheetBinding.etSecond.inputType = InputType.TYPE_CLASS_NUMBER

            bottomSheetBinding.btnYes.setOnClickListener {
                val etFirst = bottomSheetBinding.etFirst.text.toString()
                val etSecond = bottomSheetBinding.etSecond.text.toString()

                if (etFirst.isNotEmpty() && etSecond.isNotEmpty()) {
                    val dataLevel = DataClassLevel(etFirst, etSecond)
                    vmLevel.addLevel(dataLevel).observe(viewLifecycleOwner) {
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
                                    "Level Already Exist",
                                    R.drawable.copy
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