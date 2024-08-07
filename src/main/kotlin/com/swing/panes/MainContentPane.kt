package com.swing.panes

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BorderFactory
import javax.swing.JPanel


class MainContentPane : JPanel(GridBagLayout()) {
    val steeringPidPane = PidPane("steering")
    val throttlePidPane = PidPane("throttle")
    val imageSettingsPane = ImageSettingsPane()
    val motionPane = MotionPane()
    init {
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        val c = GridBagConstraints()
        c.gridx = 0
        c.gridy = 0
        c.fill = GridBagConstraints.VERTICAL
        add(imageSettingsPane, c)
        c.gridx = 1
        c.gridy = 0
        add(motionPane, c)
        c.gridx = 0
        c.gridy = 1
        c.fill = GridBagConstraints.BOTH
        add(steeringPidPane, c)
        c.gridx = 1
        c.gridy = 1
        c.fill = GridBagConstraints.BOTH
        add(throttlePidPane, c)
    }
}