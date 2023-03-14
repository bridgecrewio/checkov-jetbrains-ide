package com.bridgecrew.ui.rightPanel.extraInfoPanel

import com.bridgecrew.results.BaseCheckovResult

class SecretsExtraInfoPanel(result: BaseCheckovResult): CheckovExtraInfoPanel(result) {

    init {
        initLayout()
        createRightPanelDescriptionLine(result.name)
        addCodeDiffPanel()
        setDimensions()
    }
}