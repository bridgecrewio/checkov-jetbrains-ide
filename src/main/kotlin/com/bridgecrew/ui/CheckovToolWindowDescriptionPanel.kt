package com.bridgecrew.ui
import com.intellij.openapi.application.ApplicationManager
import com.bridgecrew.CheckovResult
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import java.awt.*
import java.net.URI
import java.net.URISyntaxException
import javax.swing.*
import java.net.URL
import com.bridgecrew.utils.*
import com.intellij.openapi.project.Project


class CheckovToolWindowDescriptionPanel(val project: Project) : SimpleToolWindowPanel(true, true) {
    val descriptionPanel: JPanel = JPanel()
    var checkNameLabel: JPanel = JPanel()
    var checkIdLabel: JPanel = JPanel()
    var checkGuidelinesLabel: JPanel = JPanel()
    var fixButton: JButton = JButton()

    init {
        emptyDescription()
    }

    /**
     * Create display of description before scanning.
     */
    fun emptyDescription() {
        descriptionPanel.removeAll()
        descriptionPanel.add(JLabel("Scan your project"), BorderLayout.CENTER)
        descriptionPanel.revalidate()
    }

    /**
     * Create description for specific checkov result.
     */
    fun getDescription(checkovResult: CheckovResult) {
        descriptionPanel.removeAll()

        descriptionPanel.layout = GridLayoutManager(3, 2)
        fixButton = JButton("Fix")
        if (checkovResult.fixed_definition == null){
            fixButton.isEnabled = false
        }
        else {
            fixButton.isEnabled = true
            fixButton.addActionListener {
                ApplicationManager.getApplication().invokeLater {
                    val (start, end) = getOffsetByLines(checkovResult.file_line_range, project)
                    updateFile(checkovResult.fixed_definition, project, start, end)
                    fixButton.isEnabled = false
                }
            }
        }

        checkNameLabel = createDescriptionSection(CHECKNAME, checkovResult.check_name)
        checkIdLabel = createDescriptionSection(CHECKID, checkovResult.check_id)
        checkGuidelinesLabel = createDescriptionSection(GUIDELINES, checkovResult.guideline)

        descriptionPanel.add(checkNameLabel, createGridRowCol(0, 0, GridConstraints.ANCHOR_WEST));
        descriptionPanel.add(checkIdLabel, createGridRowCol(1, 0, GridConstraints.ANCHOR_WEST));
        descriptionPanel.add(checkGuidelinesLabel, createGridRowCol(2, 0, GridConstraints.ANCHOR_WEST));
        descriptionPanel.add(fixButton, createGridRowCol(0, 1, GridConstraints.ANCHOR_EAST));

        descriptionPanel.revalidate()
        descriptionPanel.repaint()

    }

    /**
     * Helper function that creates a single description section with a title and a description.
     */
    private fun createDescriptionSection(title: String, description: String): JPanel {
        val fontTitle = Font("Verdana", Font.BOLD, 15)

        val JSection = JPanel()

        val Jtitle = JTextPane()
        Jtitle.font = fontTitle
        Jtitle.text = title
        val JDescription: JLabel

        if (isUrl(description)) {
            JDescription = LinkLabel.create(description) {
                try {
                    Desktop.getDesktop().browse(URI(description))
                } catch (ex: URISyntaxException) {
                }
            }
        } else {
            JDescription = JLabel(description)
        }

        JSection.layout = GridLayoutManager(2, 1)
        JSection.add(Jtitle, createGridRowCol(0, 0, GridConstraints.ANCHOR_WEST))
        JSection.add(JDescription, createGridRowCol(1, 0, GridConstraints.ANCHOR_WEST))

        return JSection
    }

    /**
     * Helper function that validates url string.
     */
    private fun isUrl(url: String): Boolean {
        try {
            URL(url)
            return true
        } catch (e: Throwable) {
            return false
        }
    }

    fun createScroll(): JScrollPane {
        return ScrollPaneFactory.createScrollPane(
            descriptionPanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
    }

}