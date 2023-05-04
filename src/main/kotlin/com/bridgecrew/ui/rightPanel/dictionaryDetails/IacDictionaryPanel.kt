package com.bridgecrew.ui.rightPanel.dictionaryDetails

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.utils.CheckovUtils

class IacDictionaryPanel(result: BaseCheckovResult) : DictionaryExtraInfoPanel() {

    override var fieldsMap: MutableMap<String, Any?> = mutableMapOf(
            "Resource" to result.resource,
            "Severity" to result.severity,
            "Description" to result.description,
            "Category" to result.category, // TODO - remove before release
            "Check Type" to result.checkType.name.lowercase(), // TODO - remove before release
            "Custom Policy" to CheckovUtils.isCustomPolicy(result) // TODO - remove before release
    )
    init {
        addCustomPolicyGuidelinesIfNeeded(result)
        createDictionaryLayout()
    }
}