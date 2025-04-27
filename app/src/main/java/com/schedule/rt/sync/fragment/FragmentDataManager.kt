package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterDataManager
import com.schedule.rt.sync.adapter.AdapterLecturer
import com.schedule.rt.sync.databinding.FragmentDataManagerBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelAdministrator
import com.schedule.rt.sync.viewmodel.ViewModelLecturer

class FragmentDataManager : Fragment() {

    private lateinit var binding: FragmentDataManagerBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterRvManager: AdapterDataManager

    private val viewModelAdministrator : ViewModelAdministrator by activityViewModels()
    private val viewModelLecturer : ViewModelLecturer by activityViewModels()

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
        binding = FragmentDataManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        actionBar()

        rvManager()

    }

    private fun actionBar() {
        binding.toolBar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val uidMajor = viewModelAdministrator.uidMajor
        viewModelAdministrator.getMajorByUid(uidMajor)
        viewModelAdministrator.dataMajorByUid.observe(viewLifecycleOwner) {
            binding.toolBar.title = buildString {
                append(it?.nameMajor)
                append(" Manager")
            }
        }

        binding.btnAdd.setOnClickListener {
            addManager()
        }
    }

    private fun rvManager() {
        recyclerView = binding.rvManager
        adapterRvManager = AdapterDataManager()
        recyclerView.adapter = adapterRvManager

        viewModelAdministrator.getManager()
        viewModelAdministrator.dataManager.observe(viewLifecycleOwner) {
            adapterRvManager.updateRvMajorManager(it)
            if (it.isNotEmpty()) {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            } else {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            }
        }

        adapterRvManager.setOnItemClickListener(object : AdapterDataManager.onItemClickListener {
            override fun onDeleteClick(position: Int) {
                DialogUtil.showBottomSheetConfirmation(
                    requireActivity(),
                    "Add Manager",
                    R.drawable.add,
                    "Add",
                    { bottomSheetBinding, bottomSheet ->

                        val uidLecturer = adapterRvManager.dataClassLecturer[position].uidLecturer
                        viewModelLecturer.getLecturerByUid(uidLecturer).observe(viewLifecycleOwner) {
                            bottomSheetBinding.tvTittle.text = it?.nameLecturer
                            bottomSheetBinding.tvData1.text = it?.nikLecturer
                        }

                        bottomSheetBinding.btnYes.setOnClickListener {
                            viewModelAdministrator.deleteManager(uidLecturer)
                            viewModelAdministrator.deleteManagerStatus.observe(viewLifecycleOwner) {
                                if (it != null) {
                                    when (it) {
                                        "Success" -> {
                                            Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                                            bottomSheet.dismiss()
                                        }
                                        "Fail" -> {
                                            Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    viewModelAdministrator.deleteManagerStatus.value = null
                                }
                            }
                        }
                    }
                )
            }
        })
    }

    private fun addManager() {
        val bottomSheetLecturer = BottomSheetLecturer()

        bottomSheetLecturer.show(parentFragmentManager, "BottomSheetLecturer")
        bottomSheetLecturer.setOnClickListener(object : BottomSheetLecturer.setOnClickListener {
            override fun onAddClick(position: Int, adapterRvLecturer: AdapterLecturer) {
                val uidMajorManager = adapterRvLecturer.dataClassLecturer[position].uidMajorManager
                val uidMajor = viewModelAdministrator.uidMajor

                if (uidMajorManager == uidMajor) {
                    DialogUtil.showToast(requireActivity(), "Lecturer Already Manage This Major", R.drawable.copy)
                } else {
                    DialogUtil.showBottomSheetConfirmation(
                        requireActivity(),
                        "Add Manager",
                        R.drawable.add,
                        "Add",
                        { bottomSheetBinding, bottomSheet ->

                            bottomSheetBinding.layoutAddManager.visibility = View.VISIBLE

                            val uidLecturer = adapterRvLecturer.dataClassLecturer[position].uidLecturer
                            viewModelLecturer.getLecturerByUid(uidLecturer).observe(viewLifecycleOwner) {
                                bottomSheetBinding.tvTittle.text = it?.nameLecturer
                                bottomSheetBinding.tvData1.text = it?.nikLecturer
                                viewModelAdministrator.getMajorManager(uidMajorManager).observe(viewLifecycleOwner) {
                                    val currentMajor = it?.nameMajor
                                    if (currentMajor != null) {
                                        bottomSheetBinding.tvCurrentMajor.text = it.nameMajor
                                    } else {
                                        bottomSheetBinding.tvCurrentMajor.text = buildString {
                                            append("-")
                                        }
                                    }
                                }
                            }

                            viewModelAdministrator.getMajorByUid(uidMajor)
                            viewModelAdministrator.dataMajorByUid.observe(viewLifecycleOwner) {
                                bottomSheetBinding.tvNewMajor.text = it?.nameMajor
                            }

                            bottomSheetBinding.btnYes.setOnClickListener {
                                viewModelAdministrator.addManager(uidLecturer)
                                viewModelAdministrator.addManagerStatus.observe(viewLifecycleOwner) {
                                    if (it != null) {
                                        when (it) {
                                            "Success" -> {
                                                Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                                                bottomSheet.dismiss()
                                                bottomSheetLecturer.dismiss()
                                            }
                                            "Fail" -> {
                                                Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        viewModelAdministrator.addManagerStatus.value = null
                                    }
                                }
                            }
                        }
                    )
                }
            }
        })
    }
}