package com.yogi.eklavya.devicenetwork

interface DeviceNetworkProvider {
    var callback: DeviceNetworkCallback?

    fun start()
    fun stop()
    fun isNetworkAvailable(networkCallback: DeviceNetworkCallback)
    fun isNetworkAvailableBlocking() : NetworkState
    fun addNetworkCallback(networkCallback: DeviceNetworkCallback)

    fun update(newState: NetworkState, stateToggled: Boolean) = callback?.onStateChanged(newState, stateToggled)
}