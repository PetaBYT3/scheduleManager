package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.adapter.AdapterClasses
import com.schedule.rt.sync.databinding.FragmentClassesBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelClasses
import com.schedule.rt.sync.viewmodel.ViewModelData
import com.schedule.rt.sync.viewmodel.ViewModelLevel
import com.schedule.rt.sync.viewmodel.ViewModelMajor
import com.schedule.rt.sync.viewmodel.ViewModelUser

class FragmentClasses(
    private val fragmentContainer: Int,
    private val toastContainer: Int,
) : Fragment() {

    private var _binding : FragmentClassesBinding? = null
    private val binding get() = _binding!!

    private val vmData: ViewModelData by activityViewModels()
    private val vmUser: ViewModelUser by activityViewModels()
    private val vmMajor: ViewModelMajor by activityViewModels()
    private val vmLevel: ViewModelLevel by activityViewModels()
    private val vmClasses: ViewModelClasses by activityViewModels()

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
        _binding = FragmentClassesBinding.inflate(inflater, container, false)
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

        val uidMajor = vmLevel.uidMajorReference.value
        val uidLevel = vmClasses.uidLevelReference.value
        vmMajor.getMajorByUid(uidMajor).observe(viewLifecycleOwner) {
            val nameMajor = it?.nameMajor
            vmLevel.getLevelByUid(uidLevel).observe(viewLifecycleOwner) {
                val nameLevel = it?.level

                binding.clToolBar.title = buildString {
                    append(nameMajor)
                    append(", ")
                    append("Level $nameLevel")
                }
            }
        }

        recyclerView()
    }

    private fun recyclerView() {
        val recyclerView: RecyclerView = binding.rvClasses
        val adapter = AdapterClasses(
            btnFirst = false,
            btnSecond = false,
            btnNext = true,
            onNextClick = {dataClass ->
                vmData.sendUidClass(dataClass.uidClasses.toString())
                replaceFragmentWithBackStack(fragmentContainer, FragmentName(fragmentContainer, toastContainer), null)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}