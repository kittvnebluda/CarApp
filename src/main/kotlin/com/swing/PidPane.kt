package com.swing

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField


class PidPane : JPanel() {
    enum class FieldTitle(val title: String) {
        P("Proportional"),
        I("Integral"),
        D("Derivative")
    }

    private val WEST_INSETS = Insets(5, 0, 5, 5)
    private val EAST_INSETS = Insets(5, 5, 5, 0)

    val fieldMap: MutableMap<FieldTitle, JTextField> = HashMap()

    init {
        layout = GridBagLayout()
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("PID publisher"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        )
        var gbc: GridBagConstraints?
        for (i in 0 until FieldTitle.values().size) {
            val fieldTitle = FieldTitle.values()[i]
            gbc = createGbc(0, i)
            add(JLabel(fieldTitle.title + ":", JLabel.LEFT), gbc)
            gbc = createGbc(1, i)
            val textField = JTextField(5)
            add(textField, gbc)
            fieldMap[fieldTitle] = textField
        }
    }

    private fun createGbc(x: Int, y: Int): GridBagConstraints {
        val gbc = GridBagConstraints()
        gbc.gridx = x
        gbc.gridy = y
        gbc.gridwidth = 1
        gbc.gridheight = 1
        gbc.anchor = if (x == 0) GridBagConstraints.WEST else GridBagConstraints.EAST
        gbc.fill = if (x == 0) GridBagConstraints.BOTH else GridBagConstraints.HORIZONTAL
        gbc.insets = if (x == 0) WEST_INSETS else EAST_INSETS
        gbc.weightx = if (x == 0) 0.1 else 1.0
        gbc.weighty = 1.0
        return gbc
    }
}