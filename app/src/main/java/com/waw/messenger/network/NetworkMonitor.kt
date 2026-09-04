package com.waw.messenger.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitor(context: Context) : AutoCloseable {
    enum class Status { ONLINE, OFFLINE }

    private val connectivity = context.getSystemService(ConnectivityManager::class.java)
    private val _status = MutableStateFlow(currentStatus())
    val status: StateFlow<Status> = _status.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // Wait for onCapabilitiesChanged; synchronous capability queries here can race.
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            update(capabilities)
        }

        override fun onLost(network: Network) {
            _status.value = currentStatus()
        }
    }

    init {
        connectivity.registerDefaultNetworkCallback(callback)
    }

    fun isOnline(): Boolean = _status.value == Status.ONLINE

    private fun update(capabilities: NetworkCapabilities) {
        _status.value = if (
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        ) Status.ONLINE else Status.OFFLINE
    }

    private fun currentStatus(): Status {
        val network = connectivity.activeNetwork ?: return Status.OFFLINE
        val capabilities = connectivity.getNetworkCapabilities(network) ?: return Status.OFFLINE
        return if (
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        ) Status.ONLINE else Status.OFFLINE
    }

    override fun close() {
        runCatching { connectivity.unregisterNetworkCallback(callback) }
    }
}
