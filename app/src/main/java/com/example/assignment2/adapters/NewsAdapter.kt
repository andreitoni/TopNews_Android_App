package com.example.assignment2.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.assignment2.DetailedNews
import com.example.assignment2.R

class NewsAdapter(
    private var titles: List<String>,
    private var details: List<String>,
    private var images: List<String>,
    private var links: List<String>,
    private var sources: MutableList<com.example.assignment2.api.Source>,
    private var publishedAt: List<String>
) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val cardView: CardView = itemView.findViewById(R.id.cardView)

        val itemTitle: TextView = itemView.findViewById(R.id.tv_title)
        val itemDetail: TextView = itemView.findViewById(R.id.tv_description)
        val itemPicture: ImageView = itemView.findViewById(R.id.iv_image)
        val itemSource: TextView = itemView.findViewById(R.id.tv_source)
        val itemPublishedAt: TextView = itemView.findViewById(R.id.tv_publishedAt)

        init {
            itemView.setOnClickListener {
                val position: Int = adapterPosition

                val intent2 = Intent(itemView.context, DetailedNews::class.java)
                val aUrl = links[position]
                intent2.putExtra("url", aUrl)
                itemView.context.startActivity(intent2)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsAdapter.ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.news_item_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: NewsAdapter.ViewHolder, position: Int) {
        holder.itemTitle.text = titles[position]
        holder.itemDetail.text = details[position]
        holder.itemSource.text = sources[position].name
        holder.itemPublishedAt.text =
            publishedAt[position].take(10) + " " + publishedAt[position].takeLast(9).dropLast(1)

        Glide.with(holder.itemPicture)
            .load(images[position])
            .into(holder.itemPicture)
    }

    override fun getItemCount(): Int {
        return titles.size
    }
}