package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.schedule.rt.sync.R
import com.schedule.rt.sync.databinding.FragmentMessageBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.removeFragmentFromContainer

class FragmentMessage(
    private val tittle: String,
    private val message: String,
    private val ivYes: Int,
    private val tvYes: String,
    private var onYesClick: () -> Unit,
) : Fragment() {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolBar.setNavigationOnClickListener {
            removeFragmentFromContainer(R.id.mainBottomSheetContainer)
        }

        binding.toolBar.title = tittle

        binding.tvMessage.text = message

        binding.ivYes.setImageResource(ivYes)
        binding.tvYes.text = tvYes

        binding.btnYes.setOnClickListener {
            onYesClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}