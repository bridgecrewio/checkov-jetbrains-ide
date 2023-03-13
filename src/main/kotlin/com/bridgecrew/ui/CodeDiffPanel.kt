package com.bridgecrew.ui

import com.bridgecrew.results.BaseCheckovResult
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Dimension
import javax.swing.*
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

class CodeDiffPanel(val result: BaseCheckovResult): JPanel() {

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createEmptyBorder(0, 10, 0, 10)

        val vulBlock = createCodeBlock(buildVulnerableLines())
        vulBlock.alignmentY = JTextArea.TOP_ALIGNMENT
        vulBlock.background = Color.decode("#F5E6E7")

        val fixBlock = createCodeBlock(buildFixLines())
        fixBlock.background = Color.decode("#E9F5E6")

        add(vulBlock)
        add(fixBlock)
        vulBlock.preferredSize = vulBlock.preferredSize
        preferredSize = Dimension(vulBlock.width, fixBlock.preferredSize.height + vulBlock.preferredSize.height)
    }

    private fun createCodeBlock(innerText: String): JTextPane {
        val textArea = JTextPane()
        textArea.text = innerText
        textArea.isEditable = false
        textArea.selectAll()
        val attributes = SimpleAttributeSet()
        StyleConstants.setLineSpacing(attributes, 0f)
        textArea.setParagraphAttributes(attributes, true)
        textArea.margin = JBUI.insets(10)
        return textArea
    }

    private fun buildVulnerableLines(): String {
        var vulnerableLines = ""
        result.codeBlock.forEachIndexed { index, block ->
            val rowNumber = (block[0] as Double).toInt().toString()
            val code = block[1]
            vulnerableLines += "$rowNumber\t$code"
        }
        return vulnerableLines.trim()
    }

    private fun buildFixLines(): String {
//        return result.fixDefinition
        val fixDefinition = "resource \"google_compute_subnetwork\" \"region_default_subnetwork\" {\n  #checkov:skip=CKV_GCP_26: \"Ensure that VPC Flow Logs is enabled for every subnet in a VPC Network\"\n  project                  = google_project.tenant_project.project_id\n  name                     = \"subnetwork-\${var.lcaas}-\${var.region}\"\n  ip_cidr_range            = \"10.181.0.0/20\"\n  region                   = var.region\n  network                  = google_compute_network.default_network.self_link\n  private_ip_google_access = true\n  private_ipv6_google_access = true\n}"
        var currentLine = (result.codeBlock[0][0] as Double).toInt()
        var fixWithRowNumber = ""
        fixDefinition.split("\n").forEach { codeRow ->
            fixWithRowNumber += "$currentLine\t$codeRow\n"
            currentLine++
        }
        return fixWithRowNumber
    }
}