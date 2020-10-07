package com.example.groupchat

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class ChatMessage(
    var author: String = "",
    var message: String = "",
    @ServerTimestamp
    var timestamp: Date? = null
)