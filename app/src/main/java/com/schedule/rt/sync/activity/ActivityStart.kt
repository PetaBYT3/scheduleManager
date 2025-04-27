package com.schedule.rt.sync.activity

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignIn.getClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.schedule.rt.sync.R
import com.schedule.rt.sync.databinding.ActivityStartBinding
import com.schedule.rt.sync.databinding.DialogAlertBinding
import com.schedule.rt.sync.dataclass.DataClassUser
import kotlin.jvm.java


class ActivityStart : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private val RC_SIGN_IN = 9001
    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("users")

        if (firebaseAuth.currentUser != null) {
            startActivity(Intent(this, ActivityMain::class.java))
            finish()
        }

        animateLogo()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = getClient(this, gso)

        binding.btnContinueWithGoogle.setOnClickListener {
            binding.pbContinueWithGoogle.visibility = View.VISIBLE
            signInWithGoogle()
        }

        binding.btnSignIn.setOnClickListener {
            binding.pbSignIn.visibility = View.VISIBLE
            val intentSignIn = Intent(this, ActivitySignIn::class.java)
            startActivity(intentSignIn)
        }

        binding.btnSignUp.setOnClickListener {
            binding.pbSignUp.visibility = View.VISIBLE
            val intentSignUp = Intent(this, ActivitySignUp::class.java)
            startActivity(intentSignUp)
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                val dialogAlertBinding = DialogAlertBinding.inflate(layoutInflater)
                val dialogAlert = AlertDialog.Builder(this)
                    .setView(dialogAlertBinding.root)
                    .setCancelable(false)
                    .create()
                dialogAlert.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialogAlertBinding.ivDialogAlert.setImageResource(R.drawable.fail)
                dialogAlertBinding.tvDialogConfirmation.text = "Sign In With Google Fail"
                dialogAlert.show()

                Handler(Looper.getMainLooper()).postDelayed({
                    dialogAlert.dismiss()
                }, 2000)

                binding.pbContinueWithGoogle.visibility = View.INVISIBLE
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uidUser = firebaseAuth.currentUser?.uid.toString()
                    val dataUser = DataClassUser(uidUser = uidUser)
                    databaseReference.child(uidUser).setValue(dataUser)

                    val dialogAlertBinding = DialogAlertBinding.inflate(layoutInflater)
                    val dialogAlert = AlertDialog.Builder(this)
                        .setView(dialogAlertBinding.root)
                        .setCancelable(false)
                        .create()
                    dialogAlert.window?.setBackgroundDrawableResource(android.R.color.transparent)
                    dialogAlertBinding.ivDialogAlert.setImageResource(R.drawable.check)
                    dialogAlertBinding.tvDialogConfirmation.text = "Sign In Successful"
                    dialogAlert.show()

                    dialogAlert.setOnDismissListener {
                        startActivity(Intent(this, ActivityMain::class.java))
                        finish()
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        dialogAlert.dismiss()
                    }, 2000)

                    binding.pbContinueWithGoogle.visibility = View.INVISIBLE
                } else {
                    val dialogAlertBinding = DialogAlertBinding.inflate(layoutInflater)
                    val dialogAlert = AlertDialog.Builder(this)
                        .setView(dialogAlertBinding.root)
                        .setCancelable(false)
                        .create()
                    dialogAlert.window?.setBackgroundDrawableResource(android.R.color.transparent)
                    dialogAlertBinding.ivDialogAlert.setImageResource(R.drawable.fail)
                    dialogAlertBinding.tvDialogConfirmation.text = "Sign In Fail"
                    dialogAlert.show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        dialogAlert.dismiss()
                    }, 2000)

                    binding.pbContinueWithGoogle.visibility = View.INVISIBLE
                }
            }
    }

    override fun onResume() {
        super.onResume()
        binding.pbSignIn.visibility = View.INVISIBLE
        binding.pbSignUp.visibility = View.INVISIBLE
        binding.pbContinueWithGoogle.visibility = View.INVISIBLE
    }

    private fun animateLogo() {
        val rotate45 = ObjectAnimator.ofFloat(binding.ivIcon, View.ROTATION, 0f, 45f).setDuration(1000)
        val pause45 = ObjectAnimator.ofFloat(binding.ivIcon, View.ROTATION, 45f, 45f).setDuration(1000)
        val rotate90 = ObjectAnimator.ofFloat(binding.ivIcon, View.ROTATION, 45f, 90f).setDuration(1000)
        val pause90 = ObjectAnimator.ofFloat(binding.ivIcon, View.ROTATION, 90f, 90f).setDuration(1000)
        val rotate135 = ObjectAnimator.ofFloat(binding.ivIcon, View.ROTATION, 90f, 135f).setDuration(1000)
        val pause135 = ObjectAnimator.ofFloat(binding.ivIcon, View.ROTATION, 135f, 135f).setDuration(1000)
        val rotate180 = ObjectAnimator.ofFloat(binding.ivIcon, View.ROTATION, 135f, 180f).setDuration(1000)
        val pause180 = ObjectAnimator.ofFloat(binding.ivIcon, View.ROTATION, 180f, 180f).setDuration(1000)
        val rotate225 = ObjectAnimator.ofFloat(binding.ivIcon, View.ROTATION, 180f, 225f).setDuration(1000)
        val pause225 = ObjectAnimator.ofFloat(binding.ivIcon, View.ROTATION, 225f, 225f).setDuration(1000)
        val rotate270 = ObjectAnimator.ofFloat(binding.ivIcon, View.ROTATION, 225f, 270f).setDuration(1000)
        val pause270 = ObjectAnimator.ofFloat(binding.ivIcon, View.ROTATION, 270f, 270f).setDuration(1000)
        val rotate315 = ObjectAnimator.ofFloat(binding.ivIcon, View.ROTATION, 270f, 315f).setDuration(1000)
        val pause315 = ObjectAnimator.ofFloat(binding.ivIcon, View.ROTATION, 315f, 315f).setDuration(1000)
        val rotate360 = ObjectAnimator.ofFloat(binding.ivIcon, View.ROTATION, 315f, 360f).setDuration(1000)
        val pause360 = ObjectAnimator.ofFloat(binding.ivIcon, View.ROTATION, 360f, 360f).setDuration(1000)

        val animatorSet = AnimatorSet().apply {
            playSequentially(rotate45, pause45, rotate90, pause90, rotate135, pause135, rotate180, pause180, rotate225, pause225, rotate270, pause270, rotate315, pause315, rotate360, pause360)

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator) {start()}
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
        animatorSet.start()
    }
}