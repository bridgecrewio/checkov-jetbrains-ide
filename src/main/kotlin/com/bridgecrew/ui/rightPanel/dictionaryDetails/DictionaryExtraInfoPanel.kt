package com.bridgecrew.ui.rightPanel.dictionaryDetails

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.utils.CheckovUtils
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

abstract class DictionaryExtraInfoPanel : JPanel() {

    // Field description (key) to field name from #BaseCheckovResult (value)
    abstract var fieldsMap: MutableMap<String, Any?>

    init {
        layout = GridBagLayout()
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    }

    fun addCustomPolicyGuidelinesIfNeeded(result: BaseCheckovResult){
        if(CheckovUtils.isCustomPolicy(result) && result.guideline != null){
            fieldsMap["Guidelines"] = "<html>${result.guideline}<html>"
        }
    }

    fun createDictionaryLayout(){
        val maxKeyWidth = fieldsMap.keys.maxByOrNull { it.length }?.let {
            getFontMetrics(font).stringWidth(it)
        } ?: 0

        val keyConstraints = GridBagConstraints().apply {
            weightx = 0.0
            anchor = GridBagConstraints.LINE_START
            insets = JBUI.insets(0, 0, 10, 15)
        }

        val valueConstraints = GridBagConstraints().apply {
            weightx = 1.0
            fill = GridBagConstraints.HORIZONTAL
            gridwidth = GridBagConstraints.REMAINDER
            insets = JBUI.insetsBottom(10)
        }

        val boldFont = Font(font.name, Font.BOLD, font.size)

        for ((key, value) in fieldsMap) {
            val isEmptyVal = (value == null || value.toString().isEmpty())
            if (key.lowercase() == "description" && isEmptyVal)
                continue
            val valueAsString = if (isEmptyVal) "---" else value.toString()
            val keyLabel = JLabel(key)
            keyLabel.font = boldFont
            keyLabel.preferredSize = Dimension(maxKeyWidth + 50, keyLabel.preferredSize.height)
            add(keyLabel, keyConstraints)
            add(JLabel(valueAsString), valueConstraints)
        }
    }
}