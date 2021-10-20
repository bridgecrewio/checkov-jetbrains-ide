package com.bridgecrew.ui
<<<<<<< Updated upstream

=======
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
>>>>>>> Stashed changes
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

class CheckovNotificationBalloon {

    companion object {
<<<<<<< Updated upstream
=======
        val title = "Checkov"
>>>>>>> Stashed changes
        private val groupNeedAction = "CheckovNeedAction"
        private val GROUP = NotificationGroup(groupNeedAction, NotificationDisplayType.STICKY_BALLOON)

        fun showError(project: Project, failureNumber: Int) {
<<<<<<< Updated upstream
                val notification = GROUP.createNotification(
                    "Checkov has detected $failureNumber configuration error(s) in your project. Check out the tool window to analyze your code ",
                    NotificationType.WARNING
                )
                notification.notify(project)
=======
            if (!CheckovToolWindowPanel(project).isVisible) {
                val notification = GROUP.createNotification(
                    "Checkov has finished ",
                    NotificationType.WARNING
                ).addAction(
                    NotificationAction.createSimpleExpiring("Go to results\u2026") {
                        CheckovToolWindowPanel(project).isVisible = true

                        println("before direct")
                    }
                )

                notification.notify(project)
            }
>>>>>>> Stashed changes
        }
    }
}