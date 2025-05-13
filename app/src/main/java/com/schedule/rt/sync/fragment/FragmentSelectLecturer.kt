package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterLecturer
import com.schedule.rt.sync.databinding.FragmentSelectLecturerBinding
import com.schedule.rt.sync.dataclass.DataClassLecturer
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelLecturer
import com.schedule.rt.sync.viewmodel.ViewModelMajor

class FragmentSelectLecturer : Fragment() {

    private var _binding: FragmentSelectLecturerBinding? = null
    private val binding get() = _binding!!

    private val vmLecturer: ViewModelLecturer by activityViewModels()
    private val vmMajor: ViewModelMajor by activityViewModels()

    var onViewCreated: ((FragmentSelectLecturerBinding) -> Unit)? = null
    var onAddClick: ((DataClassLecturer) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSelectLecturerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvLecturer.post {
            TransitionUtil.slideUpTransition(binding.rvLecturer)
        }

        onViewCreated?.invoke(binding)

        rootMaxHeight()
    }

    private fun rootMaxHeight() {

        val displayMetrics = requireActivity().windowManager.currentWindowMetrics
        val insets = displayMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        val fullScreen = displayMetrics.bounds.height() - insets.bottom - insets.top
        val halfScreen = fullScreen / 2
        binding.root.layoutParams.height = halfScreen
        binding.root.requestLayout()

        var isFullScreen = false

        binding.btnFullscreen.setOnClickListener {
            val currentHeight = binding.root.height
            if (isFullScreen) {
                TransitionUtil.animateHeight(binding.root, currentHeight, halfScreen)
                binding.ivFullscreen.setImageResource(R.drawable.fullscreen)
                isFullScreen = false
            } else {
                TransitionUtil.animateHeight(binding.root, currentHeight, fullScreen)
                binding.ivFullscreen.setImageResource(R.drawable.close_fullscreen)
                isFullScreen = true
            }
        }
    }

    fun recyclerView(
        tvData1: Boolean?,
        tvData2: Boolean?,
        tvData3: Boolean?,
        tvData4: Boolean?,
        tvData5: Boolean?
    ) {
        val recyclerView: RecyclerView = binding.rvLecturer
        val adapter = AdapterLecturer(
            vmMajor, viewLifecycleOwner,
            tvData1 = tvData1,
            tvData2 = tvData2,
            tvData3 = tvData3,
            tvData4 = tvData4,
            tvData5 = tvData5,
            btnFirst = false,
            btnSecond = false,
            ivNext = R.drawable.add,
            btnNext = true,
            onNextClick = {
                onAddClick?.invoke(it)
            }
        )

        recyclerView.adapter = adapter

        vmLecturer.getLecturer().observe(viewLifecycleOwner) {
            adapter.updateData(it)
            if (it.isNullOrEmpty()) {
                binding.pbRvLecturer.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            } else {
                binding.pbRvLecturer.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}