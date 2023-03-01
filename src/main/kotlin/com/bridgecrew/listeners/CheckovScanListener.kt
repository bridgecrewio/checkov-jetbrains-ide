package com.bridgecrew.listeners
import com.intellij.util.messages.Topic

interface CheckovScanListener {

    companion object {
        val SCAN_TOPIC =
            Topic.create("Checkov scan", CheckovScanListener::class.java)
    }

    fun scanningStarted()

    fun projectScanningStarted()

    fun scanningFinished()

    fun frameworkScanningFinished()

    fun scanningFinished(fileName: String)

    fun scanningParsingError()

    fun scanningError()
}
