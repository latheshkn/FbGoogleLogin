package com.example.facebookgoogleauth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.firebase.FirebaseApp

import com.google.firebase.auth.*

class MainActivity : AppCompatActivity() {

    lateinit var callbackManager: CallbackManager
    lateinit var auth: FirebaseAuth

    lateinit var login_button: LoginButton
    private val TAG: String = "MainActivitycheck"
    lateinit var accessTokenTracker: AccessTokenTracker
    lateinit var authlisner: FirebaseAuth.AuthStateListener
    lateinit var googleSingIn: SignInButton

    lateinit var googleSignInClient: GoogleSignInClient
    val RC_SIGN_IN: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)


        login_button = findViewById(R.id.login_button)
        googleSingIn = findViewById(R.id.googleSingIn)



        auth = FirebaseAuth.getInstance()

        login_button.setPermissions("email", "public_profile")


        FacebookSdk.sdkInitialize(
            this
        )


        callbackManager = CallbackManager.Factory.create()

        login_button.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

            override fun onSuccess(result: LoginResult?) {

                Log.d(TAG, "onSuccess" + result)
                handleFacebookToken(result!!.accessToken)
            }

            override fun onCancel() {
            Log.d(TAG, "onCancl")

            }

            override fun onError(error: FacebookException?) {
                Log.d(TAG, "OnError" + error)

            }

        })


        authlisner = FirebaseAuth.AuthStateListener {


            val user = it.currentUser

            if (user != null) {
                val intent = Intent(this, LoginSuccessActivity::class.java)
                intent.putExtra("name", user.displayName)
                intent.putExtra("url", user.photoUrl.toString())
                startActivity(intent)
                finish()
            }
        }

        accessTokenTracker = object : AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(
                oldAccessToken: AccessToken?,
                currentAccessToken: AccessToken?
            ) {

                auth.signOut()
                googleSingIn.visibility = View.VISIBLE
            }
        }

//Google signIn
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSingIn.setOnClickListener({
            signIn()
        })

    }

    private fun handleFacebookToken(token: AccessToken) {


        Log.d("LoginCallback", "handlefacebooktoken" + token)

        val creadential = FacebookAuthProvider.getCredential(token.token)

        auth.signInWithCredential(creadential)

            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = auth.currentUser


                } else {
                    // If sign in fails, display a message to the user.

                    Log.d(TAG, "signInWithCredential:fail" + task.exception)
                    Toast.makeText(this, "something went wrong" + task.exception, Toast.LENGTH_LONG)
                        .show()

                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        googleSingIn.visibility = View.GONE
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            handleGoogleSignInResult(task)
        }
    }

    private fun handleGoogleSignInResult(task: com.google.android.gms.tasks.Task<GoogleSignInAccount>) {

        try {
            val acc = task!!.result
//            if the google sign in success handle firebase with google
            FirebaseGoogleAuth(acc)

            Toast.makeText(applicationContext, "google login success", Toast.LENGTH_LONG).show()
        } catch (exeption: Exception) {
            Toast.makeText(applicationContext, "google login failed", Toast.LENGTH_LONG).show()

        }
    }


    fun FirebaseGoogleAuth(acc: GoogleSignInAccount?) {

        val authCredential = GoogleAuthProvider.getCredential(acc!!.idToken, null)

        auth.signInWithCredential(authCredential).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success")
                val user = auth.currentUser


            } else {
                // If sign in fails, display a message to the user.

                Log.d(TAG, "signInWithCredential:fail" + it.exception)
                Toast.makeText(this, "something went wrong", Toast.LENGTH_LONG).show()

            }

        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authlisner)
    }

    override fun onStop() {

        super.onStop()
        if (authlisner != null) {
            auth.removeAuthStateListener(authlisner)
        }
    }

    fun signIn() {

        val intent = googleSignInClient.signInIntent

        startActivityForResult(intent, RC_SIGN_IN)

    }

    override fun onResume() {
        super.onResume()
    }
}