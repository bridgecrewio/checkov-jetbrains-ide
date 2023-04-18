package com.bridgecrew.ui.buttons

import com.bridgecrew.ui.actions.SeverityFilterActions
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.UIManager
import javax.swing.plaf.basic.BasicButtonUI

class SeverityFilterButton(val project: Project,text: String): JButton(text) {

    init {
        addActionListener(SeverityFilterActions(project))
        preferredSize = Dimension(24, 24)
        border = null
        isOpaque = true
        ui = object: BasicButtonUI() {
            override fun paint(g: Graphics?, c: JComponent?) {
                val g2d = g?.create() as Graphics2D
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2d.color = c?.background
                c?.height?.let { g2d.fillRect(0,0,  c.width, it) }
                super.paint(g, c)
                g2d.dispose()
            }
        }
    }

    override fun updateUI() {
        super.updateUI()
        val isClicked = SeverityFilterActions.severityFilterState[text]
        val defaultBG = UIManager.getColor("Button.background")
        background = if(isClicked == true) JBColor.GRAY else defaultBG
    }

    override fun doClick() {
        super.doClick()
    }
}