package com.example.assignment2

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.assignment2.adapters.NewsAdapter
import com.example.assignment2.api.Source
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.news_page.*


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import kotlin.properties.Delegates

const val BASE_URL = "https://newsapi.org"
const val apiKey = "d745a54ad3024dde8b8f80e19a35d607"


class NewsActivity : AppCompatActivity() {


    lateinit var countDownTimer: CountDownTimer
    private lateinit var fAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var userID: String

    // a var to cycle between chosen topics to show news alternatively
    var cycleTopic by Delegates.notNull<Int>()

    private var titlesList = mutableListOf<String>()
    private var descriptionsList = mutableListOf<String>()
    private var imagesList = mutableListOf<String>()
    private var linksList = mutableListOf<String>()
    private var sourcesList = mutableListOf<Source>()
    private var publishedAtList = mutableListOf<String>()
    private var userCountry: String = ""

    // list of chosen topics fetched from FireStore
    private var preferredTopicsList = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_page)
        //Instantiate Firebase
        fAuth = FirebaseAuth.getInstance()
        //Instantiate FireStore
        fStore = FirebaseFirestore.getInstance()

        userID = fAuth.currentUser!!.uid

        cycleTopic = 0

        val searchButton = findViewById<Button>(R.id.btn_searchNews)
        val editTextSearch = findViewById<EditText>(R.id.editTextSearch)

        searchButton.setOnClickListener {
            val searchKeyword = editTextSearch.text.toString().trim()
            if (TextUtils.isEmpty(searchKeyword) || searchKeyword.length < 2) {
                editTextSearch.error = getString(R.string.invalid_search)
            } else {
                makeAPIRequestUserSearch(searchKeyword)
            }
        }

        // Prevent the keyboard from appearing automatically because of the input text
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)


        val documentReference: DocumentReference =
            fStore.collection("users").document(userID)
        documentReference.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.i("docRef", "Listen failed.", e)
                return@addSnapshotListener
            }
            //get the preferred topics from FireStore and add them to preferredTopicsList
            if (snapshot != null && snapshot.exists()) {
                preferredTopicsList.clear()
                preferredTopicsList.addAll(snapshot.get("preferences") as Collection<String>)
                Log.i("topiclist", "Preferred topics in app: $preferredTopicsList")

                // get the user's country from FireStore if exists
                if (snapshot.get("country") != null) {
                    userCountry = snapshot.get("country") as String
                    //show news from different topics alternatively
                    var cycle = 0
                    while (cycle < preferredTopicsList.size - 1) {
                        makeAPIRequestTopicsCountry(preferredTopicsList[cycle])
                        cycle++
                    }

                } else {
                    //show news from different topics alternatively
                    var cycle = 0
                    while (cycle < preferredTopicsList.size - 1) {
                        makeAPIRequestTopics(preferredTopicsList[cycle])
                        cycle++
                    }
                }
                Log.i("docRef", "Current data: $userCountry")
            } else {
                Log.i("docRef", "No snapshot yet")
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_favorites -> startActivity(Intent(this, TopicActivity::class.java))
            R.id.nav_settings -> startActivity(Intent(this, UserSettings::class.java))
            R.id.nav_refresh -> attemptRequestAgain()
        }
        return super.onOptionsItemSelected(item)
    }

    // fade in animation for when the app is done loading
    private fun fadeFromBlack() {
        v_blackScreen.animate().apply {
            alpha(0f)
            duration = 3000
        }.start()
    }

    private fun setupRecyclerView() {
        rv_recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        rv_recyclerView.adapter = NewsAdapter(
            titlesList,
            descriptionsList,
            imagesList,
            linksList,
            sourcesList,
            publishedAtList
        )
    }

    private fun populateList(
        title: String,
        description: String,
        image: String,
        link: String,
        source: Source,
        publishedAt: String
    ) {
        titlesList.add(title)
        descriptionsList.add(description)
        imagesList.add(image)
        linksList.add(link)
        sourcesList.add(source)
        publishedAtList.add(publishedAt)
    }

    //requests data from the api and forwards it to the recycler view based on topics
    private fun makeAPIRequestTopics(topic: String) {

        progressBar.visibility = View.VISIBLE

        val api = Retrofit.Builder()
            .baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(APIRequest::class.java)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val defaultCountry = "us"
                val response = api.getNewsTopics(topic, apiKey, defaultCountry)

                for (article in response.articles) {
                    populateList(
                        article.title,
                        article.description,
                        article.urlToImage,
                        article.url,
                        article.source,
                        article.publishedAt
                    )
                }
                withContext(Dispatchers.Main) {
                    setupRecyclerView()
                    fadeFromBlack()
                    progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e("NewsActivity", e.toString())

                withContext(Dispatchers.Main) {
                    attemptRequestAgain()
                }
            }
        }
    }

    //requests data from the api and forwards it to the recycler view based on topics and country
    private fun makeAPIRequestTopicsCountry(topic: String) {

        progressBar.visibility = View.VISIBLE

        val api = Retrofit.Builder()
            .baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(APIRequest::class.java)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = api.getNewsTopicsCountry(userCountry, topic, apiKey)
                for (article in response.articles) {
                    populateList(
                        article.title,
                        article.description,
                        article.urlToImage,
                        article.url,
                        article.source,
                        article.publishedAt
                    )
                }
                withContext(Dispatchers.Main) {
                    setupRecyclerView()
                    fadeFromBlack()
                    progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e("NewsActivity", e.toString())

                withContext(Dispatchers.Main) {
                    attemptRequestAgain()
                }
            }
        }
    }

    //requests data from the api and forwards it to the recycler view based on the user's query
    private fun makeAPIRequestUserSearch(query: String) {

        progressBar.visibility = View.VISIBLE

        val api = Retrofit.Builder()
            .baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(APIRequest::class.java)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = api.getSearchTopic(query, apiKey)
                for (article in response.articles) {
                    populateList(
                        article.title,
                        article.description,
                        article.urlToImage,
                        article.url,
                        article.source,
                        article.publishedAt
                    )
                }
                withContext(Dispatchers.Main) {
                    setupRecyclerView()
                    fadeFromBlack()
                    progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e("NewsActivity", e.toString())

                withContext(Dispatchers.Main) {
                    attemptRequestAgain()
                }
            }
        }
    }

    private fun attemptRequestAgain() {
        //cycle through the topics
        if (cycleTopic < preferredTopicsList.size - 1) {
            cycleTopic++
        } else {
            cycleTopic = 0
        }

        countDownTimer = object : CountDownTimer(3 * 1000, 1000) {
            override fun onFinish() {
                Log.d("cycle", "cycletopic = $cycleTopic")
                Log.d("cycle", "preferredtopiclist =  $preferredTopicsList")

                makeAPIRequestTopics(preferredTopicsList[cycleTopic])
                countDownTimer.cancel()
            }

            override fun onTick(millisUntilFinished: Long) {
                Log.i(
                    "MainActivity",
                    "Could not retrieve data... Trying again in ${millisUntilFinished / 1000} seconds"
                )
            }
        }
        countDownTimer.start()
    }
}