package io.github.dovecoteescapee.byedpi.core

import androidx.annotation.WorkerThread

class ByeDpiProxy {
    companion object {
        init {
            System.loadLibrary("byedpi")
        }
    }

    @WorkerThread
    fun startProxy(args: Array<String>): Int {
        val result = createSocket(args)

        if (result < 0) {
            return -1
        }

        val code = jniStartProxy()

        return code
    }

    fun stopProxy(): Int {
        val result = jniStopProxy()

        if (result < 0) {
            return -1
        }

        return result
    }

    @WorkerThread
    fun isProxyActive(): Boolean {
        return jniCheckProxy() == 1
    }

    private fun createSocket(args: Array<String>): Int {
        val result = createSocketFromPreferences(args)

        if (result < 0) {
            return -1
        }

        return result
    }

    private fun createSocketFromPreferences(args: Array<String>) =
        jniCreateSocket(args)

    private external fun jniCreateSocket(args: Array<String>): Int
    private external fun jniStartProxy(): Int
    private external fun jniStopProxy(): Int
    private external fun jniCheckProxy(): Int
}
