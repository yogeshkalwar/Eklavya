package com.yogi.eklavya.devicenetwork

object DeviceNetwork : DeviceNetworkCallback {

    private var isInitialized : Boolean = false
    private lateinit var deviceNetworkProvider: DeviceNetworkProvider
    private val callbacks:MutableList<DeviceNetworkCallback> = ArrayList();

    fun init() : DeviceNetwork {
        if (deviceNetworkProvider == null) {
            throw IllegalStateException("DeviceNetworkProvider not initialized. Please set one.")
        }
        deviceNetworkProvider.start()
        isInitialized = true;
        return this
    }

    fun stop() {
        callbacks.clear()
        deviceNetworkProvider.stop()
        isInitialized = false;
    }

    override fun onStateChanged(networkState: NetworkState, stateToggled: Boolean) {
        for(callback in callbacks) {
            callback.onStateChanged(networkState, stateToggled)
        }
    }

    fun addProvider(provider: DeviceNetworkProvider) : DeviceNetwork {
        deviceNetworkProvider = provider
        deviceNetworkProvider.addNetworkCallback(this)
        return this
    }

    fun register(callback: DeviceNetworkCallback): DeviceNetwork {
        callbacks.add(callback)
        return this
    }

    fun unregister(callback: DeviceNetworkCallback) {
        callbacks.remove(callback)
    }

    fun isNetworkAvailable(callback: DeviceNetworkCallback) {
        deviceNetworkProvider.isNetworkAvailable(callback)
    }

    fun isNetworkAvailableBlocking() = deviceNetworkProvider.isNetworkAvailableBlocking()
}