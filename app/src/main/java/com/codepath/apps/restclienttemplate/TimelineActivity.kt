package com.codepath.apps.restclienttemplate

import EndlessRecyclerViewScrollListener
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.toColor
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.load.engine.Resource
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.Headers
import org.json.JSONException


class TimelineActivity : AppCompatActivity() {

    lateinit var client: TwitterClient

    lateinit var rvTweets: RecyclerView

    lateinit var adapter: TweetsAdapter

    lateinit var swipeContainer: SwipeRefreshLayout

    val tweets = ArrayList<Tweet>()

    var lastTweetDisplayed: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        client = TwitterApplication.getRestClient(this)

        swipeContainer = findViewById(R.id.swipeContainer)
        swipeContainer.setOnRefreshListener {
            Log.i(TAG,"Refreshing Twitter Timeline")
            populateHomeTimeline(false)
        }

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light);

        rvTweets = findViewById(R.id.rvTweets)
        adapter = TweetsAdapter(tweets)

        val linearLayoutManager = LinearLayoutManager(this)
        rvTweets.layoutManager = linearLayoutManager
        rvTweets.adapter = adapter

        val scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                Log.i(TAG, "Populating rv with more items")
                populateHomeTimeline(true)
            }
        }

        //toolbar setup
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setLogo(R.drawable.logo48)
        supportActionBar?.setDisplayUseLogoEnabled(true)

        //compose button listener
        val btnCompose = findViewById<FloatingActionButton>(R.id.compose)
        btnCompose.setOnClickListener {
            val intent = Intent(this, ComposeActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        }

        rvTweets.addOnScrollListener(scrollListener)
        populateHomeTimeline(false)
    }

/*                  REMOVED COMPOSE BUTTON FROM TOOLBAR
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    //handles clicks on menu item
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.compose) {
            //Redirect user to compose activity

        }
        return super.onOptionsItemSelected(item)
    }*/

    //Method called back when the user comes from ComposeActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == RESULT_OK && requestCode == REQUEST_CODE){
            //get tweet object back from ComposeActivity
            val tweet = data?.getParcelableExtra("tweet") as Tweet

            //update timeline
            //modify data source of tweets
            tweets.add(0, tweet)
            //update adapter
            adapter.notifyItemInserted(0)
            //scroll adapter back to the first position so we can see the tweet the user composed
            rvTweets.smoothScrollToPosition(0)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun populateHomeTimeline(isLoadMore: Boolean) {
        if(isLoadMore) {
            client.getNextPageOfTweets(object: JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                    Log.i(TAG, "onSuccess $statusCode")
                    val jsonArray = json.jsonArray
                    try {
                        val listOfNewTweetsRetrieved = Tweet.fromJsonArray(jsonArray)
                        tweets.addAll(listOfNewTweetsRetrieved)
                        adapter.notifyDataSetChanged()
                    } catch (e: JSONException) {
                        Log.e(TAG, "JSON Exception $e")
                    }
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Headers?,
                    response: String?,
                    throwable: Throwable?
                ) {
                    Log.i(TAG, "onFailure $statusCode")
                }
            }, adapter.getLastTweetId())
        } else {
            client.getHomeTimeline(object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                    Log.i(TAG, "onSuccess $statusCode")
                    val jsonArray = json.jsonArray
                    try {
                        //clear current fetched tweets before retrieving
                        adapter.clear()
                        val listOfNewTweetsRetrieved = Tweet.fromJsonArray(jsonArray)
                        tweets.addAll(listOfNewTweetsRetrieved)
                        adapter.notifyDataSetChanged()
                        // Now we call setRefreshing(false) to signal refresh has finished
                        swipeContainer.isRefreshing = false
                    } catch (e: JSONException) {
                        Log.e(TAG, "JSON Exception $e")
                    }
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Headers?,
                    response: String?,
                    throwable: Throwable?
                ) {
                    Log.i(TAG, "onFailure $statusCode")
                }
            })
        }
    }

    companion object {
        const val TAG = "TimelineActivity"
        const val REQUEST_CODE = 20
    }
}