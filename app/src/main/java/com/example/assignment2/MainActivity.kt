package com.example.assignment2

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

/**
 * This is the starting class of the project. It creates the sign up/sign in activity.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var fireStore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_account)

        fireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val signUpSignInButton = findViewById<Button>(R.id.signUpSignInButton)
        val edtTxtInputMail = findViewById<EditText>(R.id.inputMail)
        val edtTxtInputPassword = findViewById<EditText>(R.id.inputPassword)
        val loginButton = findViewById<Button>(R.id.login_button)

        /*this button has two states: "SIGN UP"/"SIGN IN"
          checks that the input is not empty and if the text of the button is "sign up" -> creates
          a new account, else signs in the user
         */
        signUpSignInButton.setOnClickListener {
            val email = edtTxtInputMail.text.toString().trim()
            val password = edtTxtInputPassword.text.toString().trim()
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                edtTxtInputMail.error = getString(R.string.invalid_credentials)
            } else if (signUpSignInButton.text == getString(R.string.sign_up)) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this.applicationContext,
                                getString(R.string.user_created),
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(Intent(this, TopicActivity::class.java))
                        } else {
                            Toast.makeText(
                                this.applicationContext,
                                getString(R.string.user_creation_error) + (task.exception?.message),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(
                                this.applicationContext,
                                getString(R.string.sign_in_success),
                                Toast.LENGTH_SHORT
                            ).show()
                            auth.currentUser

                            startActivity(Intent(this, NewsActivity::class.java))
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(
                                this.applicationContext,
                                getString(R.string.auth_failed) + (task.exception?.message),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
        //when this button is pressed, it's text changes as well as for the sign in/sign up button
        loginButton.setOnClickListener {
            if (loginButton.text == getString(R.string.already_have_an_account_log_in)) {
                signUpSignInButton.text = getString(R.string.sign_in)
                loginButton.text = getString(R.string.no_account_create_one)
            } else {
                signUpSignInButton.text = getString(R.string.sign_up)
                loginButton.text = getString(R.string.already_have_an_account_log_in)
            }
        }
        //end of onCreate()
    }

    /* Checks with firebase for existence of user, if true, the user is sent directly to the news
     page, skipping the layout to choose topics(because he has already chosen them)
    */
    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            Log.i("1234", getString(R.string.users_already_exists))
            startActivity(Intent(this, NewsActivity::class.java))
        } else {
            Log.i("1234", getString(R.string.inexistent_user))
        }
    }
}