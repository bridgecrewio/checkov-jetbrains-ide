<<<<<<< HEAD:src/main/kotlin/com/github/bridgecrewio/checkov/listeners/MyProjectManagerListener.kt
package com.github.bridgecrewio.checkov.listeners

import com.github.bridgecrewio.checkov.services.MyProjectService
=======
package com.bridgecrew.listeners

import com.bridgecrew.services.MyProjectService
>>>>>>> 5069401bc9c3f0dba82fdbef6c97312569d32725:src/main/kotlin/com/bridgecrew/listeners/MyProjectManagerListener.kt
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

internal class MyProjectManagerListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        project.service<MyProjectService>()
    }
}
