package com.swing

import com.swing.GbcHelp.createGbc
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.*
import java.nio.file.*
import java.util.*
import javax.swing.*


class CarMotionPane : JPanel(GridBagLayout()) {
    enum class FieldTitle(val title: String) {
        IsSpeedConstant("Constant speed"),
        Speed("Car speed")
    }

    private val carSpeedZero = 1500
    private val carSpeedMin = 1000
    private val carSpeedMax = 2000

    private val fieldMap: MutableMap<FieldTitle, Component> = EnumMap(FieldTitle::class.java)

    private val carMotionPath: Path = Paths.get(cfgDirPath.toAbsolutePath().toString(), "carMotion.json")

    @Serializable
    private data class CarMotionParams(var currentSpeed: Int,
                                       var stop: Boolean,
                                       var enableConstantSpeed: Boolean)

    private lateinit var params: CarMotionParams

    init {
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Car motion control"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        )

        loadParams()

        val speedSpinnerModel = SpinnerNumberModel(
            params.currentSpeed,
            carSpeedMin,
            carSpeedMax,
            1)
        val speedSpinner = JSpinner(speedSpinnerModel)

        val movementBtn = JButton()
        if (params.stop) {
            movementBtn.text = "RUN"
            movementBtn.background = Color.GREEN
        } else {
            movementBtn.text = "STOP"
            movementBtn.background = Color.RED
            movementBtn.foreground = Color.WHITE
        }

        val constantSpeedBtn = JButton()
        if (params.enableConstantSpeed) {
            constantSpeedBtn.text = "ON"
            constantSpeedBtn.background = Color.GREEN
        } else {
            constantSpeedBtn.text = "OFF"
            constantSpeedBtn.background = Color.RED
            constantSpeedBtn.foreground = Color.WHITE
            speedSpinner.isEnabled = false
        }

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
            params.currentSpeed = speedSpinner.value as Int
            mqtt.publish("car/speed", params.currentSpeed.toString())
        }

        constantSpeedBtn.addActionListener {
            if(params.enableConstantSpeed) {
                mqtt.publish("car/enable-const-speed", "0")
                constantSpeedBtn.background = Color.RED
                constantSpeedBtn.foreground = Color.WHITE
                constantSpeedBtn.text = "OFF"
                params.enableConstantSpeed = false
                speedSpinner.isEnabled = false
            } else {
                mqtt.publish("car/enable-const-speed", "1")
                constantSpeedBtn.background = Color.GREEN
                constantSpeedBtn.foreground = Color.BLACK
                constantSpeedBtn.text = "ON"
                params.enableConstantSpeed = true
                speedSpinner.isEnabled = true
            }
        }

        movementBtn.addActionListener {
            if(params.stop) {
                mqtt.publish("car/stop", "0")
                movementBtn.background = Color.RED
                movementBtn.foreground = Color.WHITE
                movementBtn.text = "STOP"
                params.stop = false
            } else {
                mqtt.publish("car/stop", "1")
                movementBtn.background = Color.GREEN
                movementBtn.foreground = Color.BLACK
                movementBtn.text = "RUN"
                params.stop = true
            }
        }

        publishAllParams()

//        // set hotkey
//        inputMap.put(KeyStroke.getKeyStroke("F2"), "Click")
//        actionMap.put("Click", object : AbstractAction() {
//            override fun actionPerformed(e: ActionEvent?) {
//                println("LOLOLO")//movementBtn.doClick()
//            }
//        })
    }

    private fun publishAllParams() {
        mqtt.publish("car/throttle", params.currentSpeed.toString())
        mqtt.publish("car/stop", if (params.stop) "1" else "0")
        mqtt.publish("car/enable-const-speed", if (params.enableConstantSpeed) "1" else "0")
    }

    private fun loadParams() {
        try {
            params = Json.decodeFromString(Files.readString(carMotionPath))
            println("Loaded params: $params")
        } catch (e: NoSuchFileException) {
            println("Config file not found: $carMotionPath")
            println("Creating new one with default parameters")
            saveParams(default = true)
        } catch (e: SerializationException) {
            println("Something went wrong while deserializing config file: $carMotionPath")
            println("Using default parameters")
            saveParams(default = true)
        }
    }

    fun saveParams(default: Boolean = false) {
        if (default) {
            params = CarMotionParams(
                currentSpeed = carSpeedZero,
                stop = true,
                enableConstantSpeed = false
            )
        }
        Files.writeString(carMotionPath, Json.encodeToString(params), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        println("Saved: $params")
    }
}