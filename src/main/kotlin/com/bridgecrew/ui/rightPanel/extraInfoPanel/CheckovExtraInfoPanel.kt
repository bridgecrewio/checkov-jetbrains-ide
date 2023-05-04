package com.bridgecrew.ui.rightPanel.extraInfoPanel

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.ui.CodeDiffPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.UIUtil
import java.awt.Dimension
import java.awt.Point
import javax.swing.*

open class CheckovExtraInfoPanel(val result: BaseCheckovResult): JPanel() {

    fun initLayout() {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = UIUtil.getEditorPaneBackground()
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
        if(result.fixDefinition == null) {
            add(Box.Filler(Dimension(0,0), Dimension(0, Short.MAX_VALUE.toInt()), Dimension(0, Short.MAX_VALUE.toInt())))
        }
        preferredSize = Dimension(this.width, 0)
    }

    fun addCodeDiffPanel(){
        if(result.fixDefinition != null){
            val scroll = JBScrollPane(CodeDiffPanel(result, true))
            scroll.preferredSize = Dimension(Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt())
            SwingUtilities.invokeLater(Runnable {
                scroll.viewport.viewPosition = Point(0,0)
            })
            add(scroll)
        }
    }
}