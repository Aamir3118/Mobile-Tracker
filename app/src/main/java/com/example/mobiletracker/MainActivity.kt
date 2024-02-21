package com.example.mobiletracker

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mobiletracker.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.smarteist.autoimageslider.SliderView


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageUrl: ArrayList<Int>
    private lateinit var sliderView: SliderView
    private lateinit var sliderAdapter: SliderAdapter
    private var isPasswordVisible = false
    private var isPasswordVisibleRegister = false
    private lateinit var auth: FirebaseAuth
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 1
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        sliderView = findViewById(R.id.slider)
        imageUrl = ArrayList()
        imageUrl.add(R.drawable.greece)
        imageUrl.add(R.drawable.maldives)
        imageUrl.add(R.drawable.nainital)
        imageUrl.add(R.drawable.turkey)

        sliderAdapter = SliderAdapter(imageUrl)
        sliderView.autoCycleDirection = SliderView.LAYOUT_DIRECTION_LTR
        sliderView.setSliderAdapter(sliderAdapter)
        sliderView.scrollTimeInSec = 3
        sliderView.isAutoCycle = true
        sliderView.startAutoCycle()

        val builder = AlertDialog.Builder(this@MainActivity, R.style.CustomAlertDialog)
        val viewGroup = findViewById<ViewGroup>(android.R.id.content)
        val dialogView: View =
            LayoutInflater.from(this).inflate(R.layout.login_layout, viewGroup, false)
        val editTextEmail = dialogView.findViewById<EditText>(R.id.et_email)
        val switchToSignUp = dialogView.findViewById<TextView>(R.id.switch_to_signup)
        val editTextPassword = dialogView.findViewById<EditText>(R.id.et_pass)
        val passVisibilityIcon = dialogView.findViewById<ImageView>(R.id.pass_visible)
        val loginBtn = dialogView.findViewById<LinearLayout>(R.id.button_login)
        val loginProgress = dialogView.findViewById<ProgressBar>(R.id.progress_bar)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)

        passVisibilityIcon.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            val transformationMethod = if (isPasswordVisible) null else PasswordTransformationMethod.getInstance()
            editTextPassword.transformationMethod = transformationMethod

            // Change eye icon based on visibility
            val eyeIcon =
                if (isPasswordVisible) R.drawable.visibility_black_24dp else R.drawable.visibility_off_black_24dp
            passVisibilityIcon.setImageResource(eyeIcon)
        }

        loginBtn.setOnClickListener {
            if (editTextEmail.text.toString() == "" || editTextPassword.text.toString() == ""){
                Toast.makeText(this,"Please enter all the fields",Toast.LENGTH_SHORT).show()
            }
            else{
                    loginProgress.visibility = View.VISIBLE
                    auth.signInWithEmailAndPassword(editTextEmail.text.toString(),editTextPassword.text.toString()).addOnCompleteListener {
                        if (it.isSuccessful){
                            Log.d("Login: ",auth.toString())
                            val homeIntent = Intent(this@MainActivity,HomeActivity::class.java)
                            startActivity(homeIntent)

                            loginProgress.visibility = View.GONE
                            loginBtn.isClickable = true
                            }
                        else{
                            loginProgress.visibility = View.GONE
                            Log.d("Login: ",auth.toString())
                            Toast.makeText(this,"Sign Up failed", Toast.LENGTH_SHORT).show()
                            loginBtn.isClickable = true
                        }
                    }
            }
        }

        switchToSignUp.setOnClickListener{
            val builderRegister = AlertDialog.Builder(this@MainActivity, R.style.CustomAlertDialog)
            val dialogViewRegister: View =
                LayoutInflater.from(this).inflate(R.layout.register_layout, viewGroup, false)
            val alertDialogRegister = builderRegister.setView(dialogViewRegister).create()
            val etEmailRegister = dialogViewRegister.findViewById<EditText>(R.id.et_email)
            val etPasswordRegister = dialogViewRegister.findViewById<EditText>(R.id.et_pass)
            val etConfirmPasswordRegister = dialogViewRegister.findViewById<EditText>(R.id.et_confirm_pass)
            val passVisibilityIconRegister = dialogViewRegister.findViewById<ImageView>(R.id.pass_visible)
            val btnRegister = dialogViewRegister.findViewById<LinearLayout>(R.id.button_register)
            val progressBar = dialogViewRegister.findViewById<ProgressBar>(R.id.progress_bar)
            val switchToSignIn = dialogViewRegister.findViewById<TextView>(R.id.switch_to_signin)
            passVisibilityIconRegister.setOnClickListener {
                isPasswordVisibleRegister = !isPasswordVisibleRegister
                val transformationMethod = if (isPasswordVisibleRegister) null else PasswordTransformationMethod.getInstance()
                etPasswordRegister.transformationMethod = transformationMethod

                // Change eye icon based on visibility
                val eyeIcon =
                    if (isPasswordVisibleRegister) R.drawable.visibility_black_24dp else R.drawable.visibility_off_black_24dp
                passVisibilityIconRegister.setImageResource(eyeIcon)
            }

            btnRegister.setOnClickListener {
                if (etEmailRegister.text.toString().isEmpty() || etPasswordRegister.text.toString().isEmpty() || etConfirmPasswordRegister.text.toString().isEmpty()){
                    Toast.makeText(this,"Please enter all the fields",Toast.LENGTH_SHORT).show()
                }
                else if (etPasswordRegister.text.toString() != etConfirmPasswordRegister.text.toString()){
                    Toast.makeText(this,"Password and Confirm password does not match",Toast.LENGTH_SHORT).show()
                }
                else{
                    progressBar.visibility = View.VISIBLE
                    auth.createUserWithEmailAndPassword(etEmailRegister.text.toString(),etPasswordRegister.text.toString()).addOnCompleteListener {
                        if (it.isSuccessful){
                            //getLocation()
                            Log.d("SignUp: ",auth.toString())
                            val user = auth.currentUser
                            val userId = user?.uid
                            saveUserDataToDatabase(userId,etEmailRegister.text.toString())
                            val homeIntent = Intent(this@MainActivity,HomeActivity::class.java)
                            startActivity(homeIntent)

                            progressBar.visibility = View.GONE
                            btnRegister.isClickable = true
                        }
                        else{
                            progressBar.visibility = View.GONE
                            Log.d("SignUp: ",auth.toString())
                            Toast.makeText(this,"Sign Up failed", Toast.LENGTH_SHORT).show()
                            btnRegister.isClickable = true
                        }
                    }
                }

            }

            switchToSignIn.setOnClickListener {
                alertDialog.show()
                alertDialogRegister.dismiss()
            }

            alertDialogRegister.show()
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun saveUserDataToDatabase(userId: String?,email:String){
        userId?.let {
            val database = Firebase.database
            val usersRef = database.getReference("Users")
            val userData = User(userId,email,0.0,0.0)

            usersRef.child(it).setValue(userData)
            Log.d("UserData: ",usersRef.toString())
        }
    }
}