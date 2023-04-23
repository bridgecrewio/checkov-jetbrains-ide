package com.bridgecrew.ui.rightPanel.dictionaryDetails

import com.bridgecrew.results.BaseCheckovResult

class IacDictionaryPanel(result: BaseCheckovResult) : DictionaryExtraInfoPanel() {

    override var fieldsMap: MutableMap<String, Any?> = mutableMapOf(
            "Resource" to result.resource,
            "Severity" to result.severity,
            "Policy Name" to result.name,
            "Description" to result.description,
            "Category" to result.category, // TODO - remove before release
            "Check Type" to result.checkType.name.lowercase(), // TODO - remove before release
    )
    init {
        addCustomPolicyGuidelinesIfNeeded(result)
        createDictionaryLayout()
    }
}