package com.geminiai.studywithai.screens.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.geminiai.studywithai.R
import com.geminiai.studywithai.models.Chapter

class ChapterAdapter(private val chapters: List<Chapter>, private val clickListener: (Chapter) -> Unit) :
    RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    inner class ChapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chapterTitle: TextView = itemView.findViewById(R.id.chapterTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chapter, parent, false)
        return ChapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val chapter = chapters[position]
        holder.chapterTitle.text = chapter.title
        holder.itemView.setOnClickListener { clickListener(chapter) }
    }

    override fun getItemCount(): Int = chapters.size
}
