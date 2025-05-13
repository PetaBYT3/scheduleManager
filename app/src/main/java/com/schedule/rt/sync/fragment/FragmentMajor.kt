package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.adapter.AdapterMajor
import com.schedule.rt.sync.databinding.FragmentMajorBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelData
import com.schedule.rt.sync.viewmodel.ViewModelLevel
import com.schedule.rt.sync.viewmodel.ViewModelMajor
import com.schedule.rt.sync.viewmodel.ViewModelUser

class FragmentMajor(
    private val fragmentContainer: Int,
    private val toastContainer: Int,
) : Fragment() {

    private var _binding: FragmentMajorBinding? = null
    private val binding get() = _binding!!

    private val vmData: ViewModelData by activityViewModels()
    private val vmUser: ViewModelUser by activityViewModels()
    private val vmMajor: ViewModelMajor by activityViewModels()
    private val vmLevel: ViewModelLevel by activityViewModels()

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
        _binding = FragmentMajorBinding.inflate(inflater, container, false)
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

        recyclerView()
    }

    private fun recyclerView() {
        val recyclerView: RecyclerView = binding.rvMajor
        val adapter = AdapterMajor(
            btnFirst = false,
            btnSecond = false,
            btnNext = true,
            onNextClick = {
                vmLevel.uidMajorReference(it.uidMajor)
                vmData.sendUidMajor(it.uidMajor.toString())
                replaceFragmentWithBackStack(
                    fragmentContainer,
                    FragmentLevel(fragmentContainer, toastContainer),
                    null
                )
            }
        )
        recyclerView.adapter = adapter

        vmMajor.getMajors().observe(viewLifecycleOwner) {
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