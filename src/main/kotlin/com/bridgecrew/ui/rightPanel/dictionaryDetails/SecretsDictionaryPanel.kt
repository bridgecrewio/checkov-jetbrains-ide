package com.bridgecrew.ui.rightPanel.dictionaryDetails

import com.bridgecrew.results.SecretsCheckovResult

class SecretsDictionaryPanel(result: SecretsCheckovResult): DictionaryExtraInfoPanel() {
    override var fieldsMap: MutableMap<String, Any?> = mutableMapOf(
            "Severity" to result.severity,
            "Description" to result.description,
            "Code" to extractCode(result),
            "Category" to result.category, // TODO - remove before release
            "Check Type" to result.checkType.name, // TODO - remove before release
    )
    init {
        addCustomPolicyGuidelinesIfNeeded(result)
        createDictionaryLayout()
    }

    private fun extractCode(result: SecretsCheckovResult): Any {
        return try {
            result.codeBlock[0][1]
        } catch (e: Exception) {
            ""
        }
    }
}