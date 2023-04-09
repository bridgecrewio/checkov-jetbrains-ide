package com.bridgecrew.ui.rightPanel.extraInfoPanel

import com.bridgecrew.results.SecretsCheckovResult
import com.bridgecrew.ui.rightPanel.dictionaryDetails.SecretsDictionaryPanel

class SecretsExtraInfoPanel(result: SecretsCheckovResult) : CheckovExtraInfoPanel(result) {

    init {
        initLayout()
        createRightPanelDescriptionLine(result.name)
        add(SecretsDictionaryPanel(result))
        addCodeDiffPanel()
        setDimensions()
    }
}