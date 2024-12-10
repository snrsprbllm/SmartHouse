package com.example.smarthouse

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthouse.SB.RoomType
import java.lang.ref.WeakReference
import java.net.URL

class RoomTypesAdapter(private val roomTypesList: List<RoomType>) : RecyclerView.Adapter<RoomTypesAdapter.RoomTypeViewHolder>() {

    var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomTypeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room_type, parent, false)
        return RoomTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomTypeViewHolder, position: Int) {
        val roomType = roomTypesList[position]
        holder.bind(roomType, position == selectedPosition)

        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return roomTypesList.size
    }

    inner class RoomTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.roomTypeImageView)
        private val nameTextView: TextView = itemView.findViewById(R.id.roomTypeNameTextView)

        fun bind(roomType: RoomType, isSelected: Boolean) {
            roomType.image_path?.let {
                ImageDownloadTask(WeakReference(imageView)).execute(it)
            }
            nameTextView.text = roomType.name
            itemView.setBackgroundResource(if (isSelected) R.drawable.bottom_border else R.drawable.button_rounded)
        }
    }

    private inner class ImageDownloadTask(private val imageView: WeakReference<ImageView>) : AsyncTask<String, Void, Bitmap>() {
        override fun doInBackground(vararg params: String): Bitmap? {
            val imageUrl = params[0]
            return try {
                val inputStream = URL(imageUrl).openStream()
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            val imageView = imageView.get()
            if (imageView != null) {
                imageView.setImageBitmap(result)
            }
        }
    }
}