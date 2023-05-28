package com.bridgecrew.ui

import com.bridgecrew.results.BaseCheckovResult
import com.github.difflib.text.DiffRow
import com.github.difflib.text.DiffRowGenerator
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Dimension
import javax.swing.*
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

class CodeDiffPanel(val result: BaseCheckovResult, private val isErrorBubble: Boolean): JPanel() {

    private val LOG = logger<CodeDiffPanel>()
    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createEmptyBorder(0, 10, 0, 10)

        val fixHolder = JPanel()
        fixHolder.layout = BoxLayout(fixHolder, BoxLayout.Y_AXIS)
        val generator = DiffRowGenerator.create()
                .inlineDiffByWord(true)
                .build()

        var oldCode = buildCodeBlock()
        var newCode = buildFixLines()
        if(!isErrorBubble){
            newCode=buildFix()
        }
        val rows = generator.generateDiffRows(oldCode, newCode)
        val firstDiffRow = rows.find { it.tag != DiffRow.Tag.EQUAL &&
            it.newLine.trim().isNotEmpty() && it.newLine.trim().toDoubleOrNull() == null }
        updateFirstDiffLine(firstDiffRow)
        rows.filter { it.tag != DiffRow.Tag.EQUAL }.forEach { row ->
            if(row.oldLine.trim().isNotEmpty()){
                val vulBlock = createCodeBlock(row.oldLine.trim())
                vulBlock.background = Color.decode("#F5E6E7")
                fixHolder.add(vulBlock)
            }
            if(row.newLine.trim().isNotEmpty() && row.newLine.trim().toDoubleOrNull() == null){
                val fixBlock = createCodeBlock(row.newLine.trim())
                fixBlock.background = Color.decode("#E9F5E6")
                fixHolder.add(fixBlock)
            }
        }
        fixHolder.add(Box.createVerticalGlue())
        add(fixHolder)
    }

    private fun updateFirstDiffLine(diffRow: DiffRow?) {
        if (diffRow == null) {
            return
        }

        try {
            result.codeDiffFirstLine = diffRow.newLine.split(" ")[0].toInt()
        } catch (e: Exception) {
            LOG.debug("Could not update first diff line from new line \"${diffRow.newLine}\"", e)
        }
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
        textArea.maximumSize = Dimension(Int.MAX_VALUE, 5)
        return textArea
    }

    private fun buildCodeBlock(): ArrayList<String> {
        var codeBlock = arrayListOf<String>()
            result.codeBlock.forEach { block ->
                var currentLine = (block[0] as Double).toInt()
                val code = block[1]
                codeBlock += "$currentLine\t$code".replace("\n", "")
                currentLine++
            }

        return codeBlock
    }

    private fun buildFix(): ArrayList<String> {
        var fixWithRowNumber = arrayListOf<String>()
        if (result.codeBlock.isNotEmpty()) {
            var currentLine = (result.codeBlock[0][0] as Double).toInt()
            result.fixDefinition?.split("\n")?.forEach { codeRow ->
                fixWithRowNumber += "$currentLine\t$codeRow"
                currentLine++
            }
        }
        return fixWithRowNumber
    }

    private fun buildFixLines(): ArrayList<String> {
        var fixWithRowNumber = arrayListOf<String>()
        if (result.codeBlock.isNotEmpty()) {
            var currentLine = (result.codeBlock[0][0] as Double).toInt()
            result.fixDefinition?.split("\n")?.forEach { codeRow ->
                fixWithRowNumber += "$currentLine\t$codeRow".replace("\n", "")
                currentLine++
            }
        }
        return fixWithRowNumber
    }
}