package com.schedule.rt.sync.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.schedule.rt.sync.R
import com.schedule.rt.sync.databinding.ActivitySignUpBinding
import com.schedule.rt.sync.databinding.DialogAlertBinding
import com.schedule.rt.sync.dataclass.DataClassUser

class ActivitySignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("users")

        binding.btnSignUp.setOnClickListener {

            binding.pbSignUp.visibility = android.view.View.VISIBLE

            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val retypePassword = binding.etRetypePassword.text.toString()

            if (email.isNotEmpty() || password.isNotEmpty() || retypePassword.isNotEmpty()) {
                if (password == retypePassword) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val uidUser = firebaseAuth.currentUser!!.uid
                            val dataUser = DataClassUser(uidUser = uidUser)
                            databaseReference.child(uidUser).setValue(dataUser).addOnSuccessListener {
                                val dialogAlertBinding = DialogAlertBinding.inflate(layoutInflater)
                                val dialogAlert = AlertDialog.Builder(this)
                                    .setView(dialogAlertBinding.root)
                                    .setCancelable(false)
                                    .create()
                                dialogAlert.window?.setBackgroundDrawableResource(android.R.color.transparent)
                                dialogAlertBinding.ivDialogAlert.setImageResource(R.drawable.check)
                                dialogAlertBinding.tvDialogConfirmation.text = "Sign Up Successful"
                                dialogAlert.show()

                                dialogAlert.setOnDismissListener {
                                    val intentMain = Intent(this, ActivityMain::class.java)
                                    startActivity(intentMain)
                                    binding.pbSignUp.visibility = android.view.View.INVISIBLE
                                    finish()
                                }

                                Handler(Looper.getMainLooper()).postDelayed({
                                    dialogAlert.dismiss()
                                }, 2000)
                            }
                        } else {
                            val dialogAlertBinding = DialogAlertBinding.inflate(layoutInflater)
                            val dialogAlert = AlertDialog.Builder(this)
                                .setView(dialogAlertBinding.root)
                                .setCancelable(false)
                                .create()
                            dialogAlert.window?.setBackgroundDrawableResource(android.R.color.transparent)
                            dialogAlertBinding.ivDialogAlert.setImageResource(R.drawable.fail)
                            dialogAlertBinding.tvDialogConfirmation.text = "Sign Up Fail"
                            dialogAlert.show()

                            Handler(Looper.getMainLooper()).postDelayed({
                                dialogAlert.dismiss()
                            }, 2000)
                        }
                    }
                } else {
                    binding.pbSignUp.visibility = android.view.View.INVISIBLE
                    Snackbar.make(this, binding.root, "Password Not Match", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                binding.pbSignUp.visibility = android.view.View.INVISIBLE
                Snackbar.make(this, binding.root, "Empty Fields Are Not Allowed", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}