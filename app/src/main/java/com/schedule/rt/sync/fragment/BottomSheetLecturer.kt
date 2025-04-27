package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.schedule.rt.sync.adapter.AdapterLecturer
import com.schedule.rt.sync.databinding.BottomSheetLecturerBinding
import com.schedule.rt.sync.viewmodel.ViewModelAdministrator
import com.schedule.rt.sync.viewmodel.ViewModelLecturer

class BottomSheetLecturer : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetLecturerBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterRvLecturer: AdapterLecturer

    private val viewModelAdministrator: ViewModelAdministrator by activityViewModels()
    private val viewModelLecturer: ViewModelLecturer by activityViewModels()

    private var onClickListener: setOnClickListener? = null

    override fun onStart() {
        super.onStart()

//        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
//
//        val behavior = BottomSheetBehavior.from(bottomSheet!!)
//        behavior.state = BottomSheetBehavior.STATE_EXPANDED
//        behavior.skipCollapsed = true // ⬅️ Ini penting biar langsung skip STATE_COLLAPSED
//        behavior.isHideable = true     // ⬅️ Harus bisa di-hide supaya bisa dismiss
//
//        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
//            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
//                    dismiss() // ⬅️ Langsung dismiss saat sudah dalam posisi tersembunyi
//                }
//            }
//
//            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
//        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = BottomSheetLecturerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvLecturer()
    }

    private fun rvLecturer() {
        recyclerView = binding.rvLecturer
        adapterRvLecturer = AdapterLecturer(viewModelAdministrator, viewLifecycleOwner)
        recyclerView.adapter = adapterRvLecturer

        viewModelLecturer.getLecturer()
        viewModelLecturer.dataLecturer.observe(viewLifecycleOwner) {
            adapterRvLecturer.updateRvLecturer(it)
            if (it.isNotEmpty()) {
                binding.pbRvLecturer.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            } else {
                binding.pbRvLecturer.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            }
        }

        adapterRvLecturer.setOnItemClickListener(object : AdapterLecturer.onItemClickListener {
            override fun onAddClick(position: Int) {
                onClickListener?.onAddClick(position, adapterRvLecturer)
            }
        })
    }

    interface setOnClickListener {
        fun onAddClick(position: Int, adapterRvLecturer: AdapterLecturer)
    }

    fun setOnClickListener(onClickListener: setOnClickListener) {
        this.onClickListener = onClickListener
    }
}