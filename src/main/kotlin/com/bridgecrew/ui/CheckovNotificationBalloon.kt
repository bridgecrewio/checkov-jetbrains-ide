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
                val notification = GROUP.createNotification(
                    "Checkov has detected $failureNumber configuration error(s) in your project. Check out the tool window to analyze your code ",
                    NotificationType.WARNING
                )
                notification.notify(project)
        }
    }
}