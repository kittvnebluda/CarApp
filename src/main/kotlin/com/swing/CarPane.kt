package com.swing

import java.awt.*
import java.util.*
import javax.swing.*


class CarPane : JPanel(GridBagLayout()) {
    enum class FieldTitle(val title: String) {
        IsSpeedConstant("Constant speed"),
        Speed("Car speed")
    }

    private val carSpeedZero = 1500
    private val carSpeedMin = 1000
    private val carSpeedMax = 2000

    private val fieldMap: MutableMap<FieldTitle, Component> = EnumMap(FieldTitle::class.java)

    init {
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Car motion control"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        )

        var currentSpeed = carSpeedZero
        var stop = true
        var enableConstantSpeed = false

        val speedSpinnerModel = SpinnerNumberModel(
            currentSpeed,
            carSpeedMin,
            carSpeedMax,
            1)
        val speedSpinner = JSpinner(speedSpinnerModel)
        speedSpinner.isEnabled = false

        val movementBtn = JButton("RUN")
        movementBtn.background = Color.GREEN

        val constantSpeedBtn = JButton("OFF")
        constantSpeedBtn.background = Color.RED
        constantSpeedBtn.foreground = Color.WHITE

        val labelsFor = arrayOf(constantSpeedBtn, speedSpinner)
        val carPane = JPanel(GridBagLayout())

        var gbc: GridBagConstraints
        for (i in 0 until FieldTitle.values().size) {
            val fieldTitle = FieldTitle.values()[i]
            gbc = createGbc(0, i)
            carPane.add(JLabel(fieldTitle.title + ":", JLabel.LEFT), gbc)
            gbc = createGbc(1, i)
            val labelFor = labelsFor[i]
            carPane.add(labelFor, gbc)
            fieldMap[fieldTitle] = labelFor
        }

        val c = GridBagConstraints()
        c.gridx = 0
        c.gridy = 0
        add(carPane, c)

        c.gridx = 0
        c.gridy = 1
        c.insets = Insets(35, 10, 10, 10)
        add(movementBtn, c)

        speedSpinner.addChangeListener {
            currentSpeed = speedSpinner.value as Int
            mqtt.publish("/car/speed", currentSpeed.toString())
        }

        constantSpeedBtn.addActionListener {
            if(enableConstantSpeed) {
                mqtt.publish("/car/enable-const-speed", "0")
                constantSpeedBtn.background = Color.RED
                constantSpeedBtn.foreground = Color.WHITE
                constantSpeedBtn.text = "OFF"
                enableConstantSpeed = false
                speedSpinner.isEnabled = false
            } else {
                mqtt.publish("/car/enable-const-speed", "1")
                constantSpeedBtn.background = Color.GREEN
                constantSpeedBtn.foreground = Color.BLACK
                constantSpeedBtn.text = "ON"
                enableConstantSpeed = true
                speedSpinner.isEnabled = true
            }
        }

        movementBtn.addActionListener {
            if(stop) {
                mqtt.publish("/car/stop", "0")
                movementBtn.background = Color.RED
                movementBtn.foreground = Color.WHITE
                movementBtn.text = "STOP"
                stop = false
            } else {
                mqtt.publish("/car/stop", "1")
                movementBtn.background = Color.GREEN
                movementBtn.foreground = Color.BLACK
                movementBtn.text = "RUN"
                stop = true
            }
        }
    }
}