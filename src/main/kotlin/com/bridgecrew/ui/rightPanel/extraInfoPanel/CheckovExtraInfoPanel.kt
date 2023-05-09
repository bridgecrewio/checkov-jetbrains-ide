package com.bridgecrew.ui.rightPanel.extraInfoPanel

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.ui.CodeDiffPanel
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

open class CheckovExtraInfoPanel(val result: BaseCheckovResult): JPanel() {

    fun initLayout() {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = UIUtil.getEditorPaneBackground()
    }

    fun createRightPanelDescriptionLine(text: String) {
        val descPanel = JPanel(BorderLayout())
        descPanel.background = UIUtil.getEditorPaneBackground()
        val textLabel = JLabel(text)
        textLabel.toolTipText = text
        descPanel.add(textLabel, BorderLayout.WEST)
        descPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        add(descPanel)
    }

    fun setDimensions(){
        if(result.fixDefinition == null) {
            add(Box.Filler(Dimension(0,0), Dimension(0, 0), Dimension(0, Short.MAX_VALUE.toInt())))
        }
    }

    fun addCodeDiffPanel(){
        if(result.fixDefinition != null){
            add(CodeDiffPanel(result, true))
        }
    }
}