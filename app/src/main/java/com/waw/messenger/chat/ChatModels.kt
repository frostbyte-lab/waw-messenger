package com.waw.messenger.chat

import java.util.UUID

enum class MessageStatus { SENDING, SENT, DELIVERED, READ, FAILED }

data class User(val id: String, val name: String, val online: Boolean = false)

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val senderId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENDING,
    val replyToId: String? = null,
    val deleted: Boolean = false
)

data class Conversation(
    val id: String,
    val participant: User,
    val lastMessage: String = "",
    val unreadCount: Int = 0
)
