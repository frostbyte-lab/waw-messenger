package com.waw.messenger.security

import android.net.Uri

/** Conservative local URL safety gate; it never inspects WhatsApp credentials or message content. */
object WawShield {
    private val blockedHosts = setOf(
        "bit.ly", "tinyurl.com", "t.co", "is.gd", "cutt.ly"
    )
    private val blockedTerms = setOf("judol", "slot", "scam", "phishing", "claim-hadiah")

    fun isBlocked(uri: Uri): Boolean {
        val host = uri.host.orEmpty().lowercase()
        val text = uri.toString().lowercase()
        return blockedHosts.any { host == it || host.endsWith(".$it") } || blockedTerms.any(text::contains)
    }
}
