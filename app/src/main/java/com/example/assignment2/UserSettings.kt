package com.example.assignment2

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.settings_layout.*
import java.util.*

class UserSettings : AppCompatActivity() {

    private lateinit var fireStore: FirebaseFirestore
    private lateinit var fAuth: FirebaseAuth
    private lateinit var userID: String


    // the list of selected notification to be saved in FireStore
    private var topicNotificationList = arrayListOf<String>()

    // list of selected notifications fetched from FireStore
    private var topicNotificationListFromFirestore = arrayListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_layout)

        createNotificationChannel()

        fAuth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()

        userID = fAuth.currentUser!!.uid


        val findButton = findViewById<Button>(R.id.button_find)
        val edtTxtCountry = findViewById<EditText>(R.id.inputCountry)

        //gets the country, converts it to initials, saves it in FireStore and passes an intent to
        // the NewsActivity to make the search
        findButton.setOnClickListener {
            val userCountry = edtTxtCountry.text.toString().trim()
            if (userCountry != "") {
                val countryCode = getCountryCode(userCountry)?.toLowerCase(Locale.ROOT)
                if (countryCode != null) {
                    // add country preference in firestore
                    val user: HashMap<String, Any> = HashMap<String, Any>() // define empty hashmap

                    user["country"] = countryCode

                    val documentReference: DocumentReference =
                        fireStore.collection("users").document(userID)

                    documentReference.update(user)

                    val intent = Intent(this, NewsActivity::class.java)
                    intent.putExtra("countryCode", countryCode)
                    Log.i("popo", countryCode)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, getString(R.string.invalid_country), Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(this, getString(R.string.insert_country), Toast.LENGTH_SHORT).show()
            }
        }
        getNotifPrefFromFirestore()

        val btnSave: Button = findViewById(R.id.button_savePref)

        //save the user notification preferences and sets up a reminder repeating every 24 hours
        btnSave.setOnClickListener {

            val user: HashMap<String, ArrayList<String>> = HashMap() // define empty hashmap

            user["notifications preferences"] = topicNotificationList

            fireStore.collection("users").document(userID).set(user, SetOptions.merge())

            Toast.makeText(
                this.applicationContext,
                getString(R.string.notifications_preferences_saved),
                Toast.LENGTH_LONG
            ).show()
            //Setup notifications
            val intent = Intent(this, ReminderBroadcast::class.java)
            val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
            val alarmManager: AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager


            val btnDontNotifyMe = findViewById<Button>(R.id.noNotifications)
            // Stop receiving notifications
            btnDontNotifyMe.setOnClickListener {
                Toast.makeText(this, getString(R.string.notifications_off), Toast.LENGTH_SHORT)
                    .show()
                alarmManager.cancel(pendingIntent)
            }

            //Wake up the device to fire the alarm in 30 minutes, and every day after that
            alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1000,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
        getNotifPrefFromFirestore()
        // Log out the current user and send it to the sign in/sign up screen
        btn_log_out.setOnClickListener() {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        //end of onCreate
    }


    private fun getNotifPrefFromFirestore() {
        // get data from firestore about preferences to make checkboxes checked

        val docRef =
            fireStore.collection("users").document(userID)

        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.i("notifications", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                topicNotificationListFromFirestore.clear()
                if (snapshot.get("notifications preferences") != null) {
                    topicNotificationListFromFirestore.addAll(snapshot.get("notifications preferences") as Collection<String>)
                    Log.i(
                        "notifications",
                        "Current notifications data in app: $topicNotificationListFromFirestore"
                    )
                    displayPreviousNotificationsSelected(topicNotificationListFromFirestore)
                    makeCboxesChecked(topicNotificationListFromFirestore)


                } else {
                    Log.i("notifications", "User has no notifications preferences yet")
                }

            } else {
                Log.i("notifications", "Current notifications data: null")
            }

        }
    }


    private fun makeCboxesChecked(list: ArrayList<String>) {
        list.forEach {
            if (it == getString(R.string.business)) {
                cbox_business.setChecked(true)
                Log.i("checkboxBUSINESS", "was set to true")
            }

            if (it == getString(R.string.technology)) {
                cbox_tech.setChecked(true)
                Log.i("checkboxTECH", "was set to true")
            }

            if (it == getString(R.string.entertainment)) {
                cbox_art.setChecked(true)
                Log.i("checkboxENTERTAINMENT", "was set to true")
            }

            if (it == getString(R.string.health)) {
                cbox_medicine.setChecked(true)
                Log.i("checkboxHEALTH", "was set to true")
            }

            if (it == getString(R.string.sports)) {
                cbox_sports.setChecked(true)
                Log.i("checkboxSPORTS", "was set to true")
            }
        }
    }


    private fun displayPreviousNotificationsSelected(list: ArrayList<String>): ArrayList<String> {
        Log.i("5555555", "Passed notifications: $list")
        return list
    }


    fun onCboxBusinessClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            if (checked) {
                topicNotificationList.add("business")
            } else {
                topicNotificationList.remove("business")
            }
        }
    }


    fun onCboxComputersClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            if (checked) {
                topicNotificationList.add("technology")
            } else {
                topicNotificationList.remove("technology")
            }
        }
    }

    fun onCboxArtClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            if (checked) {
                topicNotificationList.add("entertainment")
            } else {
                topicNotificationList.remove("entertainment")
            }
        }
    }

    fun onCboxMedicineClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            if (checked) {
                topicNotificationList.add("health")
            } else {
                topicNotificationList.remove("health")
            }
        }
    }

    fun onCboxSportsClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            if (checked) {
                topicNotificationList.add("sports")
            } else {
                topicNotificationList.remove("sports")
            }
        }
    }

    // This method converts a country name to initials (e.g. France -> FR)
    private fun getCountryCode(countryName: String) =
        Locale.getISOCountries().find {
            Locale("", it).displayCountry == countryName
        }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.NotificationTitle)
            val descriptionText = getString(R.string.NotificationDescription)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("notify", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}
