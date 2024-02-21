package com.example.mobiletracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.mobiletracker.databinding.ActivityMainBinding
import com.example.mobiletracker.databinding.ActivitySelectBinding
import com.google.firebase.auth.FirebaseAuth
import com.smarteist.autoimageslider.SliderView

class SelectActivity : AppCompatActivity() {
    private lateinit var imageUrl: ArrayList<Int>
    private lateinit var sliderView: SliderView
    private lateinit var sliderAdapter: SliderAdapter
    private lateinit var binding: ActivitySelectBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null){
            Log.d("UserOnline","Yes")
        }
        else{
            Log.d("UserOnline","No")
        }

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

        binding.btnTracked.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val homeIntent = Intent(this@SelectActivity, HomeActivity::class.java)
                startActivity(homeIntent)
            }else{
                startActivity(Intent(this,MainActivity::class.java))
            }
        }
        binding.btnTracker.setOnClickListener {
            startActivity(Intent(this,TrackerActivity::class.java))
        }
    }
}