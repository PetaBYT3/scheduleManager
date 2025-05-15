package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.schedule.rt.sync.databinding.FragmentInputBinding

class FragmentInputTest() : Fragment() {

    private lateinit var binding: FragmentInputBinding

    var onViewCreated: ((FragmentInputBinding, fragmentLifecycleOwner: LifecycleOwner) -> Unit)? = null
    var onDestroyView: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onViewCreated?.invoke(binding, viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onDestroyView?.invoke()
    }
}