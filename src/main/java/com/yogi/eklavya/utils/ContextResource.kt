package com.yogi.eklavya.utils

import android.content.Context

class ContextResource(var context: Context) : Resource {

    override fun getSystemService(name: String) = context.getSystemService(name)

    companion object {
        val ConnectivityService: String = Context.CONNECTIVITY_SERVICE
    }
}