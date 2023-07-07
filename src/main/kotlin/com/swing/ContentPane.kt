package com.swing

import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JButton
import javax.swing.JPanel


class ContentPane : JPanel(GridBagLayout()) {

    init {
        val pidPane = PidPane()
        val publishButton = JButton("PUBLISH")
        val imageSetupPane = ImageSetupPane()
        val carPane = CarPane()

        val c = GridBagConstraints()

        c.gridx = 0
        c.gridy = 0
        add(pidPane, c)

        c.gridx = 0
        c.gridy = 1
        c.insets = Insets(30, 10, 10, 10)
        add(publishButton, c)

        val mqtt = Mqtt("tcp://localhost:1883", "pid_publisher")

        publishButton.addActionListener {
            try {
                val kp = pidPane.fieldMap[PidPane.FieldTitle.P]!!.text.toFloat()
                val ki = pidPane.fieldMap[PidPane.FieldTitle.I]!!.text.toFloat()
                val kd = pidPane.fieldMap[PidPane.FieldTitle.D]!!.text.toFloat()

                mqtt.publish("/car/pid", "$kp $ki $kd")
                publishButton.background = Color.GREEN
                publishButton.foreground = Color.WHITE

            } catch (e: Exception) {
                publishButton.background = Color.RED
                publishButton.foreground = Color.WHITE
                println(e.message)
            }
        }
    }
}