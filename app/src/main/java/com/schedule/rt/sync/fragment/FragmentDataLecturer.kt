package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterDataLecturer
import com.schedule.rt.sync.databinding.FragmentDataLecturerBinding
import com.schedule.rt.sync.dataclass.DataClassLecturer
import com.schedule.rt.sync.function.capitalizeAfterDot
import com.schedule.rt.sync.function.capitalizeEachWord
import com.schedule.rt.sync.objectsingleton.DialogUtil
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelAdministrator
import com.schedule.rt.sync.viewmodel.ViewModelLecturer

class FragmentDataLecturer : Fragment() {

    private lateinit var binding: FragmentDataLecturerBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterRvLecturer: AdapterDataLecturer

    private val viewModelAdministrator: ViewModelAdministrator by activityViewModels()
    private val viewModelLecturer: ViewModelLecturer by activityViewModels()

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
        binding = FragmentDataLecturerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        actionBar()

        rvLecturer()

    }

    private fun actionBar() {
        binding.toolBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnAdd.setOnClickListener {
            addLecturer()
        }
    }

    private fun rvLecturer() {
        recyclerView = binding.rvLecturer
        adapterRvLecturer = AdapterDataLecturer()
        recyclerView.adapter = adapterRvLecturer

        viewModelLecturer.getLecturer()
        viewModelLecturer.dataLecturer.observe(viewLifecycleOwner) {
            adapterRvLecturer.updateRvLecturer(it)
            if (it.isEmpty()) {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            } else {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            }
        }

        adapterRvLecturer.setOnItemClickListener(object : AdapterDataLecturer.onItemClickListener {
            override fun onEditClick(position: Int) {
                DialogUtil.showBottomSheet2Et(
                    requireActivity(),
                    "Edit Lecturer",
                    "Name Lecturer",
                    "NIK Lecturer",
                    R.drawable.edit,
                    "Edit"
                ) { bottomSheetBinding, bottomSheet ->

                    val uidLecturer = adapterRvLecturer.dataClassLecturer[position].uidLecturer.toString()

                    bottomSheetBinding.etSecond.inputType = InputType.TYPE_CLASS_NUMBER

                    bottomSheetBinding.layoutAdministratorAccess.visibility = View.VISIBLE

                    viewModelLecturer.getLecturerByUid(uidLecturer).observe(viewLifecycleOwner) {
                        bottomSheetBinding.etFirst.setText(it?.nameLecturer)
                        bottomSheetBinding.etSecond.setText(it?.nikLecturer)

                        when (it?.administratorAccess) {
                            "True" -> {
                                bottomSheetBinding.swAdministratorAccess.isChecked = true
                            }

                            "False" -> {
                                bottomSheetBinding.swAdministratorAccess.isChecked = false
                            }
                        }
                    }

                    bottomSheetBinding.btnYes.setOnClickListener {
                        val etFirst =
                            bottomSheetBinding.etFirst.text.toString().capitalizeEachWord().capitalizeAfterDot()
                        val etSecond = bottomSheetBinding.etSecond.text.toString()
                        val AdministratorAccess = if (bottomSheetBinding.swAdministratorAccess.isChecked) "True" else "False"
                        val dataLecturer = DataClassLecturer(
                            nameLecturer = etFirst,
                            nikLecturer = etSecond,
                            uidLecturer = uidLecturer,
                            administratorAccess = AdministratorAccess
                        )

                        if (etFirst.isNotEmpty() && etSecond.isNotEmpty()) {
                            viewModelLecturer.editLecturer(dataLecturer).observe(viewLifecycleOwner) {
                                if (it != null) {
                                    when (it) {
                                        "Success" -> {
                                            DialogUtil.showToast(
                                                requireActivity(),
                                                "Edit Lecturer Successful",
                                                R.drawable.check
                                            )
                                            bottomSheet.dismiss()
                                        }

                                        "Nik Exist" -> {
                                            DialogUtil.showToast(
                                                requireActivity(),
                                                "NIK Lecturer Already Exist",
                                                R.drawable.warning
                                            )
                                        }

                                        "Fail" -> {
                                            DialogUtil.showToast(
                                                requireActivity(),
                                                "Edit Lecturer Fail",
                                                R.drawable.fail
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            DialogUtil.showToast(
                                requireActivity(),
                                "Please Fill All Field",
                                R.drawable.warning
                            )
                        }
                    }
                }
            }

            override fun onDeleteClick(position: Int) {
                DialogUtil.showBottomSheetConfirmation(
                    requireActivity(),
                    "Delete Lecturer",
                    R.drawable.delete,
                    "Delete",
                    { bottomSheetBinding, bottomSheet ->

                        bottomSheetBinding.tvData2.visibility = View.VISIBLE

                        val uidLecturer = adapterRvLecturer.dataClassLecturer[position].uidLecturer.toString()
                        viewModelLecturer.getLecturerByUid(uidLecturer).observe(viewLifecycleOwner) {
                            bottomSheetBinding.tvTittle.text = it?.nameLecturer
                            bottomSheetBinding.tvData1.text = it?.nikLecturer
                            bottomSheetBinding.tvData2.text = buildString {
                                append("Administrator Access : ")
                                append(it?.administratorAccess)
                            }
                        }

                        bottomSheetBinding.btnYes.setOnClickListener {
                            viewModelLecturer.deleteLecturer(uidLecturer).observe(viewLifecycleOwner) {
                                if (it != null) {
                                    when (it) {
                                        "Success" -> {
                                            DialogUtil.showToast(requireActivity(), "Delete Lecturer Successful", R.drawable.check)
                                            bottomSheet.dismiss()
                                        }
                                        "Fail" -> {
                                            DialogUtil.showToast(requireActivity(), "Delete Lecturer Fail", R.drawable.fail)
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

    private fun addLecturer() {
        DialogUtil.showBottomSheet2Et(
            requireActivity(),
            "Add Lecturer",
            "Name Lecturer",
            "NIK Lecturer",
            R.drawable.add,
            "Add"
        ) { bottomSheetBinding, bottomSheet ->

            bottomSheetBinding.etSecond.inputType = InputType.TYPE_CLASS_NUMBER

            bottomSheetBinding.layoutAdministratorAccess.visibility = View.VISIBLE

            var AdministratorAccess: String? = "False"
            bottomSheetBinding.swAdministratorAccess.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    AdministratorAccess = "True"
                } else {
                    AdministratorAccess = "False"
                }
            }

            bottomSheetBinding.btnYes.setOnClickListener {
                val etFirst = bottomSheetBinding.etFirst.text.toString().capitalizeEachWord()
                    .capitalizeAfterDot()
                val etSecond = bottomSheetBinding.etSecond.text.toString()
                val dataLecturer = DataClassLecturer(etFirst, etSecond, null, AdministratorAccess)

                if (etFirst.isNotEmpty() && etSecond.isNotEmpty()) {
                    viewModelLecturer.addLecturer(dataLecturer).observe(viewLifecycleOwner) {
                        if (it != null) {
                            when (it) {
                                "Success" -> {
                                    DialogUtil.showToast(
                                        requireActivity(),
                                        "Add Lecturer Successful",
                                        R.drawable.check
                                    )
                                    bottomSheet.dismiss()
                                }

                                "Nik Exist" -> {
                                    DialogUtil.showToast(
                                        requireActivity(),
                                        "NIK Lecturer Already Exist",
                                        R.drawable.warning
                                    )
                                }
                            }
                        }
                    }
                } else {
                    DialogUtil.showToast(
                        requireActivity(),
                        "Please Fill All Field",
                        R.drawable.warning
                    )
                }
            }
        }
    }
}