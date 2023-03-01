package com.bridgecrew.ui.rightPanel.extraInfoPanel

import java.awt.Dimension
import javax.swing.*

open class CheckovExtraInfoPanel: JPanel() {

    fun initLayout() {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }

    fun createRightPanelDescriptionLine(text: String) {
        val textArea = JTextArea(text)
        textArea.lineWrap = true
        textArea.wrapStyleWord = true
        textArea.isOpaque = false
        textArea.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        add(textArea)
    }

    fun setDimensions(){
        add(Box.Filler(Dimension(0,0), Dimension(0, Short.MAX_VALUE.toInt()), Dimension(0, Short.MAX_VALUE.toInt())))
        preferredSize = Dimension(this.width, 0)
    }
}