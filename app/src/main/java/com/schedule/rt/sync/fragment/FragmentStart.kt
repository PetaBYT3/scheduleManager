package com.schedule.rt.sync.fragment

import AdapterStartCarrousel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.schedule.rt.sync.R
import com.schedule.rt.sync.activity.ActivityMain
import com.schedule.rt.sync.databinding.FragmentStartBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithoutBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceToastFragment
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FragmentStart : Fragment() {

    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!

    private val vmUser: ViewModelUser by activityViewModels()

    private lateinit var viewPager2: ViewPager2
    private val scrollInterval = 3000L
    private var autoScrollJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = TransitionUtil.enterTransition()
        returnTransition = TransitionUtil.returnTransition()
        exitTransition = TransitionUtil.exitTransition()
        reenterTransition = TransitionUtil.reenterTransition()

        sharedElementEnterTransition = TransitionUtil.sharedElementEnterTransition(requireActivity())
        sharedElementReturnTransition = TransitionUtil.sharedElementReturnTransition(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignIn.setOnClickListener {
            replaceFragmentWithBackStack(R.id.startFragmentContainer, FragmentSignIn(), "startContainer")
        }

        binding.btnSignUp.setOnClickListener {
            replaceFragmentWithBackStack(R.id.startFragmentContainer, FragmentSignUp(), "startContainer")
        }

        binding.btnContinueWithGoogle.setOnClickListener {
            binding.pbContinueWithGoogle.visibility = View.VISIBLE
            signInWithGoogle()
        }

        viewPagerCarrousel()
    }

    private fun viewPagerCarrousel() {
        viewPager2 = binding.slideAnimation
        val imageList = listOf(
            R.drawable.schedule,
            R.drawable.add_schedule,
            R.drawable.edit_schedule,
            R.drawable.delete_schedule
        )

        val adapterCarrousel = AdapterStartCarrousel(imageList)
        viewPager2.adapter = adapterCarrousel
        viewPager2.isUserInputEnabled = false

        autoScrollJob()
    }

    private fun autoScrollJob() {
        autoScrollJob?.cancel()

        autoScrollJob = lifecycleScope.launch {
            while (isActive) {
                delay(scrollInterval)
                val nextItem = viewPager2.currentItem + 1
                viewPager2.setCurrentItem(nextItem, true)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        autoScrollJob?.cancel()
    }

    override fun onResume() {
        super.onResume()
        autoScrollJob()
    }

    private fun signInWithGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(requireContext())

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = requireContext()
                )
                handleSignInResult(result)
            } catch (e: Exception) {

            }
        }
    }

    private fun handleSignInResult(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        firebaseAuthWithGoogle(idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("SignIn", "Invalid ID token: ${e.message}")
                    }
                } else {
                    Log.e("SignIn", "Unexpected credential type: ${credential.type}")
                }
            }
            else -> {
                Log.e("SignIn", "Unsupported credential type")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                vmUser.addUserFromGoogle().observe(viewLifecycleOwner) {
                    when (it) {
                        "Success" -> {
                            vmUser.getUser().observe(viewLifecycleOwner) {
                                val uidMajor = it?.uidMajor
                                val uidLevel = it?.uidLevel
                                val uidClasses = it?.uidClasses
                                val uidLecturer = it?.uidLecturer
                                if (uidMajor != null && uidLevel != null && uidClasses != null || uidLecturer != null) {
                                    startActivity(Intent(requireActivity(), ActivityMain::class.java))
                                    requireActivity().finish()
                                } else {
                                    replaceFragmentWithoutBackStack(
                                        R.id.startFragmentContainer,
                                        FragmentSelectRole(R.id.startFragmentContainer, R.id.startToastContainer)
                                    )
                                }
                            }
                            binding.pbContinueWithGoogle.visibility = View.GONE
                        }
                        "Fail" -> {
                            replaceToastFragment(
                                R.id.startToastContainer,
                                FragmentToast(R.drawable.fail, "Sign In Failed")
                            )
                            binding.pbContinueWithGoogle.visibility = View.GONE
                        }
                    }
                }
            } else {
                replaceToastFragment(
                    R.id.startToastContainer,
                    FragmentToast(R.drawable.fail, "${it.exception?.message}")
                )
                binding.pbContinueWithGoogle.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}