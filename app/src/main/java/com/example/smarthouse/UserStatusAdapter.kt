package com.example.smarthouse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserStatusAdapter(private val userStatusList: List<String>) : RecyclerView.Adapter<UserStatusAdapter.UserStatusViewHolder>() {

    var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserStatusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_status, parent, false)
        return UserStatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserStatusViewHolder, position: Int) {
        val userStatus = userStatusList[position]
        holder.bind(userStatus, position == selectedPosition)

        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return userStatusList.size
    }

    class UserStatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.userStatusImageView)
        private val nameTextView: TextView = itemView.findViewById(R.id.userStatusNameTextView)

        fun bind(userStatus: String, isSelected: Boolean) {
            // Загрузка изображения и имени статуса пользователя
            // imageView.setImageResource(R.drawable.usericon)
            nameTextView.text = userStatus
            itemView.setBackgroundResource(if (isSelected) R.drawable.bottom_border else R.drawable.button_rounded)
        }
    }
}