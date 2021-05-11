<<<<<<< HEAD:src/main/kotlin/com/github/bridgecrewio/checkov/services/MyProjectService.kt
package com.github.bridgecrewio.checkov.services

import com.github.bridgecrewio.checkov.MyBundle
=======
package com.bridgecrew.services

import com.bridgecrew.MyBundle
>>>>>>> 5069401bc9c3f0dba82fdbef6c97312569d32725:src/main/kotlin/com/bridgecrew/services/MyProjectService.kt
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
        val checkovService = CheckovService()
        checkovService.installCheckov()
    }
}
