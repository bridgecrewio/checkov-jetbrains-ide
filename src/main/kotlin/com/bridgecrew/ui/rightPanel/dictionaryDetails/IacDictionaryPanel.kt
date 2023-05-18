package com.bridgecrew.ui.rightPanel.dictionaryDetails

import com.bridgecrew.results.BaseCheckovResult

class IacDictionaryPanel(result: BaseCheckovResult) : DictionaryExtraInfoPanel() {

    override var fieldsMap: MutableMap<String, Any?> = mutableMapOf(
            "Resource" to result.resource,
            "Description" to result.description
    )
    init {
        addCustomPolicyGuidelinesIfNeeded(result)
        createDictionaryLayout()
    }
}