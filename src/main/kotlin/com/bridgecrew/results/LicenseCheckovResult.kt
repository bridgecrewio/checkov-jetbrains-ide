package com.bridgecrew.results

import java.nio.file.Path

class LicenseCheckovResult(
        checkType: CheckType,
        filePath: String,
        resource: String,
        name: String,
        id: String,
        severity: Severity,
        description: String?,
        guideline: String?,
        absoluteFilePath: String,
        fileLineRange: List<Int>,
        fixDefinition: String?,
        codeBlock: List<List<Object>>,
        val licenseType: String?,
        val approvedSPDX: Boolean) :
        BaseCheckovResult(
                category = Category.LICENSES,
                checkType,
                filePath,
                resource,
                name,
                id,
                severity,
                description,
                guideline,
                absoluteFilePath,
                fileLineRange,
                fixDefinition,
                codeBlock) {}