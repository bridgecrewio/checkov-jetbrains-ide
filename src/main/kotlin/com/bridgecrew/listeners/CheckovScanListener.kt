package com.bridgecrew.listeners
import com.bridgecrew.CheckovResult
import com.intellij.util.messages.Topic

interface CheckovScanListener {

    companion object {
        val SCAN_TOPIC =
            Topic.create("Checkov scan", CheckovScanListener::class.java)
    }

    fun scanningStarted()

    fun scanningFinished()

    fun scanningFinished(fileName: String)

    fun scanningError()
}
