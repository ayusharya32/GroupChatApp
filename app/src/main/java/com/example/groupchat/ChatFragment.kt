package com.example.groupchat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


class ChatFragment: Fragment(R.layout.fragment_chat) {

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val messageRef = db.collection("chatMessages")
    private var chatList: MutableList<ChatMessage> = arrayListOf()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var messageListener: ListenerRegistration

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatList.clear()

        Utilities.currentUser = auth.currentUser?.email

        setUpRecyclerView()
        subscribeToRealtimeUpdates()

        toggleFabOnTextChange()

        fab.setOnClickListener {
            val author = Utilities.currentUser
            val message = etMessage.text.trim().toString()
            val chatMessage = ChatMessage(author!!, message)

            etMessage.setText("")
            saveMessage(chatMessage)
        }

        toolbar.setOnMenuItemClickListener {
            if(it.itemId == R.id.miLogout){
                logOut()
            }
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messageListener.remove()
    }

    private fun logOut() {
        auth.signOut()
        findNavController().navigate(R.id.action_chatFragment_to_loginFragment)
    }

    private fun setUpRecyclerView() {
        chatAdapter = ChatAdapter()
        rvChat.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
                reverseLayout = true
            }
            adapter = chatAdapter
        }

        rvChat.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if(bottom < oldBottom){
                scrollToLatestPosition()
            }
        }
    }

    private fun subscribeToRealtimeUpdates() {
        messageListener = messageRef.orderBy("timestamp")
            .addSnapshotListener{ querySnapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                error?.let {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                querySnapshot?.let {
                    for(documentChange in it.documentChanges){
                        val chatMessage = documentChange.document.toObject<ChatMessage>()
                        if(chatMessage.timestamp != null) {
                            chatList.add(chatMessage)
                        }
                    }
                    chatAdapter.differ.submitList(chatList.reversed().toList())
                    scrollToLatestPosition()
                }
            }
    }

    private fun saveMessage(chatMessage: ChatMessage) = CoroutineScope(Dispatchers.IO).launch {
        try {
            messageRef.add(chatMessage).await()
            withContext(Dispatchers.Main){
                Toast.makeText(requireContext(), "Successfully saved data", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main){
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scrollToLatestPosition() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(100L)
            rvChat.scrollToPosition(0)
        }
    }

    private fun toggleFabOnTextChange() {
        etMessage.addTextChangedListener(
            object: TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    fab.isEnabled = etMessage.text.isNotEmpty()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {/*NO-OP*/}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {/*NO-OP*/}
            }
        )
    }
}