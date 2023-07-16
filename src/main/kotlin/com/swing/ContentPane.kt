package com.swing

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BorderFactory
import javax.swing.JPanel


class ContentPane : JPanel(GridBagLayout()) {
    val pidPane = PidPane()
    val imageSettingsPane = ImageSettingsPane()
    val carMotionPane = CarMotionPane()
    init {
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        val c = GridBagConstraints()
        c.gridx = 0
        c.gridy = 0
        c.fill = GridBagConstraints.VERTICAL
        add(imageSettingsPane, c)
        c.gridx = 1
        c.gridy = 0
        add(carMotionPane, c)
        c.gridx = 2
        c.gridy = 0
        add(pidPane, c)
    }
}