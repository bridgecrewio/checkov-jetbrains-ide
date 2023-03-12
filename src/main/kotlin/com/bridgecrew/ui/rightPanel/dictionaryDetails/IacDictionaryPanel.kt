package com.bridgecrew.ui.rightPanel.dictionaryDetails

import com.bridgecrew.results.BaseCheckovResult

class IacDictionaryPanel(result: BaseCheckovResult) : DictionaryExtraInfoPanel() {

    override var fieldsMap: MutableMap<String, Any?> = mutableMapOf(
            "Resource" to result.resource,
            "Policy Name" to result.name
    )
    init {
        addCustomPolicyGuidelinesIfNeeded(result)
        createDictionaryLayout()
    }
}