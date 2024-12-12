package com.example.smarthouse

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthouse.SB.Device
import com.example.smarthouse.SB.Room
import com.example.smarthouse.SB.User
import java.lang.ref.WeakReference
import java.net.URL

class MainAdapter(
    private var itemsList: List<Any>,
    private val deviceTypesMap: Map<Int, SB.DeviceType>
) : RecyclerView.Adapter<MainAdapter.ItemViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ROOM = 0
        private const val VIEW_DEVICE = 1
        private const val VIEW_USER = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return when (viewType) {
            VIEW_TYPE_ROOM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
                RoomViewHolder(view)
            }
            VIEW_DEVICE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device_type, parent, false)
                DeviceViewHolder(view)
            }
            VIEW_USER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_status, parent, false)
                UserViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemsList[position]
        when (holder) {
            is RoomViewHolder -> holder.bind(item as Room)
            is DeviceViewHolder -> holder.bind(item as Device, deviceTypesMap[item.device_type]?.image_path)
            is UserViewHolder -> holder.bind(item as User)
        }
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemsList[position]) {
            is Room -> VIEW_TYPE_ROOM
            is Device -> VIEW_DEVICE
            is User -> VIEW_USER
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }

    fun updateList(newList: List<Any>) {
        itemsList = newList
        notifyDataSetChanged()
    }

    abstract class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class RoomViewHolder(itemView: View) : ItemViewHolder(itemView) {
        private val roomImageView: ImageView = itemView.findViewById(R.id.roomImageView)
        private val roomNameTextView: TextView = itemView.findViewById(R.id.roomNameTextView)

        fun bind(room: Room) {
            room.image_path?.let {
                ImageDownloadTask(WeakReference(roomImageView)).execute(it)
            }
            roomNameTextView.text = room.name
        }
    }

    class DeviceViewHolder(itemView: View) : ItemViewHolder(itemView) {
        private val deviceImageView: ImageView = itemView.findViewById(R.id.deviceTypeImageView)
        private val deviceNameTextView: TextView = itemView.findViewById(R.id.deviceTypeNameTextView)
        private val deviceStateCheckBox: CheckBox = itemView.findViewById(R.id.deviceStateCheckBox)

        fun bind(device: Device, imagePath: String?) {
            imagePath?.let {
                ImageDownloadTask(WeakReference(deviceImageView)).execute(it)
            }
            deviceNameTextView.text = device.name
            deviceStateCheckBox.isChecked = device.ison
        }
    }

    class UserViewHolder(itemView: View) : ItemViewHolder(itemView) {
        private val userImageView: ImageView = itemView.findViewById(R.id.userStatusImageView)
        private val userNameTextView: TextView = itemView.findViewById(R.id.userStatusNameTextView)

        fun bind(user: User) {
            userImageView.setImageResource(R.drawable.usericon)
            userNameTextView.text = user.username
        }
    }

    private class ImageDownloadTask(private val imageView: WeakReference<ImageView>) : AsyncTask<String, Void, Bitmap>() {
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
            if (imageView != null && result != null) {
                imageView.setImageBitmap(result)
            }
        }
    }
}