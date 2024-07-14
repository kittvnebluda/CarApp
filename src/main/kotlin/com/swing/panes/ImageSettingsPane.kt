package com.swing.panes

import com.swing.GbcHelp.createGbc
import com.swing.ParamsManager
import com.swing.cfgDirPath
import com.swing.mqtt
import kotlinx.serialization.Serializable
import java.awt.Color
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.swing.*

class ImageSettingsPane : JPanel(GridBagLayout()) {
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
    private val adaptThreshTypes = mapOf(Pair("BINARY_INV", 1), Pair("BINARY", 0))

    private val imageSettingsPath: Path = Paths.get(cfgDirPath.toAbsolutePath().toString(), "image.json")

    @Serializable
    data class ImageParams(
        var enableImShow: Boolean,
        var enableImShowTopic: String,
        var adaptThreshMaxValue: Int,
        var adaptThreshMaxValueTopic: String,
        var adaptThreshMethod: String,
        var adaptThreshMethodTopic: String,
        var adaptThreshType: String,
        var adaptThreshTypeTopic: String,
        var adaptThreshBlockSize: Int,
        var adaptThreshBlockSizeTopic: String,
        var adaptThreshC: Int,
        var adaptThreshCTopic: String,
        var closingIterations: Int,
        var closingIterationsTopic: String)

    private val defaultParams = ImageParams(
        enableImShow = true,
        enableImShowTopic = "enable-imshow",
        adaptThreshMaxValue = 255,
        adaptThreshMaxValueTopic = "adapt-thresh/max-value",
        adaptThreshMethod = adaptThreshMethods.keys.first(),
        adaptThreshMethodTopic = "adapt-thresh/method",
        adaptThreshType = adaptThreshTypes.keys.first(),
        adaptThreshTypeTopic = "adapt-thresh/type",
        adaptThreshBlockSize = 191,
        adaptThreshBlockSizeTopic = "adapt-thresh/block-size",
        adaptThreshC = -70,
        adaptThreshCTopic = "adapt-thresh/c",
        closingIterations = 3,
        closingIterationsTopic = "closing/iterations")

    val paramsManager = ParamsManager(imageSettingsPath, defaultParams, ImageParams.serializer())

    private val imShowBtn = JButton()

    init {
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Image setup"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        )

        paramsManager.loadParams()

        // ImShow button
        if (paramsManager.params.enableImShow) {
            imShowBtn.text = "ON"
            imShowBtn.background = Color.GREEN
        } else {
            imShowBtn.text = "OFF"
            imShowBtn.background = Color.RED
            imShowBtn.foreground = Color.WHITE
        }

        val adaptThreshMaxValueSpinnerModel = SpinnerNumberModel(
            paramsManager.params.adaptThreshMaxValue,
            0,
            255,
            1)
        val adaptThreshMaxValueSpinner = JSpinner(adaptThreshMaxValueSpinnerModel)

        val adaptThreshMethodBox = JComboBox(adaptThreshMethods.keys.toTypedArray())
        adaptThreshMethodBox.model.selectedItem = paramsManager.params.adaptThreshMethod

        val adaptThreshTypeBox = JComboBox(adaptThreshTypes.keys.toTypedArray())
        adaptThreshTypeBox.model.selectedItem = paramsManager.params.adaptThreshType

        val adaptThreshBlockSizeSpinnerModel = SpinnerNumberModel(
            paramsManager.params.adaptThreshBlockSize,
            3,
            255,
            2)
        val adaptThreshBlockSizeSpinner = JSpinner(adaptThreshBlockSizeSpinnerModel)

        val adaptThreshCSpinnerModel = SpinnerNumberModel(
            paramsManager.params.adaptThreshC,
            -1000,
            1000,
            1)
        val adaptThreshCSpinner = JSpinner(adaptThreshCSpinnerModel)

        val closingIterationsSpinnerModel = SpinnerNumberModel(
            paramsManager.params.closingIterations,
            0,
            1000,
            1)
        val closingIterationsSpinner = JSpinner(closingIterationsSpinnerModel)

        val labelsFor = arrayOf(imShowBtn,
            adaptThreshMaxValueSpinner, adaptThreshMethodBox, adaptThreshTypeBox,
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

        imShowBtn.addActionListener {
            if(paramsManager.params.enableImShow) {
                mqtt.publish(paramsManager.params.enableImShowTopic, "0")
                imShowBtn.background = Color.RED
                imShowBtn.foreground = Color.WHITE
                imShowBtn.text = "OFF"
                paramsManager.params.enableImShow = false
            } else {
                mqtt.publish(paramsManager.params.enableImShowTopic, "1")
                imShowBtn.background = Color.GREEN
                imShowBtn.foreground = Color.BLACK
                imShowBtn.text = "ON"
                paramsManager.params.enableImShow = true
            }
        }

        adaptThreshMaxValueSpinner.addChangeListener {
            paramsManager.params.adaptThreshMaxValue = adaptThreshMaxValueSpinner.value as Int
            mqtt.publish(paramsManager.params.adaptThreshMaxValueTopic, paramsManager.params.adaptThreshMaxValue.toString())
        }

        adaptThreshMethodBox.addActionListener {
            paramsManager.params.adaptThreshMethod = adaptThreshMethodBox.selectedItem as String
            mqtt.publish(paramsManager.params.adaptThreshMethodTopic, adaptThreshMethods[paramsManager.params.adaptThreshMethod].toString())
        }

        adaptThreshTypeBox.addActionListener {
            paramsManager.params.adaptThreshType = adaptThreshTypeBox.selectedItem as String
            mqtt.publish(paramsManager.params.adaptThreshTypeTopic, adaptThreshTypes[paramsManager.params.adaptThreshType].toString())
        }

        adaptThreshBlockSizeSpinner.addChangeListener {
            paramsManager.params.adaptThreshBlockSize = adaptThreshBlockSizeSpinner.value as Int
            if(paramsManager.params.adaptThreshBlockSize % 2 == 0) {
                println("Block size can't be even")
                paramsManager.params.adaptThreshBlockSize += 1
                adaptThreshBlockSizeSpinner.value = paramsManager.params.adaptThreshBlockSize
            }
            mqtt.publish(paramsManager.params.adaptThreshBlockSizeTopic, paramsManager.params.adaptThreshBlockSize.toString())
        }

        adaptThreshCSpinner.addChangeListener {
            paramsManager.params.adaptThreshC = adaptThreshCSpinner.value as Int
            mqtt.publish(paramsManager.params.adaptThreshCTopic, paramsManager.params.adaptThreshC.toString())
        }

        closingIterationsSpinner.addChangeListener {
            paramsManager.params.closingIterations = closingIterationsSpinner.value as Int
            mqtt.publish(paramsManager.params.closingIterationsTopic, paramsManager.params.closingIterations.toString())
        }

        publishAllParams()
    }

    private fun publishAllParams() {
        mqtt.publish(paramsManager.params.enableImShowTopic, if (paramsManager.params.enableImShow) "1" else "0")
        mqtt.publish(paramsManager.params.adaptThreshMaxValueTopic, paramsManager.params.adaptThreshMaxValue.toString())
        mqtt.publish(paramsManager.params.adaptThreshMethodTopic, adaptThreshMethods[paramsManager.params.adaptThreshMethod].toString())
        mqtt.publish(paramsManager.params.adaptThreshTypeTopic, adaptThreshTypes[paramsManager.params.adaptThreshType].toString())
        mqtt.publish(paramsManager.params.adaptThreshBlockSizeTopic, paramsManager.params.adaptThreshBlockSize.toString())
        mqtt.publish(paramsManager.params.adaptThreshCTopic, paramsManager.params.adaptThreshC.toString())
        mqtt.publish(paramsManager.params.closingIterationsTopic, paramsManager.params.closingIterations.toString())
    }
}