package com.swing

import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.util.*
import javax.swing.*


class PidPane : JPanel(GridBagLayout()) {
    enum class FieldTitle(val title: String) {
        P("Proportional"),
        I("Integral"),
        D("Derivative")
    }

    private val fieldMap: MutableMap<FieldTitle, JTextField> = EnumMap(FieldTitle::class.java)

    init {
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("PID"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        )

        val pidPane = JPanel(GridBagLayout())
        val publishButton = JButton("PUBLISH")

        var gbc: GridBagConstraints
        for (i in 0 until FieldTitle.values().size) {
            val fieldTitle = FieldTitle.values()[i]
            gbc = createGbc(0, i)
            pidPane.add(JLabel(fieldTitle.title + ":", JLabel.LEFT), gbc)
            gbc = createGbc(1, i)
            val textField = JTextField(5)
            pidPane.add(textField, gbc)
            fieldMap[fieldTitle] = textField
        }

        val c = GridBagConstraints()
        c.gridx = 0
        c.gridy = 0
        add(pidPane, c)

        c.gridx = 0
        c.gridy = 1
        c.insets = Insets(10, 10, 10, 10)
        add(publishButton, c)

        publishButton.addActionListener {
            try {
                val kp = this.fieldMap[FieldTitle.P]!!.text.toFloat()
                val ki = this.fieldMap[FieldTitle.I]!!.text.toFloat()
                val kd = this.fieldMap[FieldTitle.D]!!.text.toFloat()

                mqtt.publish("/car/pid", "$kp $ki $kd")
                publishButton.background = Color.GREEN
                publishButton.foreground = Color.BLACK

            } catch (e: Exception) {
                publishButton.background = Color.RED
                publishButton.foreground = Color.WHITE
                println(e.message)
            }
        }
    }
}