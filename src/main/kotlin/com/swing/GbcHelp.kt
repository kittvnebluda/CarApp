package com.swing

import java.awt.GridBagConstraints
import java.awt.Insets

object GbcHelp {
    private val westInsets = Insets(5, 0, 5, 5)
    private val eastInsets = Insets(5, 5, 5, 0)

    fun createGbc(x: Int, y: Int): GridBagConstraints {
        val gbc = GridBagConstraints()
        gbc.gridx = x
        gbc.gridy = y
        gbc.gridwidth = 1
        gbc.gridheight = 1
        gbc.anchor = if (x == 0) GridBagConstraints.WEST else GridBagConstraints.EAST
        gbc.fill = if (x == 0) GridBagConstraints.BOTH else GridBagConstraints.HORIZONTAL
        gbc.insets = if (x == 0) westInsets else eastInsets
        gbc.weightx = if (x == 0) 0.1 else 1.0
        gbc.weighty = 1.0
        return gbc
    }
}