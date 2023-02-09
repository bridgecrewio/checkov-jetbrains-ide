package com.bridgecrew.services.checkovService

import com.intellij.openapi.components.Service

class CheckovCliFlagsConfig() {
    companion object {
        val frameworks = arrayListOf("ansible", "arm", "bicep", "cloudformation", "dockerfile", "helm", "json",
                "yaml", "kubernetes", "kustomize", "openapi", "sca_package", "sca_image", "secrets", "serverless", "terraform", "terraform_plan")
        val excludedPaths = arrayListOf("node_modules")
    }

}