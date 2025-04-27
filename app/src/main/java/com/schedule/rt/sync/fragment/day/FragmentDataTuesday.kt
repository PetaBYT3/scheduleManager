package com.schedule.rt.sync.fragment.day

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterDataSchedule
import com.schedule.rt.sync.databinding.FragmentDataTuesdayBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil
import com.schedule.rt.sync.viewmodel.ViewModelLecturer
import com.schedule.rt.sync.viewmodel.ViewModelSchedule

class FragmentDataTuesday : Fragment() {

    private lateinit var binding: FragmentDataTuesdayBinding

    private lateinit var rv: RecyclerView
    private lateinit var adapterRv: AdapterDataSchedule

    private val vmLecturer: ViewModelLecturer by activityViewModels()
    private val vmSchedule: ViewModelSchedule by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDataTuesdayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView()
    }

    private fun recyclerView() {
        rv = binding.rvTuesday
        adapterRv = AdapterDataSchedule(vmLecturer, viewLifecycleOwner)
        rv.adapter = adapterRv

        vmSchedule.getSchedule("tuesday").observe(viewLifecycleOwner) {
            adapterRv.updateRvSchedule(it)
            if (it.isNotEmpty()) {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            } else {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            }
        }

        adapterRv.setOnItemClickListener(object : AdapterDataSchedule.onItemClickListener {
            override fun onDeleteClick(position: Int) {
                DialogUtil.showBottomSheetConfirmation(
                    requireActivity(),
                    "Delete Schedule",
                    R.drawable.delete,
                    "Delete",
                    { bottomSheetBinding, bottomSheet ->

                        bottomSheetBinding.btnYes.setOnClickListener {
                            val uidCourse = adapterRv.dataClassCourse[position].uidCourse
                            vmSchedule.deleteSchedule(uidCourse).observe(viewLifecycleOwner) {
                                when (it) {
                                    "Success" -> {
                                        DialogUtil.showToast(requireActivity(), "Success", R.drawable.check)
                                        bottomSheet.dismiss()
                                    }
                                    "Fail" -> {
                                        DialogUtil.showToast(requireActivity(), "Fail", R.drawable.fail)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        })
    }
}