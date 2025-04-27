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
import com.schedule.rt.sync.adapter.AdapterBuilding
import com.schedule.rt.sync.databinding.FragmentBuildingBinding
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelAdministrator
import com.schedule.rt.sync.viewmodel.ViewModelBuilding
import com.schedule.rt.sync.viewmodel.ViewModelRoom
import com.schedule.rt.sync.viewmodel.ViewModelSchedule

class FragmentBuilding : Fragment() {

    private lateinit var binding: FragmentBuildingBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterRvBuilding: AdapterBuilding

    private val viewModelAdministrator: ViewModelAdministrator by activityViewModels()
    private val viewModelBuilding: ViewModelBuilding by activityViewModels()

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
        binding = FragmentBuildingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        actionBar()

        rvBuilding()
    }

    private fun actionBar() {
        binding.toolBar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun rvBuilding() {
        recyclerView = binding.rvBuilding
        adapterRvBuilding = AdapterBuilding(viewModelBuilding, viewLifecycleOwner)
        recyclerView.adapter = adapterRvBuilding

        viewModelBuilding.getBuilding()
        viewModelBuilding.dataBuilding.observe(viewLifecycleOwner) {
            adapterRvBuilding.updateRvBuilding(it)
            if (it.isNotEmpty()) {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            } else {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            }
        }

        adapterRvBuilding.setOnItemClickListener(object : AdapterBuilding.onItemClickListener {
            override fun onItemClick(position: Int) {
                val uidBuilding = adapterRvBuilding.dataClassBuilding[position].uidBuilding

                vmRoom.uidBuilding = uidBuilding
                vmSchedule.uidBuilding = uidBuilding

                requireActivity().supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace(R.id.fragmentContainer, FragmentRoom::class.java, null)
                    addToBackStack(null)
                }
            }
        })
    }
}