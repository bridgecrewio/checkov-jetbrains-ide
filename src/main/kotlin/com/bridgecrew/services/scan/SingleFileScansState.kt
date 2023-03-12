package com.bridgecrew.services.scan

class SingleFileScansState {
    private val currentScans = mutableMapOf<String, String>()

    fun startASingleFileScan(filePath: String) {
        if (currentScans.isEmpty() || !currentScans.containsKey(filePath)) {
            currentScans[filePath] = ""
        }


    }

}