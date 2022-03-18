package com.codepath.apps.restclienttemplate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.google.android.material.snackbar.Snackbar
import okhttp3.Headers

class ComposeActivity : AppCompatActivity() {

    lateinit var etCompose: EditText
    lateinit var btnTweet: Button

    lateinit var client: TwitterClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)

        etCompose = findViewById(R.id.etTweetCompose)
        btnTweet = findViewById(R.id.btnTweet)

        client = TwitterApplication.getRestClient(this)

        //Handle user's click on tweet button
        btnTweet.setOnClickListener {
            //Grab the text user has inputted in the etTweetCompose layout
            val tweetContent = etCompose.text.toString()

            // 1. make sure tweet is not empty
            if(tweetContent.isEmpty()) {
                //display Snackbar message
                Snackbar.make(it,
                    "Your tweet can not be empty!",
                    Snackbar.LENGTH_SHORT)
                    .show()
            } else
                // 2. make sure tweet is under character count (140)
                if (tweetContent.length > 140) {
                Snackbar.make(
                    it,
                    "Tweet is too long! Limit is 140 characters",
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            } else {
                client.publishTweet(tweetContent, object : JsonHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON) {
                        Log.i(TAG, "Tweet successfully published!")

                        val tweet = Tweet.fromJson(json.jsonObject)

                        val intent = Intent()
                        intent.putExtra("tweet", tweet)
                        setResult(RESULT_OK, intent)
                        finish()
                    }

                    override fun onFailure(
                        statusCode: Int,
                        headers: Headers?,
                        response: String?,
                        throwable: Throwable?
                    ) {
                        Log.i(TAG, "Failure tweeting!", throwable)
                    }



                })
            }
        }
    }

    companion object {
        val TAG = "ComposeActivity"
    }
}