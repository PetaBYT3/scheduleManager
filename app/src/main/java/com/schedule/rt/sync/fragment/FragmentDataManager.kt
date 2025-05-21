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
import com.schedule.rt.sync.adapter.AdapterLecturer
import com.schedule.rt.sync.databinding.FragmentDataManagerBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.showToastFragment
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelData
import com.schedule.rt.sync.viewmodel.ViewModelLecturer
import com.schedule.rt.sync.viewmodel.ViewModelMajor

class FragmentDataManager : Fragment() {

    private var _binding: FragmentDataManagerBinding? = null
    private val binding get() = _binding!!

    private val vmData: ViewModelData by activityViewModels()
    private val vmMajor: ViewModelMajor by activityViewModels()
    private val vmLecturer : ViewModelLecturer by activityViewModels()

    private val fragmentTag = "dataManager"

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
        _binding = FragmentDataManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        binding.toolBar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        val uidMajor = vmData.uidMajor.value
        vmMajor.getMajorByUid(uidMajor).observe(viewLifecycleOwner) {
            binding.clToolBar.title = buildString {
                append(it?.nameMajor)
                append(" Manager")
            }
        }

        binding.btnAdd.setOnClickListener {
            addManager()
        }

        rvManager()

    }

    private fun rvManager() {
        val recyclerView: RecyclerView = binding.rvManager
        val adapter = AdapterLecturer(
            vmMajor, viewLifecycleOwner,
            tvData1 = true,
            btnFirst = false,
            btnSecond = true,
            btnNext = false,
            onSecondClick = {
                val uidLecturer = it.uidLecturer
                val fragmentCard = FragmentCard().apply {
                    onViewCreated = { cardBinding ->

                        cardBinding.toolBar.setNavigationOnClickListener {
                            requireActivity().supportFragmentManager.popBackStack()
                        }

                        cardBinding.toolBar.title = "Delete Manager"
                        cardBinding.ivYes.setImageResource(R.drawable.delete)
                        cardBinding.tvYes.text = "Delete"

                        vmLecturer.getLecturerByUid(uidLecturer).observe(viewLifecycleOwner) {
                            cardBinding.tvTitle.text = it?.nameLecturer
                            cardBinding.tvData1.text = it?.nikLecturer
                        }

                        cardBinding.tvData2.visibility = View.GONE
                        cardBinding.tvData3.visibility = View.GONE
                        cardBinding.tvData4.visibility = View.GONE
                        cardBinding.tvData5.visibility = View.GONE

                        cardBinding.btnYes.setOnClickListener {
                            vmLecturer.deleteLecturerManager(uidLecturer).observe(viewLifecycleOwner) {
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
            }
        )

        recyclerView.adapter = adapter

        val uidMajor = vmData.uidMajor.value
        vmLecturer.getLecturerByManager(uidMajor.toString()).observe(viewLifecycleOwner) {
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

    private fun addManager() {
        val fragmentLecturer = FragmentSelectLecturer().apply {
            onViewCreated = { fragmentSelect ->
                fragmentSelect.toolBar.setNavigationOnClickListener {
                    requireActivity().supportFragmentManager.popBackStack()
                }

                recyclerView(
                    tvData1 = true,
                    tvData2 = false,
                    tvData3 = true,
                    tvData4 = false,
                    tvData5 = false
                )
            }
            onAddClick = {
                val uidLecturer = it.uidLecturer

                val fragmentCard = FragmentCard().apply {
                    onViewCreated = { cardBinding ->

                        cardBinding.toolBar.setNavigationOnClickListener {
                            requireActivity().supportFragmentManager.popBackStack()
                        }

                        cardBinding.ivCard.setImageResource(R.drawable.profile)
                        cardBinding.tvData3.visibility = View.GONE
                        cardBinding.tvData4.visibility = View.GONE
                        cardBinding.tvData5.visibility = View.GONE
                        cardBinding.toolBar.title = "Add Manager"
                        cardBinding.ivYes.setImageResource(R.drawable.add)
                        cardBinding.tvYes.text = "Add"

                        vmLecturer.getLecturerByUid(uidLecturer).observe(viewLifecycleOwner) {
                            cardBinding.tvTitle.text = it?.nameLecturer
                            cardBinding.tvData1.text = it?.nikLecturer
                        }

                        val uidMajor = vmData.uidMajor.value
                        vmMajor.getMajorByUid(uidMajor).observe(viewLifecycleOwner) {
                            cardBinding.tvData2.text = it?.nameMajor
                        }

                        cardBinding.btnYes.setOnClickListener {
                            vmLecturer.addLecturerManager(uidLecturer, uidMajor).observe(viewLifecycleOwner) {
                                when (it) {
                                    "Success" -> {
                                        showToastFragment(FragmentToast(R.drawable.check, "Add Success"))
                                        requireActivity().supportFragmentManager.popBackStack()
                                    }
                                    "Fail" -> {
                                        showToastFragment(FragmentToast(R.drawable.fail, "Add Failed"))
                                    }
                                }
                            }
                        }
                    }
                }
                replaceFragmentWithBackStack(R.id.mainBottomSheetContainer, fragmentCard, fragmentTag)
            }
        }
        requireActivity().supportFragmentManager.popBackStack(fragmentTag, POP_BACK_STACK_INCLUSIVE)
        replaceFragmentWithBackStack(R.id.mainBottomSheetContainer, fragmentLecturer, fragmentTag)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().supportFragmentManager.popBackStack(fragmentTag, POP_BACK_STACK_INCLUSIVE)
        _binding = null
    }
}