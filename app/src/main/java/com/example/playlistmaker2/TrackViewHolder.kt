package com.example.playlistmaker2

import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class TrackViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
    private val ivTrackCover: ImageView = itemView.findViewById(R.id.ivTrackCover)

    private val tvTrackName: TextView = itemView.findViewById(R.id.tvTrackName)

    private val tvArtistName: TextView = itemView.findViewById(R.id.tvArtistName)

    private val tvTrackTime: TextView = itemView.findViewById(R.id.tvTrackTime)

    fun bind(track: Track){

        tvTrackName.text = track.trackName
        tvArtistName.text = track.artistName
        tvTrackTime.text = track.trackTime

        val cornerRadius = itemView.context.resources.getDimensionPixelSize(R.dimen.track_cover_radius)
        Glide.with(itemView.context)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.ic_placeholder_45)
            .centerCrop()
            .transform(RoundedCorners(cornerRadius))
            .into(ivTrackCover)



    }

}