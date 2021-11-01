package com.bridgecrew.ui

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

class CheckovNotificationBalloon {

    companion object {
        private val groupNeedAction = "CheckovNeedAction"
        private val GROUP = NotificationGroup(groupNeedAction, NotificationDisplayType.STICKY_BALLOON)

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