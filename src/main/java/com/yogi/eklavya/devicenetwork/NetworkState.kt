package com.yogi.eklavya.devicenetwork

enum class NetworkType {
    NONE,
    OFFLINE,
    WIFI,
    LTE
}

data class NetworkState(var networkType: NetworkType) {

    fun isOnline() = !(networkType == NetworkType.NONE || networkType == NetworkType.OFFLINE)
}