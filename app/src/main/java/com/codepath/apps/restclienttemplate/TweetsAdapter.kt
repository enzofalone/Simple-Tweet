package com.codepath.apps.restclienttemplate

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codepath.apps.restclienttemplate.models.Tweet

class TweetsAdapter(val tweets: ArrayList<Tweet>) : RecyclerView.Adapter<TweetsAdapter.ViewHolder>() {

    private var lastTweetId: Int = 0;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetsAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)

        //Inflate item layout
        val view = inflater.inflate(R.layout.item_tweet, parent, false)

        return ViewHolder(view)
    }

    //populate data into the item through ViewHolder
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TweetsAdapter.ViewHolder, position: Int) {
        //get data model based on position
        val tweet: Tweet = tweets.get(position)

        //set item views based on views and model
        holder.tvUsername.text = tweet.user?.name
        holder.tvScreenName.text = "@" + tweet.user?.screenName
        holder.tvTweetBody.text = tweet.body
        holder.tvCreatedAt.text = tweet.createdAt
        Glide.with(holder.itemView).load(tweet.user?.publicImageUrl).into(holder.ivProfileImage)
    }

    override fun getItemCount(): Int {
        return tweets.size
    }

    //get last Id in order to implement infinite scrolling
    fun getLastTweetId(): Long {
        return tweets.get(getItemCount() - 1).id
    }

    // Clean all elements of the recycler
    fun clear() {
        tweets.clear()
        notifyDataSetChanged()
    }

    // Add a list of items
    fun addAll(tweetList: List<Tweet>) {
        tweets.addAll(tweetList)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfileImage = itemView.findViewById<ImageView>(R.id.ivProfileImage)
        val tvUsername = itemView.findViewById<TextView>(R.id.tvUsername)
        val tvScreenName = itemView.findViewById<TextView>(R.id.tvScreenName)
        val tvTweetBody = itemView.findViewById<TextView>(R.id.tvTweetBody)
        val tvCreatedAt = itemView.findViewById<TextView>(R.id.tvCreatedAt)

    }
}