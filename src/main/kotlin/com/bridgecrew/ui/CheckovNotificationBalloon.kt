package com.bridgecrew.ui

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

class CheckovNotificationBalloon {
    companion object {
        private const val groupId = "CheckovError"
        private val notificationGroupManager = NotificationGroupManager.getInstance().getNotificationGroup(groupId)
        fun showNotification(project: Project, notificationContent: String, notificationType: NotificationType) {
            val notification = notificationGroupManager.createNotification(
                    notificationContent,
                    notificationType
            )
            notification.notify(project)

        }
    }
}