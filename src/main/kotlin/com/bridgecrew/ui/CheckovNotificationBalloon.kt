package com.bridgecrew.ui

import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.utils.DESIRED_NUMBER_OF_FRAMEWORK_FOR_FULL_SCAN
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class CheckovNotificationBalloon {

    companion object {
        private val groupNeedAction = "CheckovNeedAction"
        private val GROUP = NotificationGroup(groupNeedAction, NotificationDisplayType.STICKY_BALLOON)
        private var fullScanFinishedFrameworksNumber = 0

        fun showFullScanError(project: Project) {
            fullScanFinishedFrameworksNumber++
            if (fullScanFinishedFrameworksNumber == DESIRED_NUMBER_OF_FRAMEWORK_FOR_FULL_SCAN) {
                fullScanFinishedFrameworksNumber = 0
                showError(project, project.service<ResultsCacheService>().getAllCheckovResults().size)
            }
        }

        fun showError(project: Project, failureNumber: Int) {
            var balloonContent: String
                if (failureNumber == 1){
                    balloonContent= "Checkov has detected $failureNumber configuration error in your project. Check out the tool window to analyze your code "
                } else {
                    balloonContent =
                        "Checkov has detected $failureNumber configuration errors in your project. Check out the tool window to analyze your code "
                }
            val notification = GROUP.createNotification(
                balloonContent,
                NotificationType.WARNING
            )
            notification.notify(project)

        }
    }
}