package com.bridgecrew.utils

import java.io.File
import java.nio.file.Files
import java.nio.file.Path

const val CHECKNAMEDEPTH = 4
const val POLICYDETAILS = "Policy Details:"
const val GUIDELINES_TITLE = "View Guidelines"
const val CUSTOM_GUIDELINES_TITLE = "Guidelines:"

object PANELTYPE {
    const val AUTO_CHOOSE_PANEL = 0
    const val CHECKOV_FILE_SCAN_FINISHED = 1
    const val CHECKOV_FRAMEWORK_SCAN_FINISHED = 2
    const val CHECKOV_REPOSITORY_SCAN_STARTED = 3
    const val CHECKOV_INITIALIZATION_PROGRESS = 4
}

const val DEFAULT_TIMEOUT: Long = 1800000

const val GIT_DEFAULT_REPOSITORY_NAME = "jetbrains/extension"

const val DOCKER_MOUNT_DIR = "/checkovScan"

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

