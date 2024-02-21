package com.example.mobiletracker

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.mobiletracker.databinding.ActivitySelectBinding
import com.example.mobiletracker.databinding.ActivityTrackerBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class TrackerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTrackerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnBack.setOnClickListener {
            startActivity(Intent(this,SelectActivity::class.java))
            finish()
        }

        binding.clTracker.setOnClickListener {
            closeKeyBoard()
        }

        binding.startTrackingBtn.setOnClickListener {
            closeKeyBoard()
            val email = binding.etEmail.text.toString()
            val errorEmailText = "Email id is required"
            val emailNotFountTxt = "Email not found"
            if (email.isEmpty()){
                binding.errorEmail.text = errorEmailText
                binding.errorEmail.visibility = View.VISIBLE
            }
            else{
                binding.progressBar.visibility = View.VISIBLE
                binding.llVisible.visibility = View.VISIBLE
                val database = Firebase.database
                val usersRef = database.getReference("Users")
                val userQuery = usersRef.orderByChild("email").equalTo(email)
                userQuery.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (userSnapshot in snapshot.children){
                                val userId = userSnapshot.key
                                val latitude = userSnapshot.child("latitude").getValue(Double::class.java)
                                val longitude = userSnapshot.child("longitude").getValue(Double::class.java)
                                val intent = Intent(this@TrackerActivity, SearchPersonActivity::class.java)
                                intent.putExtra("userId", userId)
                                intent.putExtra("latitude", latitude)
                                intent.putExtra("longitude", longitude)
                                startActivity(intent)
                                binding.errorEmail.visibility = View.GONE
                                binding.progressBar.visibility = View.GONE
                                binding.llVisible.visibility = View.GONE
                            }

                        } else {
                            binding.errorEmail.text = emailNotFountTxt
                            binding.errorEmail.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.GONE
                            binding.llVisible.visibility = View.GONE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("DatabaseError", error.message)
                        binding.progressBar.visibility = View.GONE
                        binding.llVisible.visibility = View.GONE
                    }

                })
            }
        }
    }

    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}