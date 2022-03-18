package com.codepath.apps.restclienttemplate

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.google.android.material.snackbar.Snackbar
import okhttp3.Headers
import java.io.*

const val CHARACTER_LIMIT = 280
class ComposeActivity : AppCompatActivity() {
    lateinit var etCompose: EditText
    lateinit var btnTweet: Button
    lateinit var tvCount: TextView
    lateinit var client: TwitterClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)

        etCompose = findViewById(R.id.etTweetCompose)
        btnTweet = findViewById(R.id.btnTweet)
        tvCount = findViewById<TextView>(R.id.tvCount)
        client = TwitterApplication.getRestClient(this)

        //check if there is something saved as a draft
        try {
            val file = File(filesDir, "SavedTweet")
            etCompose.setText(file.readText()) // Read file
            Log.i(TAG,file.readText())
            tvCount.text = "Characters left: ${CHARACTER_LIMIT - etCompose.text.toString().length}"
        }catch (e: Exception) {
            e.printStackTrace()
        }

        //Handle user's click on tweet button
        btnTweet.setOnClickListener { it ->
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
                // 2. make sure tweet is under character count -> see CHARACTER_LIMIT value
                if (tweetContent.length > CHARACTER_LIMIT) {

                    Snackbar.make(
                    it,
                    "Tweet is too long! Limit is ${CHARACTER_LIMIT.toString()} characters",
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            } else {
                    client.publishTweet(tweetContent, object : JsonHttpResponseHandler() {
                        override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON) {
                            //delete any drafts we had
                            try {
                                openFileOutput("SavedTweet", Context.MODE_PRIVATE).use {
                                    it.write("".toByteArray())
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

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
        //Handle character count
        etCompose.addTextChangedListener(object : TextWatcher {
            val tweetContent = this.toString()

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //reset tvCount text color in any case beforehand to prevent problems
                tvCount.setTextColor(Color.GRAY)
            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s != null) {
                    if(s.length > CHARACTER_LIMIT) {
                        tvCount.setTextColor(Color.RED)
                    }
                    tvCount.text = "Characters left: ${CHARACTER_LIMIT - s.length}"
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    override fun onBackPressed() {
        val text = etCompose.text.toString()
        if(text.isNotEmpty()) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Tweet not published")
                .setMessage("Would you like to save it as a draft?")
                .setNegativeButton("No") { dialog, which ->
                    //do nothing and go back
                    super.onBackPressed()
                }
                .setPositiveButton("Yes") { dialog, which ->
                    //respond to positive button press
                    //save data
                    try {
                        this.openFileOutput("SavedTweet", Context.MODE_PRIVATE).use {
                            it.write(text.toByteArray())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    //go back
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Draft saved successfully!",
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                    super.onBackPressed()
                }
            builder.create()
            builder.show()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        val TAG = "ComposeActivity"
    }
}