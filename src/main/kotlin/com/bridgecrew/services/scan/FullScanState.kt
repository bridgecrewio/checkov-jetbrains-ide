package com.bridgecrew.services.scan

import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.ui.CheckovNotificationBalloon
import com.bridgecrew.utils.DESIRED_NUMBER_OF_FRAMEWORK_FOR_FULL_SCAN
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service
class FullScanStateService {
    private var fullScanFinishedFrameworksNumber = 0

    fun fullScanStarted() {
        fullScanFinishedFrameworksNumber = 0
    }

    fun fullScanFrameworkFinished(project: Project) {
        fullScanFinishedFrameworksNumber++
        if (fullScanFinishedFrameworksNumber == DESIRED_NUMBER_OF_FRAMEWORK_FOR_FULL_SCAN) {
            val totalErrors = project.service<ResultsCacheService>().getAllCheckovResults().size
            val errorMessage = "Checkov has detected $totalErrors configuration errors in your project. Check out the tool window to analyze your code"
            CheckovNotificationBalloon.showNotification(project, errorMessage, NotificationType.INFORMATION)
        }
    }
}