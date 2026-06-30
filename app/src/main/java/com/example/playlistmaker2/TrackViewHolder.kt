package com.example.playlistmaker2

import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.util.TypedValueCompat.dpToPx
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

        val cornerRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8f, itemView.context.resources.displayMetrics
        ).toInt()
        Glide.with(itemView.context)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.ic_placeholder_45)
            .centerCrop()
            .transform(RoundedCorners(cornerRadius))
            .into(ivTrackCover)



    }

}