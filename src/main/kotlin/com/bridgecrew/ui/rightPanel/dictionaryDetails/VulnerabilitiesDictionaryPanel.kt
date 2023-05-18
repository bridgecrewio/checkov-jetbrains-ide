package com.bridgecrew.ui.rightPanel.dictionaryDetails

import com.bridgecrew.results.CheckType
import com.bridgecrew.results.VulnerabilityCheckovResult

class VulnerabilitiesDictionaryPanel(result: VulnerabilityCheckovResult): DictionaryExtraInfoPanel() {

    override var fieldsMap: MutableMap<String, Any?> = generateFieldsMap(result)

    init {
        addCustomPolicyGuidelinesIfNeeded(result)
        createDictionaryLayout()
    }

    private fun generateFieldsMap(result: VulnerabilityCheckovResult): MutableMap<String, Any?> {
        val vulnerabilityFieldsMap: MutableMap<String, Any?> = mutableMapOf(
        "Vulnerable Package" to result.packageName,
        "Vulnerable Package Version" to result.packageVersion,
        "Fixed Version" to result.fixVersion,
        )

        vulnerabilityFieldsMap.putAll(generateFieldsByCheckType(result))

        vulnerabilityFieldsMap.putAll(mapOf(
                       "CVE ID" to result.violationId,
                       "CVSS" to result.cvss,
                       "Published" to result.publishedDate,
                       "Vector" to result.vector,
                       "Risk Factors" to result.riskFactors,
                       "Description" to result.description)
        )

        return vulnerabilityFieldsMap
    }
    private fun generateFieldsByCheckType(result: VulnerabilityCheckovResult): Map<String, String?> {
        if (result.checkType == CheckType.SCA_IMAGE) {
            return mapOf("Vulnerable Image" to result.resource)
        }

        if (result.checkType == CheckType.SCA_PACKAGE) {
            return mapOf("Root Package" to result.rootPackageName,
            "Root Package Version" to result.rootPackageVersion,
            "Compliant Version (Fix all CVEs)" to result.rootPackageFixVersion)
        }

        return mapOf()
    }
}