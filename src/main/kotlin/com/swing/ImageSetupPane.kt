package com.swing

import java.awt.Color
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.util.*
import javax.swing.*

class ImageSetupPane : JPanel(GridBagLayout()) {
    enum class FieldTitle(val title: String) {
        DebugImShow("Video output"),
        AdaptThreshMaxValue("Threshold max value"),
        AdaptThreshMethodLabel("Threshold method"),
        AdaptThreshType("Threshold type"),
        AdaptThreshBlockSize("Threshold block size"),
        AdaptThreshC("Threshold C"),
        ClosingIterations("Closing iterations")
    }

    private val fieldMap: MutableMap<FieldTitle, Component> = EnumMap(FieldTitle::class.java)

    private val adaptThreshMethods = mapOf(Pair("MEAN", 0), Pair("GAUSSIAN", 1))
    private val adaptThreshTypes = mapOf(Pair("BINARY_INV", 0), Pair("BINARY", 1))

    init {
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Image setup"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        )

        var enableImShow = true
        var adaptThreshMaxValue = 255
        var adaptThreshMethod = adaptThreshMethods.keys.first()
        var adaptThreshType= adaptThreshTypes.keys.first()
        var adaptThreshBlockSize = 191
        var adaptThreshC: Int = -70
        var closingIterations = 3

        val enableImShowBtn = JButton("ON")
        enableImShowBtn.background = Color.GREEN
        enableImShowBtn.foreground = Color.BLACK

        val adaptThreshMaxValueSpinnerModel = SpinnerNumberModel(
            adaptThreshMaxValue,
            0,
            255,
            1)
        val adaptThreshMaxValueSpinner = JSpinner(adaptThreshMaxValueSpinnerModel)

        val adaptThreshMethodBox = JComboBox(adaptThreshMethods.keys.toTypedArray())
        adaptThreshMethodBox.model.selectedItem = adaptThreshMethod

        val adaptThreshTypeBox = JComboBox(adaptThreshTypes.keys.toTypedArray())
        adaptThreshTypeBox.model.selectedItem = adaptThreshType

        val adaptThreshBlockSizeSpinnerModel = SpinnerNumberModel(
            adaptThreshBlockSize,
            3,
            255,
            2)
        val adaptThreshBlockSizeSpinner = JSpinner(adaptThreshBlockSizeSpinnerModel)

        val adaptThreshCSpinnerModel = SpinnerNumberModel(
            adaptThreshC,
            -1000,
            1000,
            1)
        val adaptThreshCSpinner = JSpinner(adaptThreshCSpinnerModel)

        val closingIterationsSpinnerModel = SpinnerNumberModel(
            closingIterations,
            0,
            1000,
            1)
        val closingIterationsSpinner = JSpinner(closingIterationsSpinnerModel)

        val labelsFor = arrayOf(enableImShowBtn, adaptThreshMaxValueSpinner, adaptThreshMethodBox, adaptThreshTypeBox,
            adaptThreshBlockSizeSpinner, adaptThreshCSpinner, closingIterationsSpinner)

        var gbc: GridBagConstraints
        for (i in 0 until FieldTitle.values().size) {
            val fieldTitle = FieldTitle.values()[i]
            gbc = createGbc(0, i)
            add(JLabel(fieldTitle.title + ":", JLabel.LEFT), gbc)
            gbc = createGbc(1, i)
            val fieldComp = labelsFor[i]
            add(fieldComp, gbc)
            fieldMap[fieldTitle] = fieldComp
        }

        enableImShowBtn.addActionListener {
            if(enableImShow) {
                mqtt.publish("/car/enable-imshow", "0")
                enableImShowBtn.background = Color.RED
                enableImShowBtn.foreground = Color.WHITE
                enableImShowBtn.text = "OFF"
                enableImShow = false
            } else {
                mqtt.publish("/car/enable-imshow", "1")
                enableImShowBtn.background = Color.GREEN
                enableImShowBtn.foreground = Color.BLACK
                enableImShowBtn.text = "ON"
                enableImShow = true
            }
        }

        adaptThreshMaxValueSpinner.addChangeListener {
            adaptThreshMaxValue = adaptThreshMaxValueSpinner.value as Int
            mqtt.publish("/car/adaptive-threshold/max-value", adaptThreshMaxValue.toString())
        }

        adaptThreshMethodBox.addActionListener {
            adaptThreshMethod = adaptThreshMethodBox.selectedItem as String
            mqtt.publish("/car/adaptive-threshold/method", adaptThreshMethods[adaptThreshMethod].toString())
        }

        adaptThreshTypeBox.addActionListener {
            adaptThreshType = adaptThreshTypeBox.selectedItem as String
            mqtt.publish("/car/adaptive-threshold/type", adaptThreshTypes[adaptThreshType].toString())
        }

        adaptThreshBlockSizeSpinner.addChangeListener {
            adaptThreshBlockSize = adaptThreshBlockSizeSpinner.value as Int
            if(adaptThreshBlockSize % 2 == 0) {
                println("Block size can't be even")
                adaptThreshBlockSize += 1
                adaptThreshBlockSizeSpinner.value = adaptThreshBlockSize
            }
            mqtt.publish("/car/adaptive-threshold/block-size", adaptThreshBlockSize.toString())
        }

        adaptThreshCSpinner.addChangeListener {
            adaptThreshC = adaptThreshCSpinner.value as Int
            mqtt.publish("/car/adaptive-threshold/C", adaptThreshC.toString())
        }

        closingIterationsSpinner.addChangeListener {
            closingIterations = closingIterationsSpinner.value as Int
            mqtt.publish("/car/closing/iterations", closingIterations.toString())
        }
    }
}