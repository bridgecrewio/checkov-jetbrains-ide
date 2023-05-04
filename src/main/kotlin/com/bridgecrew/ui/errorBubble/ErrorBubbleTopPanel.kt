package com.bridgecrew.ui.errorBubble

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.utils.getSeverityIconBySeverity
import com.intellij.icons.AllIcons
import com.intellij.ui.scale.JBUIScale
import java.awt.Component
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class ErrorBubbleTopPanel(val result: BaseCheckovResult, private val title: String, val index: Int, private val total: Int, private val callback: navigationCallback) : JPanel() {

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)

        add(buildTitleAndIcon())
        add(Box.createHorizontalGlue())
        if (total > 1) {
            add(buildNavigation())
        }

        border = BorderFactory.createEmptyBorder(JBUIScale.scale(10), JBUIScale.scale(10), JBUIScale.scale(10), JBUIScale.scale(10))
        preferredSize = Dimension(ErrorBubbleInnerPanel.PANEL_WIDTH, this.preferredSize.height)
        minimumSize = preferredSize
        maximumSize = preferredSize
    }

    private fun buildTitleAndIcon(): JPanel {
        val iconLabel = JLabel(getSeverityIconBySeverity(result.severity))
        val titleLabel = JLabel(title)
        titleLabel.preferredSize = Dimension(ErrorBubbleInnerPanel.MAX_TITLE_TEXT_WIDTH, titleLabel.preferredSize.height)
        titleLabel.toolTipText = title
        val leftPanel = JPanel()

        leftPanel.layout = BoxLayout(leftPanel, BoxLayout.X_AXIS)
        leftPanel.add(iconLabel)
        leftPanel.add(Box.createRigidArea(Dimension(5, 0)))
        leftPanel.add(titleLabel)
        return leftPanel
    }

    private fun buildNavigation(): JPanel {
        val rightPanel = JPanel()
        rightPanel.layout = BoxLayout(rightPanel, BoxLayout.X_AXIS)
        addLeftArrow(rightPanel)
        rightPanel.add(JLabel("${index + 1}/$total"))
        addRightArrow(rightPanel)
        rightPanel.border = BorderFactory.createEmptyBorder(0, 10, 0, 0)
        rightPanel.alignmentX = Component.RIGHT_ALIGNMENT
        return rightPanel
    }

    private fun addLeftArrow(rightPanel: JPanel) {
        if (index > 0) {
            val leftArrowLabel = JLabel(AllIcons.General.ArrowLeft)
            leftArrowLabel.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    super.mouseClicked(e)
                    callback(index, "left")
                }
            })
            rightPanel.add(leftArrowLabel)
        }
    }

    private fun addRightArrow(rightPanel: JPanel) {
        if (index < total - 1) {
            val rightArrowLabel = JLabel(AllIcons.General.ArrowRight)
            rightArrowLabel.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    super.mouseClicked(e)
                    callback(index, "right")
                }
            })
            rightPanel.add(rightArrowLabel)
        }
    }
}