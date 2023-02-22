package com.bridgecrew.services

import com.bridgecrew.CheckovResult
import com.bridgecrew.ResourceToCheckovResultsList
import com.bridgecrew.getFailedChecksFromResultString
import com.bridgecrew.results.*
import com.intellij.openapi.components.Service
import kotlin.io.path.Path

@Service
class ResultsCacheService {
    private var checkovResults: MutableList<BaseCheckovResult> = mutableListOf()
    private val results: MutableMap<String, ResourceToCheckovResultsList> = mutableMapOf()

    fun getAllCheckovResults(): List<BaseCheckovResult> {
        return this.checkovResults
    }


    fun getCheckovResultsFilteredBySeverityGroupedByPath(severitiesToFilterBy: List<Severity>?): Map<String, List<BaseCheckovResult>> {
        if(severitiesToFilterBy != null) {
            checkovResults.filter {baseCheckovResult ->
                severitiesToFilterBy.all { includedSeverity ->
                    includedSeverity == baseCheckovResult.severity
                }
            }
        }

        return this.checkovResults.groupBy { it.filePath.toString() }
    }

    fun getAllResults(): MutableMap<String, ResourceToCheckovResultsList> {
        return results
    }

    fun setResult(key: String, value: ResourceToCheckovResultsList) {
        results[key] = value
    }

    fun addCheckovResult(checkovResult: BaseCheckovResult) {
        checkovResults.add(checkovResult)
    }

    fun deleteAll() {
        results.keys.forEach {
            results.remove(it)
        }
    }

    fun deleteAllCheckovResults() {
        checkovResults.clear()
    }

    fun setMockCheckovResultsFromExampleFile() {
        val inputString: String = javaClass.classLoader.getResource("examples/example-output.json").readText()
        val checkovResults: List<CheckovResult> = getFailedChecksFromResultString(inputString)
        checkovResultsFromResultsList(checkovResults)
    }
    fun checkovResultsFromResultsList(results: List<CheckovResult>) {
        for (result in results) {
            val category = mapCheckovCheckTypeToScanType(result.check_type, result.check_id)
            val resource = (if (category == Category.VULNERABILITIES) result.vulnerability_details?.package_name else result.resource)
                    ?: throw Exception("null resource, category is ${category.name}, result is $result")
            val name = getResourceName(result, category)
                    ?: throw Exception("null name, category is ${category.name}, result is $result")
            val checkType = CheckType.valueOf(result.check_type.uppercase())
            val severity = if (result.severity != null) Severity.valueOf(result.severity.uppercase()) else Severity.UNKNOWN

            when (category) {
                Category.VULNERABILITIES -> {
                    if (result.vulnerability_details == null) {
                        throw Exception("type is vulnerability but no vulnerability_details")
                    }
                    val vulnerabilityCheckovResult = VulnerabilityCheckovResult(checkType, Path(result.file_path),
                            resource, name, result.check_id, severity, result.description,
                            result.guideline, result.file_abs_path, result.file_line_range, result.fixed_definition,
                            result.code_block,
                            result.vulnerability_details.cvss,
                            result.vulnerability_details.package_version,
                            result.vulnerability_details.lowest_fixed_version,
                            result.vulnerability_details.link,
                            result.vulnerability_details.published_date,
                            result.vulnerability_details.vector,
                            result.check_id,
                            result.file_path,
                            result.vulnerability_details.risk_factors
                    )
                    addToSorted(checkovResults, vulnerabilityCheckovResult)
                    continue
                }
                Category.SECRETS -> {
                    val secretCheckovResult = SecretsCheckovResult(checkType, Path(result.file_path),
                            resource, name, result.check_id, severity, result.description,
                            result.guideline, result.file_abs_path, result.file_line_range, result.fixed_definition,
                            result.code_block)
                    addToSorted(checkovResults, secretCheckovResult)
                    continue
                }
                Category.IAC -> {
                    val iacCheckovResult = IacCheckovResult(checkType, Path(result.file_path),
                            resource, name, result.check_id, severity, result.description,
                            result.guideline, result.file_abs_path, result.file_line_range, result.fixed_definition,
                            result.code_block)
                    addToSorted(checkovResults, iacCheckovResult)
                    continue
                }
                Category.LICENSES -> {
                    if (result.vulnerability_details == null) {
                        throw Exception("type is license but no vulnerability_details")
                    }

                    val licenseCheckovResult = LicenseCheckovResult(checkType, Path(result.file_path),
                            resource, name, result.check_id, severity, result.description,
                            result.guideline, result.file_abs_path, result.file_line_range, result.fixed_definition,
                            result.code_block,
                            result.vulnerability_details.licenses,
                            result.check_id.uppercase() == "BC_LIC_1"
                    )
                    addToSorted(checkovResults, licenseCheckovResult)
                    continue
                }
            }
        }
    }
    private val checkovResultsComparator: Comparator<BaseCheckovResult> = compareBy({ it.filePath }, { it.resource }, {it.severity})
    private fun addToSorted(checkovResults: MutableList<BaseCheckovResult>, checkovResult: BaseCheckovResult) {
        var index = checkovResults.binarySearch(checkovResult, checkovResultsComparator)
        val insertionPoint =
                if (index < 0) {
                    -(index + 1)
                } else {
                    index
                }
        checkovResults.add(insertionPoint, checkovResult)
    }
    fun mapCheckovCheckTypeToScanType(checkType: String, checkId: String): Category {
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

    fun getResourceName(result: CheckovResult, category: Category): String? {
        return when (category) {
            Category.IAC, Category.SECRETS -> {
                result.check_name
            }

            Category.LICENSES -> {
                result.vulnerability_details?.licenses ?: "NOT FOUND"
            }

            Category.VULNERABILITIES -> {
                result.vulnerability_details?.id
            }
        }
    }
}
