package com.bridgecrew.listeners
import com.bridgecrew.services.scan.CheckovScanService
import com.intellij.util.messages.Topic

interface CheckovScanListener {

    companion object {
        val SCAN_TOPIC =
            Topic.create("Checkov scan", CheckovScanListener::class.java)
    }

    fun projectScanningStarted()

    fun scanningFinished(scanSourceType: CheckovScanService.ScanSourceType)

    fun fullScanFailed()
}
