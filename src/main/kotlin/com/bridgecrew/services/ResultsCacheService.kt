package com.bridgecrew.services

import com.bridgecrew.CheckovResult
import com.bridgecrew.results.*
import com.bridgecrew.services.scan.CheckovScanService
import com.bridgecrew.utils.CheckovUtils
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.jetbrains.rpc.LOG
import java.io.File
import java.nio.file.Paths

@Service
class ResultsCacheService(val project: Project) {
    var checkovResults: MutableList<BaseCheckovResult> = mutableListOf()

    private val baseDir: String = project.basePath!!

    fun getAllCheckovResults(): List<BaseCheckovResult> {
        return this.checkovResults
    }

    fun addCheckovResult(checkovResult: BaseCheckovResult) {
        checkovResults.add(checkovResult)
    }

    fun addCheckovResults(newCheckovResults: List<CheckovResult>, scanSourceType: CheckovScanService.ScanSourceType) {
        if (scanSourceType == CheckovScanService.ScanSourceType.FILE) {
            newCheckovResults.forEach { newCheckovResult ->
                run {
                    checkovResults.removeIf { savedCheckovResult -> savedCheckovResult.absoluteFilePath == newCheckovResult.file_abs_path }

                }
            }
        }

        setCheckovResultsFromResultsList(newCheckovResults)
    }

    fun deleteAllCheckovResults() {
        checkovResults.clear()
    }

    fun setMockCheckovResultsFromExampleFile() {
        val inputString: String = javaClass.classLoader.getResource("examples/example-output.json").readText()
        val checkovResults: List<CheckovResult> = CheckovUtils.extractFailedChecksAndParsingErrorsFromCheckovResult(inputString, "mock - examples/example-output.json").failedChecks
        setCheckovResultsFromResultsList(checkovResults)
    }
    fun setCheckovResultsFromResultsList(results: List<CheckovResult>) {
        for (result in results) {
            val category: Category = mapCheckovCheckTypeToScanType(result.check_type, result.check_id)
            val resource: String = getResource(result, category)
            val name: String = getResourceName(result, category)
                    ?: throw Exception("null name, category is ${category.name}, result is $result")
            val checkType = CheckType.valueOf(result.check_type.uppercase())
            val severity = if (result.severity != null) Severity.valueOf(result.severity.uppercase()) else Severity.UNKNOWN
            val description = if(!result.description.isNullOrEmpty()) result.description else result.short_description
            val filePath = result.file_abs_path.replace(baseDir, "")
            val fileAbsPath = if (!result.file_abs_path.contains(baseDir)) Paths.get(baseDir, File.separator, result.file_abs_path).toString() else result.file_abs_path

            when (category) {
                Category.VULNERABILITIES -> {
                    if (result.vulnerability_details == null) {
                        throw Exception("type is vulnerability but no vulnerability_details")
                    }

                    // deployment.yaml (alpine:3.10 lines:11-29 (sha256:e7b300aee9)).openssl
                    val vulnerabilityRootPackageData = result.resource
//                    val imageName = getVulnerabilityImage(result)
                    val vulnerabilityCheckovResult = VulnerabilityCheckovResult(
                            checkType, filePath,
                            resource, name, result.check_id, severity, description,
                            result.guideline, fileAbsPath, result.file_line_range, result.fixed_definition,
                            result.code_block,
                            result.vulnerability_details.cvss,
                            result.vulnerability_details.package_name,
                            result.vulnerability_details.package_version,
                            result.vulnerability_details.lowest_fixed_version,
                            result.vulnerability_details.link,
                            result.vulnerability_details.published_date,
                            result.vulnerability_details.vector,
                            result.vulnerability_details.id,
                            result.file_path,
                            null, // TODO - fix after Saar's team fixes their side
                            result.vulnerability_details.root_package_name,
                            result.vulnerability_details.root_package_version,
                            result.vulnerability_details.root_package_fix_version,
                            getVulnerabilityImage(result)
                            )
                    checkovResults.add(vulnerabilityCheckovResult)

                    continue
                }
                Category.SECRETS -> {
                    val secretCheckovResult = SecretsCheckovResult(checkType, filePath,
                            resource, name, result.check_id, severity, description,
                            result.guideline, fileAbsPath, result.file_line_range, result.fixed_definition,
                            result.code_block)
                    checkovResults.add(secretCheckovResult)
                    continue
                }
                Category.IAC -> {
                    val iacCheckovResult = IacCheckovResult(checkType, filePath,
                            resource, name, result.check_id, severity, description,
                            result.guideline, fileAbsPath, result.file_line_range, result.fixed_definition,
                            result.code_block)
                    checkovResults.add(iacCheckovResult)
                    continue
                }
                Category.LICENSES -> {
                    if (result.vulnerability_details == null) {
                        throw Exception("type is license but no vulnerability_details")
                    }

                    val licenseCheckovResult = LicenseCheckovResult(checkType, filePath,
                            resource, name, result.check_id, severity, description,
                            result.guideline, fileAbsPath, result.file_line_range, result.fixed_definition,
                            result.code_block,
                            result.vulnerability_details.package_name,
                            result.vulnerability_details.license,
                            result.check_id.uppercase() == "BC_LIC_1"
                    )
                    checkovResults.add(licenseCheckovResult)
                    continue
                }
            }
        }
    }


    //{
    //          "check_id": "BC_LIC_2",
    //          "bc_check_id": "BC_LIC_2",
    //          "check_name": "SCA license",
    //          "check_result": {
    //            "result": "FAILED"
    //          },
    //          "code_block": [
    //            [
    //              0,
    //              "pam: 1.4.0-9+deb11u1"
    //            ]
    //          ],
    //          "file_path": "/features/image-referncer/IaC/ecs.tf (nginx lines:1-31 (sha256:6efc10a051))",
    //          "file_abs_path": "/Users/mshavit/source/testing-resources/features/image-referncer/IaC/ecs.tf",
    //          "repo_file_path": "/source/testing-resources/features/image-referncer/IaC/ecs.tf",
    //          "file_line_range": [
    //            0,
    //            0
    //          ],
    //          "resource": "features/image-referncer/IaC/ecs.tf (nginx lines:1-31 (sha256:6efc10a051)).pam",
    //          "evaluations": null,
    //          "check_class": "checkov.common.bridgecrew.vulnerability_scanning.image_scanner.ImageScanner",
    //          "fixed_definition": null,
    //          "entity_tags": null,
    //          "caller_file_path": null,
    //          "caller_file_line_range": null,
    //          "resource_address": null,
    //          "severity": "LOW",
    //          "bc_category": null,
    //          "benchmarks": null,
    //          "description": null,
    //          "short_description": "License GPL - pam: 1.4.0-9+deb11u1",
    //          "vulnerability_details": {
    //            "package_name": "pam",
    //            "package_version": "1.4.0-9+deb11u1",
    //            "package_registry": "",
    //            "is_private_registry": false,
    //            "license": "GPL",
    //            "status": "FAILED",
    //            "policy": "BC_LIC_2",
    //            "package_type": "os"
    //          },
    //          "connected_node": null,
    //          "guideline": "https://docs.bridgecrew.io/docs/open-source-package-unknown-license",
    //          "details": [],
    //          "check_len": null,
    //          "definition_context_file_path": null
    //        }
    private fun mapCheckovCheckTypeToScanType(checkType: String, checkId: String): Category {
        when (checkType) {
            "ansible", "arm", "bicep", "cloudformation", "dockerfile", "helm", "json",
            "yaml", "kubernetes", "kustomize", "openapi", "serverless", "terraform", "terraform_plan" -> {
                return Category.IAC
            }

            "secrets" -> {
                return Category.SECRETS
            }

            "sca_package", "sca_image" -> {
                if (checkId.uppercase().startsWith("BC_LIC")) {
                    return Category.LICENSES
                } else if (checkId.uppercase().startsWith("BC_VUL")) {
                    return Category.VULNERABILITIES
                }
            }
        }

        throw Exception("Scan type is not found in the result!")
    }

    private fun getResourceName(result: CheckovResult, category: Category): String? {
        return when (category) {
            Category.IAC, Category.SECRETS -> {
                "${result.check_name} (${result.file_line_range[0]} - ${result.file_line_range[1]})"
            }

            Category.LICENSES -> {
                result.vulnerability_details?.license ?: "NOT FOUND"
            }

            Category.VULNERABILITIES -> {
                result.vulnerability_details?.id
            }
        }
    }
    private fun getResource(result: CheckovResult, category: Category) : String {
        if (category == Category.VULNERABILITIES || category == Category.LICENSES) {
            return result.vulnerability_details?.package_name ?: throw Exception("null resource, category is ${category.name}, result is $result")
        }

        return result.resource
    }

    private fun getVulnerabilityImage(result: CheckovResult): String {
        //"resource": "features/image-referncer/IaC/ecs.tf (nginx lines:1-31 (sha256:6efc10a051)).curl",

        try {
            val image = result.resource.split(" ").find { token -> token.startsWith("(") }
            return image!!.replace("(", "")
        } catch (error: Error) {
            LOG.warn("Could not find image name from result resource ${result.resource}", error)
            return ""
        }
    }
}
