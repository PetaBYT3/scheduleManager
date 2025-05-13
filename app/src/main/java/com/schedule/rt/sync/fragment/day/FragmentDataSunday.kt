package com.schedule.rt.sync.fragment.day

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.schedule.rt.sync.databinding.FragmentDataSundayBinding

class FragmentDataSunday : Fragment() {

    private var _binding: FragmentDataSundayBinding? = null
    private val binding get() = _binding!!

    var onViewCreated: ((FragmentDataSundayBinding) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDataSundayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onViewCreated?.invoke(binding)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}