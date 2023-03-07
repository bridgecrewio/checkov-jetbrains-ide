package com.bridgecrew.utils

const val CHECKNAMEDEPTH = 4
const val POLICYDETAILS = "Policy Details:"
const val GUIDELINES_TITLE = "View Guidelines"
const val CUSTOM_GUIDELINES_TITLE = "Guidelines:"

object PANELTYPE {
    const val AUTO_CHOOSE_PANEL = 0
    const val CHECKOV_SCAN_FINISHED_EMPTY = 1
    const val CHECKOV_SCAN_FINISHED = 2
    const val CHECKOV_SCAN_STARTED = 3
    const val CHECKOV_REPOSITORY_SCAN_STARTED = 8
    const val CHECKOV_SCAN_ERROR = 4
    const val CHECKOV_PRE_SCAN = 5
    const val CHECKOV_SCAN_PARSING_ERROR = 6
    const val CHECKOV_INSTALATION_STARTED = 7
}

const val DEFAULT_TIMEOUT: Long = 80000

const val GIT_DEFAULT_REPOSITORY_NAME = "jetbrains/extension"

const val DOCKER_MOUNT_DIR = "/checkovScan"

val FULL_SCAN_FRAMEWORKS = arrayListOf("ansible", "arm", "bicep", "cloudformation", "dockerfile", "helm", "json",
        "yaml", "kubernetes", "kustomize", "openapi", "sca_package", "sca_image", "secrets", "serverless", "terraform", "terraform_plan")
val FULL_SCAN_EXCLUDED_PATHS = arrayListOf("node_modules")

val DESIRED_NUMBER_OF_FRAMEWORK_FOR_FULL_SCAN = FULL_SCAN_FRAMEWORKS.size

enum class FileType {
    JSON,
    TERRAFORM,
    YAML,
    DOCKERFILE,
    UNKNOWN
}