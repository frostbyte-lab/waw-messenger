package com.waw.messenger.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

interface ChatRepository {
    fun conversations(): Flow<List<Conversation>>
    fun messages(conversationId: String): Flow<List<Message>>
    suspend fun sendMessage(conversationId: String, senderId: String, text: String): Message
    suspend fun markRead(conversationId: String)
    suspend fun deleteMessage(messageId: String)
}

/** Local repository used until the production transport is connected. */
class LocalChatRepository(private val currentUserId: String) : ChatRepository {
    private val users = listOf(
        User("u-1", "Andi", true),
        User("u-2", "Budi", false),
        User("u-3", "Citra", true)
    )
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    private val _conversations = MutableStateFlow(
        users.map { Conversation("c-${it.id}", it) }
    )

    override fun conversations(): Flow<List<Conversation>> = _conversations.asStateFlow()

    override fun messages(conversationId: String): Flow<List<Message>> =
        _messages.map { all -> all.filter { it.conversationId == conversationId } }

    override suspend fun sendMessage(conversationId: String, senderId: String, text: String): Message {
        val pending = Message(conversationId = conversationId, senderId = senderId, text = text)
        _messages.value = _messages.value + pending.copy(status = MessageStatus.SENT)
        _conversations.value = _conversations.value.map {
            if (it.id == conversationId) it.copy(lastMessage = text) else it
        }
        return pending.copy(status = MessageStatus.SENT)
    }

    override suspend fun markRead(conversationId: String) {
        _messages.value = _messages.value.map {
            if (it.conversationId == conversationId && it.senderId != currentUserId) {
                it.copy(status = MessageStatus.READ)
            } else it
        }
        _conversations.value = _conversations.value.map {
            if (it.id == conversationId) it.copy(unreadCount = 0) else it
        }
    }

    override suspend fun deleteMessage(messageId: String) {
        _messages.value = _messages.value.map {
            if (it.id == messageId) it.copy(deleted = true, text = "Pesan dihapus") else it
        }
    }
}
