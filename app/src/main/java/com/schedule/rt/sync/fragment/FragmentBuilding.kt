package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterBuilding
import com.schedule.rt.sync.databinding.FragmentBuildingBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelBuilding
import com.schedule.rt.sync.viewmodel.ViewModelRoom
import com.schedule.rt.sync.viewmodel.ViewModelSchedule

class FragmentBuilding(
    var btnAddSchedule: Boolean? = null,
    var btnEditSchedule: Boolean? = null
) : Fragment() {

    private var _binding: FragmentBuildingBinding? = null
    private val binding get() = _binding!!

    private val vmBuilding: ViewModelBuilding by activityViewModels()
    private val vmRoom: ViewModelRoom by activityViewModels()
    private val vmSchedule: ViewModelSchedule by activityViewModels()

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
        _binding = FragmentBuildingBinding.inflate(inflater, container, false)
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

        rvBuilding()
    }

    private fun rvBuilding() {
        val recyclerView: RecyclerView = binding.rvBuilding
        val adapter = AdapterBuilding(
            btnFirst = false,
            btnSecond = false,
            btnNext = true,
            onNextClick = {
                val uidBuilding = it.uidBuilding
                vmRoom.uidBuildingReference(uidBuilding)
                vmSchedule.uidBuildingReference(uidBuilding)
                val fragmentRoom = FragmentRoom(btnAddSchedule = btnAddSchedule, btnEditSchedule = btnEditSchedule)
                replaceFragmentWithBackStack(R.id.mainFragmentContainer, fragmentRoom, null)
            }
        )

        recyclerView.adapter = adapter

        vmBuilding.getBuilding().observe(viewLifecycleOwner) {
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