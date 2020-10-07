package com.example.groupchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.chat_item_left.view.*

class ChatAdapter: RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    private val diffCallback = object : DiffUtil.ItemCallback<ChatMessage>(){
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder{

        val view = if(viewType == Utilities.MESSAGE_LEFT){
            LayoutInflater.from(parent.context).inflate(R.layout.chat_item_left, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.chat_item_right, parent, false)
        }

        return ChatViewHolder(view)
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val curMessage = differ.currentList[position]

        if(Utilities.currentUser == curMessage.author){
            holder.itemView.tvAuthor.text = "You"
        } else {
            holder.itemView.tvAuthor.text = curMessage.author
        }

        holder.itemView.tvMessage.text = curMessage.message

    }

    override fun getItemViewType(position: Int): Int {
        return if(Utilities.currentUser != differ.currentList[position].author){
            Utilities.MESSAGE_LEFT
        } else {
            Utilities.MESSAGE_RIGHT
        }
    }
}