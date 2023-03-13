package com.bridgecrew.ui.rightPanel.extraInfoPanel

import com.bridgecrew.results.VulnerabilityCheckovResult
import com.bridgecrew.ui.rightPanel.dictionaryDetails.VulnerabilitiesDictionaryPanel

class VulnerabilitiesExtraInfoPanel(result: VulnerabilityCheckovResult): CheckovExtraInfoPanel(result) {

    init {
        initLayout()
        createRightPanelDescriptionLine(result.name)
        add(VulnerabilitiesDictionaryPanel(result))
        addCodeDiffPanel()
        setDimensions()
    }
}