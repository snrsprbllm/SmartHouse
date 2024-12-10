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
import com.example.smarthouse.SB.Device
import com.example.smarthouse.SB.Room
import com.example.smarthouse.SB.User
import java.lang.ref.WeakReference
import java.net.URL

class MainAdapter(private var itemsList: List<Any>, private val deviceTypesMap: Map<Int, SB.DeviceType>) : RecyclerView.Adapter<MainAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemsList[position]
        when (item) {
            is Room -> holder.bind(item)
            is Device -> holder.bind(item, deviceTypesMap[item.device_type]?.image_path)
            is User -> holder.bind(item)
        }
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    fun updateList(newList: List<Any>) {
        itemsList = newList
        notifyDataSetChanged()
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemImageView: ImageView = itemView.findViewById(R.id.roomImageView)
        private val itemNameTextView: TextView = itemView.findViewById(R.id.roomNameTextView)

        fun bind(room: Room) {
            room.image_path?.let {
                ImageDownloadTask(WeakReference(itemImageView), it).execute()
            }
            itemNameTextView.text = room.name
        }

        fun bind(device: Device, imagePath: String?) {
            imagePath?.let {
                ImageDownloadTask(WeakReference(itemImageView), it).execute()
            }
            itemNameTextView.text = device.name
        }

        fun bind(user: User) {
            itemNameTextView.text = user.username
            itemImageView.setImageResource(R.drawable.usericon)
        }
    }

    private class ImageDownloadTask(private val imageView: WeakReference<ImageView>, private val imageUrl: String) : AsyncTask<Void, Void, Bitmap>() {
        override fun doInBackground(vararg params: Void): Bitmap? {
            var bitmap: Bitmap? = ImageCache.cache.get(imageUrl)
            if (bitmap == null) {
                try {
                    val inputStream = URL(imageUrl).openStream()
                    bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        ImageCache.cache.put(imageUrl, bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return bitmap
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            super.onPostExecute(bitmap)
            val imageView = imageView.get()
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap)
            }
        }
    }
}