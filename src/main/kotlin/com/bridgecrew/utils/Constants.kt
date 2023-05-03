package com.bridgecrew.utils

const val GUIDELINES_TITLE = "View Guidelines"
const val CUSTOM_GUIDELINES_TITLE = "Guidelines:"

object PANELTYPE {
    const val AUTO_CHOOSE_PANEL = 0
    const val CHECKOV_FILE_SCAN_FINISHED = 1
    const val CHECKOV_FRAMEWORK_SCAN_FINISHED = 2
    const val CHECKOV_REPOSITORY_SCAN_STARTED = 3
    const val CHECKOV_REPOSITORY_SCAN_FAILED = 4
    const val CHECKOV_INITIALIZATION_PROGRESS = 5
    const val CHECKOV_LOAD_TABS_CONTENT = 6
}

const val DEFAULT_FILE_TIMEOUT: Long = 80000 // 1.3 minutes
const val DEFAULT_FRAMEWORK_TIMEOUT: Long = 720000 // 12 minutes

const val GIT_DEFAULT_REPOSITORY_NAME = "jetbrains/extension"

val FULL_SCAN_FRAMEWORKS = arrayListOf("ansible", "arm", "bicep", "cloudformation", "dockerfile", "helm", "json",
        "yaml", "kubernetes", "kustomize", "openapi", "sca_package", "sca_image", "secrets", "serverless", "terraform", "terraform_plan")
val FULL_SCAN_EXCLUDED_PATHS = arrayListOf("node_modules")
const val FULL_SCAN_STATE_FILE = "full_scan_state"

val DESIRED_NUMBER_OF_FRAMEWORK_FOR_FULL_SCAN = FULL_SCAN_FRAMEWORKS.size
val DESIRED_NUMBER_OF_SINGLE_FILE_SCANS = 10

enum class FileType {
    JSON,
    TERRAFORM,
    YAML,
    DOCKERFILE,
    JAVASCRIPT,
    TYPESCRIPT,
    JAVA,
    KOTLIN,
    PYTHON,
    TEXT,
    XML,
    UNKNOWN
}

val SUPPRESSION_BUTTON_ALLOWED_FILE_TYPES: Set<FileType> = setOf(
        FileType.DOCKERFILE,
        FileType.YAML,
        FileType.TERRAFORM
)
