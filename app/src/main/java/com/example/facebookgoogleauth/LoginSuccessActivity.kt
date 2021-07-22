package com.example.facebookgoogleauth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso

class LoginSuccessActivity : AppCompatActivity() {
    lateinit var imgProfile: ImageView
    lateinit var tvName: TextView
    lateinit var googleSingOut: Button
    lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_success)

        imgProfile = findViewById(R.id.imgProfile)
        tvName = findViewById(R.id.tvName)
        googleSingOut = findViewById(R.id.googleSingOut)

        firebaseAuth = FirebaseAuth.getInstance()

        val extras = intent.extras


        updateUI(extras!!.getString("name", "null"), extras.getString("url", "null"))

        googleSingOut.setOnClickListener({
            firebaseAuth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        })

    }

    fun updateUI(name: String, url: String) {

        tvName.text = name

        Picasso.get().load(url).into(imgProfile)
    }


}