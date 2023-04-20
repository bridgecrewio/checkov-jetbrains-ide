package com.bridgecrew.ui

import com.bridgecrew.results.BaseCheckovResult
import com.github.difflib.text.DiffRow
import com.github.difflib.text.DiffRowGenerator
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

        val fixHolder = JPanel()
        fixHolder.layout = BoxLayout(fixHolder, BoxLayout.Y_AXIS)
        val generator = DiffRowGenerator.create()
                .inlineDiffByWord(true)
                .build()

        val rows = generator.generateDiffRows(buildVulnerableLines(), buildFixLines())
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

    private fun buildVulnerableLines(): ArrayList<String> {
        var vulnerableLines = arrayListOf<String>()
        result.codeBlock.forEachIndexed { index, block ->
            val rowNumber = (block[0] as Double).toInt().toString()
            val code = block[1]
            vulnerableLines += "$rowNumber\t$code".replace("\n","")
        }
        return vulnerableLines
    }

    private fun buildFixLines(): ArrayList<String> {
        var currentLine = (result.codeBlock[0][0] as Double).toInt()
        var fixWithRowNumber = arrayListOf<String>()
        result.fixDefinition?.split("\n")?.forEach { codeRow ->
            fixWithRowNumber += "$currentLine\t$codeRow".replace("\n","")
            currentLine++
        }
        return fixWithRowNumber
    }
}