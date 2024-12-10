package com.example.smarthouse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthouse.SB.Room

class RoomsAdapter(private var roomsList: List<SB.Room>) : RecyclerView.Adapter<RoomsAdapter.RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = roomsList[position]
        holder.bind(room)
    }

    override fun getItemCount(): Int {
        return roomsList.size
    }

    fun updateList(newList: List<Room>) {
        roomsList = newList
        notifyDataSetChanged()
    }

    class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val roomImageView: ImageView = itemView.findViewById(R.id.roomImageView)
        private val roomNameTextView: TextView = itemView.findViewById(R.id.roomNameTextView)

        fun bind(room: Room) {
            // Загрузка изображения из image_path
            // Здесь можно использовать библиотеку для загрузки изображений, например, Glide или Picasso
            // Glide.with(itemView.context).load(room.image_path).into(roomImageView)

            roomNameTextView.text = room.name
        }
    }
}