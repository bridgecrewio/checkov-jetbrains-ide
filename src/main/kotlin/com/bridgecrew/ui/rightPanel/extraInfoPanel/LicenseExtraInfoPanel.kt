package com.bridgecrew.ui.rightPanel.extraInfoPanel

import com.bridgecrew.results.LicenseCheckovResult
import com.bridgecrew.ui.rightPanel.dictionaryDetails.LicenseDictionaryPanel
import com.bridgecrew.utils.CheckovUtils

class LicenseExtraInfoPanel(result: LicenseCheckovResult) : CheckovExtraInfoPanel(result) {
    init {
        initLayout()

        val description = CheckovUtils.createLicenseDescription(result)
        if (description.isNotEmpty()) {
            createRightPanelDescriptionLine(description)

        }
        
        add(LicenseDictionaryPanel(result))
        addCodeDiffPanel()
        setDimensions()
    }
}