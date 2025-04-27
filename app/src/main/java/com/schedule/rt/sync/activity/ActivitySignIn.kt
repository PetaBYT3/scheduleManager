package com.schedule.rt.sync.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.schedule.rt.sync.R
import com.schedule.rt.sync.databinding.ActivitySignInBinding
import com.schedule.rt.sync.databinding.DialogAlertBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil

class ActivitySignIn : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("users")

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSignIn.setOnClickListener {
            binding.pbSignIn.visibility = android.view.View.VISIBLE

            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
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
                            val intentMain = Intent(this, ActivityMain::class.java)
                            startActivity(intentMain)
                            binding.pbSignIn.visibility = android.view.View.INVISIBLE
                            finish()
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            dialogAlert.dismiss()
                        }, 2000)
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

                        binding.pbSignIn.visibility = android.view.View.INVISIBLE
                    }
                }
            } else {
                binding.pbSignIn.visibility = android.view.View.INVISIBLE
                DialogUtil.showToast(
                    context = this,
                    message = "Please Fill All Fields",
                    icon = R.drawable.warning
                )
            }
        }
    }
}