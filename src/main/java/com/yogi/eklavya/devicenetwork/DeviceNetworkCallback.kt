package com.yogi.eklavya.devicenetwork

interface DeviceNetworkCallback {

    fun onStateChanged(networkState: NetworkState, stateToggled: Boolean)
}