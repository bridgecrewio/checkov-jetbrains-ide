package com.bridgecrew.ui

import com.bridgecrew.CheckovResult
import com.intellij.openapi.project.Project

class CheckovFileNameTreeNode(val checkovResult: CheckovResult, val project: Project): CheckovTreeNode(checkovResult) {
    override fun toString(): String {
        return checkovResult.file_abs_path.replace(project.basePath!!, "")
    }
}