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
import com.example.smarthouse.SB.DeviceType
import java.lang.ref.WeakReference
import java.net.URL

class DeviceTypesAdapter(private val deviceTypesList: List<DeviceType>) : RecyclerView.Adapter<DeviceTypesAdapter.DeviceTypeViewHolder>() {

    var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceTypeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device_type, parent, false)
        return DeviceTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceTypeViewHolder, position: Int) {
        val deviceType = deviceTypesList[position]
        holder.bind(deviceType, position == selectedPosition)

        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return deviceTypesList.size
    }

    inner class DeviceTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.deviceTypeImageView)
        private val nameTextView: TextView = itemView.findViewById(R.id.deviceTypeNameTextView)

        fun bind(deviceType: DeviceType, isSelected: Boolean) {
            deviceType.image_path?.let {
                ImageDownloadTask(WeakReference(imageView)).execute(it)
            }
            nameTextView.text = deviceType.name
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