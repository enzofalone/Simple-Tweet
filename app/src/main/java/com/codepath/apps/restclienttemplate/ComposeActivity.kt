package com.codepath.apps.restclienttemplate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

class ComposeActivity : AppCompatActivity() {

    lateinit var etCompose: EditText
    lateinit var btnTweet: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)

        etCompose = findViewById(R.id.etTweetCompose)
        btnTweet = findViewById(R.id.btnTweet)

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
                //TODO: Make an api call to Twitter in order to publish the text into a tweet
                Snackbar.make(it, tweetContent, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}