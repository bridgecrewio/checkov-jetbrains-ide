package com.bridgecrew.ui

import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.utils.DESIRED_NUMBER_OF_FRAMEWORK_FOR_FULL_SCAN
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class CheckovErrorNotificationBalloon {
    companion object {
        private val groupId = "CheckovError"
        private val notificationGroupManager = NotificationGroupManager.getInstance().getNotificationGroup(groupId)
        fun showError(project: Project, notificationContent: String, notificationType: NotificationType) {
//            var balloonContent: String = "Error while running checkov on , please check the error logs"
//                if (failureNumber == 1){
//                    balloonContent= "Checkov has detected $failureNumber configuration error in your project. Check out the tool window to analyze your code "
//                } else {
//                    balloonContent =
//                        "Checkov has detected $failureNumber configuration errors in your project. Check out the tool window to analyze your code "
//                }
            val notification = notificationGroupManager.createNotification(
                    notificationContent,
                notificationType
            )
            notification.notify(project)

        }
    }
}