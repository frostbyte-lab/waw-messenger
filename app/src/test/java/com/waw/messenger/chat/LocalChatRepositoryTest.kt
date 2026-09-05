package com.waw.messenger.chat

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalChatRepositoryTest {
    @Test
    fun sendMessageUpdatesMessageAndConversation() = runBlocking {
        val repository = LocalChatRepository(currentUserId = "me")

        val message = repository.sendMessage("c-u-1", "me", "Halo")

        assertEquals(MessageStatus.SENT, message.status)
        assertEquals("Halo", repository.messages("c-u-1").first().single().text)
        assertEquals("Halo", repository.conversations().first().first { it.id == "c-u-1" }.lastMessage)
    }

    @Test
    fun deleteMessageMarksMessageAsDeleted() = runBlocking {
        val repository = LocalChatRepository(currentUserId = "me")
        val message = repository.sendMessage("c-u-1", "me", "Hapus saya")

        repository.deleteMessage(message.id)

        val deleted = repository.messages("c-u-1").first().single()
        assertTrue(deleted.deleted)
        assertEquals("Pesan dihapus", deleted.text)
    }
}
