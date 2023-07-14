package com.swing

import com.swing.GbcHelp.createGbc
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.Color
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
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

    private val imageSettingsPath: Path = Paths.get(cfgDirPath.toAbsolutePath().toString(), "imageSettings.json")

    @Serializable
    private data class ImageParams(var enableImShow: Boolean,
                                   var adaptThreshMaxValue: Int,
                                   var adaptThreshMethod: String,
                                   var adaptThreshType: String,
                                   var adaptThreshBlockSize: Int,
                                   var adaptThreshC: Int,
                                   var closingIterations: Int)

    private lateinit var params: ImageParams

    init {
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Image setup"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        )

        loadParams()

        val enableImShowBtn = JButton()
        if (params.enableImShow) {
            enableImShowBtn.text = "ON"
            enableImShowBtn.background = Color.GREEN
        } else {
            enableImShowBtn.text = "OFF"
            enableImShowBtn.background = Color.RED
            enableImShowBtn.foreground = Color.WHITE
        }

        val adaptThreshMaxValueSpinnerModel = SpinnerNumberModel(
            params.adaptThreshMaxValue,
            0,
            255,
            1)
        val adaptThreshMaxValueSpinner = JSpinner(adaptThreshMaxValueSpinnerModel)

        val adaptThreshMethodBox = JComboBox(adaptThreshMethods.keys.toTypedArray())
        adaptThreshMethodBox.model.selectedItem = params.adaptThreshMethod

        val adaptThreshTypeBox = JComboBox(adaptThreshTypes.keys.toTypedArray())
        adaptThreshTypeBox.model.selectedItem = params.adaptThreshType

        val adaptThreshBlockSizeSpinnerModel = SpinnerNumberModel(
            params.adaptThreshBlockSize,
            3,
            255,
            2)
        val adaptThreshBlockSizeSpinner = JSpinner(adaptThreshBlockSizeSpinnerModel)

        val adaptThreshCSpinnerModel = SpinnerNumberModel(
            params.adaptThreshC,
            -1000,
            1000,
            1)
        val adaptThreshCSpinner = JSpinner(adaptThreshCSpinnerModel)

        val closingIterationsSpinnerModel = SpinnerNumberModel(
            params.closingIterations,
            0,
            1000,
            1)
        val closingIterationsSpinner = JSpinner(closingIterationsSpinnerModel)

        val labelsFor = arrayOf(enableImShowBtn,
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

        enableImShowBtn.addActionListener {
            if(params.enableImShow) {
                mqtt.publish("/car/enable-imshow", "0")
                enableImShowBtn.background = Color.RED
                enableImShowBtn.foreground = Color.WHITE
                enableImShowBtn.text = "OFF"
                params.enableImShow = false
            } else {
                mqtt.publish("/car/enable-imshow", "1")
                enableImShowBtn.background = Color.GREEN
                enableImShowBtn.foreground = Color.BLACK
                enableImShowBtn.text = "ON"
                params.enableImShow = true
            }
        }

        adaptThreshMaxValueSpinner.addChangeListener {
            params.adaptThreshMaxValue = adaptThreshMaxValueSpinner.value as Int
            mqtt.publish("/car/adaptive-threshold/max-value", params.adaptThreshMaxValue.toString())
        }

        adaptThreshMethodBox.addActionListener {
            params.adaptThreshMethod = adaptThreshMethodBox.selectedItem as String
            mqtt.publish("/car/adaptive-threshold/method", adaptThreshMethods[params.adaptThreshMethod].toString())
        }

        adaptThreshTypeBox.addActionListener {
            params.adaptThreshType = adaptThreshTypeBox.selectedItem as String
            mqtt.publish("/car/adaptive-threshold/type", adaptThreshTypes[params.adaptThreshType].toString())
        }

        adaptThreshBlockSizeSpinner.addChangeListener {
            params.adaptThreshBlockSize = adaptThreshBlockSizeSpinner.value as Int
            if(params.adaptThreshBlockSize % 2 == 0) {
                println("Block size can't be even")
                params.adaptThreshBlockSize += 1
                adaptThreshBlockSizeSpinner.value = params.adaptThreshBlockSize
            }
            mqtt.publish("/car/adaptive-threshold/block-size", params.adaptThreshBlockSize.toString())
        }

        adaptThreshCSpinner.addChangeListener {
            params.adaptThreshC = adaptThreshCSpinner.value as Int
            mqtt.publish("/car/adaptive-threshold/C", params.adaptThreshC.toString())
        }

        closingIterationsSpinner.addChangeListener {
            params.closingIterations = closingIterationsSpinner.value as Int
            mqtt.publish("/car/closing/iterations", params.closingIterations.toString())
        }

        publishAllParams()
    }

    private fun publishAllParams() {
        mqtt.publish("/car/enable-imshow", if (params.enableImShow) "1" else "0")
        mqtt.publish("/car/adaptive-threshold/max-value", params.adaptThreshMaxValue.toString())
        mqtt.publish("/car/adaptive-threshold/method", adaptThreshMethods[params.adaptThreshMethod].toString())
        mqtt.publish("/car/adaptive-threshold/type", adaptThreshTypes[params.adaptThreshType].toString())
        mqtt.publish("/car/adaptive-threshold/block-size", params.adaptThreshBlockSize.toString())
        mqtt.publish("/car/adaptive-threshold/C", params.adaptThreshC.toString())
        mqtt.publish("/car/closing/iterations", params.closingIterations.toString())
    }

    private fun loadParams() {
        try {
            params = Json.decodeFromString<ImageParams>(Files.readString(imageSettingsPath))
            println("Loaded params: $params")
        } catch (e: NoSuchFileException) {
            println("Config file not found: $imageSettingsPath")
            println("Creating new one with default parameters")
            saveParams(default = true)
        } catch (e: SerializationException) {
            println("Something went wrong while deserializing config file: $imageSettingsPath")
            println("Using default parameters")
            saveParams(default = true)
        }
    }

    fun saveParams(default: Boolean = false) {
        if (default) {
            params = ImageParams(
                enableImShow = true,
                adaptThreshMaxValue = 255,
                adaptThreshMethod = adaptThreshMethods.keys.first(),
                adaptThreshType = adaptThreshTypes.keys.first(),
                adaptThreshBlockSize = 191,
                adaptThreshC = -70,
                closingIterations = 3)
        }
        Files.writeString(imageSettingsPath, Json.encodeToString(params), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        println("Saved: $params")
    }
}