package com.example.assignment2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * This class lets the user choose his preferred topics and sets them in FireStore
 */
class TopicActivity : AppCompatActivity() {
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userID: String

    private var selectedTopicList = arrayListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.topic_screen)

        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser!!.uid
        fireStore = FirebaseFirestore.getInstance()

        val btnNext: Button = findViewById(R.id.nextButton)
        // if the user selected at least 3 preferred topics, proceed to News Activity
        btnNext.setOnClickListener {
            if (selectedTopicList.size < 3) {
                Toast.makeText(
                    this.applicationContext,
                    getString(R.string.please_select_3_topics),
                    Toast.LENGTH_LONG
                ).show()

            } else {
                val user: HashMap<String, Any> = HashMap() // define empty HashMap
                user["preferences"] = selectedTopicList
                fireStore.collection("users").document(userID).set(user, SetOptions.merge())

                startActivity(Intent(this, NewsActivity::class.java))
            }
        }
    }

    fun onCboxBusinessClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            if (checked) {
                selectedTopicList.add("business")
            } else {
                selectedTopicList.remove("business")
            }
        }
    }

    fun onCboxComputersClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            if (checked) {
                selectedTopicList.add("technology")
            } else {
                selectedTopicList.remove("technology")
            }
        }
    }

    fun onCboxArtClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            if (checked) {
                selectedTopicList.add("entertainment")
            } else {
                selectedTopicList.remove("entertainment")
            }
        }
    }

    fun onCboxMedicineClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            if (checked) {
                selectedTopicList.add("health")
            } else {
                selectedTopicList.remove("health")
            }
        }
    }

    fun onCboxSportsClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            if (checked) {
                selectedTopicList.add("sports")
            } else {
                selectedTopicList.remove("sports")
            }
        }
    }
}