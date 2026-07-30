package com.example.playlistmaker2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter(
    private var tracks: List<Track>,
    private val onItemClick: (Track) -> Unit
): RecyclerView.Adapter<TrackViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)
        holder.itemView.setOnClickListener {
            onItemClick(track)
        }

    }

    override fun getItemCount(): Int = tracks.size

    override fun getItemId(position: Int): Long = tracks[position].trackId

    fun updateTracks(newTracks: List<Track>) {
        val oldTracks = tracks
        val updatedTracks = newTracks.toList()
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldTracks.size

            override fun getNewListSize(): Int = updatedTracks.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldTracks[oldItemPosition].trackId ==
                        updatedTracks[newItemPosition].trackId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldTracks[oldItemPosition] == updatedTracks[newItemPosition]
            }
        })

        tracks = updatedTracks
        diffResult.dispatchUpdatesTo(this)
    }
}
