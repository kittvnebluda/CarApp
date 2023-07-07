package com.swing

import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*


class CarPane : JPanel(GridBagLayout()) {
    private val speedLabel = JLabel("Car speed:", JLabel.LEFT)

    private val carSpeedZero = 1500
    private val carSpeedMin = 1000
    private val carSpeedMax = 2000

    private var currentSpeed = carSpeedZero

    init {
        val speedSpinnerModel = SpinnerNumberModel(
            currentSpeed,
            carSpeedMin,
            carSpeedMax,
            1)
        val speedSpinner = JSpinner(speedSpinnerModel)

        val movementBtn = JButton("RUN")
        movementBtn.background = Color.GREEN
        movementBtn.foreground = Color.WHITE

        val c = GridBagConstraints()
        c.gridx = 0
        c.gridy = 0
        add(speedLabel)
        c.gridx = 1
        c.gridy = 0
        add(speedSpinner)
        c.gridx = 0
        c.gridy = 1
        c.gridwidth = 2
        add(movementBtn)

        speedSpinner.addChangeListener {
            currentSpeed = speedSpinner.value as Int
        }
    }
}