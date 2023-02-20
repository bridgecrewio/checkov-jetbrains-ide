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
    const val CHECKOV_SCAN_ERROR = 4
    const val CHECKOV_PRE_SCAN = 5
    const val CHECKOV_SCAN_PARSING_ERROR = 6
    const val CHECKOV_INSTALATION_STARTED = 7
}

const val DEFAULT_TIMEOUT: Long = 80000

const val GIT_DEFAULT_REPOSITORY_NAME = "jetbrains/extension"

const val DOCKER_MOUNT_DIR = "/checkovScan"