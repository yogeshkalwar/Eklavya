package com.yogi.eklavya.devicenetwork.provider

import android.net.*
import android.os.Build
import android.util.Log
import com.yogi.eklavya.devicenetwork.DeviceNetworkCallback
import com.yogi.eklavya.devicenetwork.DeviceNetworkProvider
import com.yogi.eklavya.devicenetwork.NetworkState
import com.yogi.eklavya.devicenetwork.NetworkType
import com.yogi.eklavya.utils.ContextResource
import com.yogi.eklavya.utils.Resource

class BasicDeviceNetworkProvider(var context: Resource) : DeviceNetworkProvider {

    override var callback: DeviceNetworkCallback? = null
    private var networkState: NetworkState = NetworkState(NetworkType.NONE)
    private val connectivityManager = context.getSystemService(ContextResource.ConnectivityService) as ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null;
    private var networkRequest: NetworkRequest? = null

    override fun start() {
        checkState{networkState, stateToggled -> update(networkState, stateToggled)}
    }

    override fun stop() {
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    override fun addNetworkCallback(networkCallback: DeviceNetworkCallback) {
        callback = networkCallback
    }

    private fun checkState(notify: (NetworkState, Boolean) -> Unit) {
        supportBeforeAndroidLollypop {
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            setNetworkState(activeNetwork){networkState, stateToggled -> notify(networkState, stateToggled)}
        }
        supportAndroidLollypopAndAfter {
            networkCallback = networkCallback ?: object : ConnectivityManager.NetworkCallback() {
                override fun onLost(network: Network?) {
                    networkState = NetworkState(NetworkType.OFFLINE)
                    notify(networkState, true)
                }
                override fun onUnavailable() {
                    networkState = NetworkState(NetworkType.OFFLINE)
                    notify(networkState, true)
                }
                override fun onLosing(network: Network?, maxMsToLive: Int) {

                }
                override fun onAvailable(network: Network?) {
                    setNetworkType(network)
                    notify(networkState, true)
                }
            }
            networkRequest = networkRequest ?: NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .build()
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        }
    }

    private fun setNetworkType(network: Network?) {
        val networkCapabilities: NetworkCapabilities? = connectivityManager.getNetworkCapabilities(network)
        Log.d("Dig ", networkCapabilities?.toString())
        if (networkCapabilities != null) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                networkState.networkType = NetworkType.WIFI
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                networkState.networkType = NetworkType.LTE
            }
        }
        Log.d("Dig ", networkState?.toString())
    }

    private fun setNetworkState(networkInfo: NetworkInfo?, notify: (NetworkState, Boolean) -> Unit) {
        val wasOffline = isOffline();
        val lastNetworkType = networkState.networkType
        var networkType = NetworkType.NONE
        if (networkInfo?.isConnected == true) {
            when(networkInfo.type) {
                ConnectivityManager.TYPE_WIFI -> networkType = NetworkType.WIFI
                ConnectivityManager.TYPE_MOBILE -> networkType = NetworkType.LTE
            }
        } else {
            networkType = NetworkType.OFFLINE
        }
        networkState = NetworkState(networkType)
        if (lastNetworkType != networkState.networkType) {
            notify(networkState, wasOffline != isOffline())
        }
    }

    inline fun supportBeforeAndroidLollypop(code: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            code()
        }
    }

    inline fun supportAndroidLollypopAndAfter(code: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            code()
        }
    }

    override fun isNetworkAvailable(networkCallback: DeviceNetworkCallback) {
        checkState{networkState, stateToggled -> networkCallback.onStateChanged(networkState, stateToggled)}
    }

    override fun isNetworkAvailableBlocking() : NetworkState {
        if (networkState.networkType == NetworkType.NONE) {
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            setNetworkState(activeNetwork){_, _ -> {}}
        }
        return networkState;
    }

    private fun isOffline() = (networkState.networkType == NetworkType.OFFLINE
            || networkState.networkType == NetworkType.NONE)
}