package com.swing.panes

import com.swing.GbcHelp.createGbc
import com.swing.ParamsManager
import com.swing.cfgDirPath
import com.swing.mqtt
import kotlinx.serialization.Serializable
import java.awt.*
import java.nio.file.*
import java.util.*
import javax.swing.*


class MotionPane : JPanel(GridBagLayout()) {
    @Serializable
    data class MotionParams(var speedZero: Int,
                            var speedMin: Int,
                            var speedMax: Int,
                            var speedCurr: Int,
                            var stop: Boolean,
                            var listenToSpeed: Boolean,
                            var topicSpeed: String,
                            var topicStop: String,
                            var topicPublishSpeed: String)

    enum class FieldTitle(val title: String) {
        PublishSpeed("Publish speed"),
        Speed("Car speed")
    }

    private val defaultParams = MotionParams(
            1500,
            1000,
            2000,
            1500,
            stop = true,
            listenToSpeed = false,
            topicSpeed = "speed",
            topicStop = "stop",
            topicPublishSpeed = "listen-to-published-speed",)

    private val fieldMap: MutableMap<FieldTitle, Component> = EnumMap(FieldTitle::class.java)
    private val configPath: Path = Paths.get(cfgDirPath.toAbsolutePath().toString(), "motion.json")

    private val stateBtn = JButton()
    private val listenToSpeedBtn = JButton()
    private var speedSpinner: JSpinner

    val paramsManager = ParamsManager(configPath, defaultParams, MotionParams.serializer())

    init {
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Motion control"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        )

        paramsManager.loadParams()

        // Speed spinner
        val speedSpinnerModel = SpinnerNumberModel(
            paramsManager.params.speedCurr,
            paramsManager.params.speedMin,
            paramsManager.params.speedMax,
            1)

        speedSpinner = JSpinner(speedSpinnerModel)
        speedSpinner.addChangeListener {
            paramsManager.params.speedCurr = speedSpinner.value as Int
            mqtt.publish(paramsManager.params.topicSpeed, paramsManager.params.speedCurr.toString())
        }
        mqtt.publish(paramsManager.params.topicSpeed, paramsManager.params.speedCurr.toString())

        // State button
        stateBtnAction(paramsManager.params.stop)
        stateBtn.addActionListener { stateBtnAction(!paramsManager.params.stop) }

        // Publish speed button
        listenToSpeedBtnAction(paramsManager.params.listenToSpeed)
        listenToSpeedBtn.addActionListener { listenToSpeedBtnAction(!paramsManager.params.listenToSpeed) }

        val labelsFor = arrayOf(listenToSpeedBtn, speedSpinner)
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
        add(stateBtn, c)
    }

    private fun stateBtnAction(stop: Boolean) {
        if(stop) {
            mqtt.publish(paramsManager.params.topicStop, "1")
            stateBtn.background = Color.RED
            stateBtn.foreground = Color.WHITE
            stateBtn.text = "STOPPED"
            paramsManager.params.stop = true
        } else {
            mqtt.publish(paramsManager.params.topicStop, "0")
            stateBtn.background = Color.GREEN
            stateBtn.foreground = Color.BLACK
            stateBtn.text = "RUNNING"
            paramsManager.params.stop = false
        }
    }

    private fun listenToSpeedBtnAction(listen: Boolean) {
        if(listen) {
            mqtt.publish(paramsManager.params.topicPublishSpeed, "1")
            listenToSpeedBtn.background = Color.GREEN
            listenToSpeedBtn.foreground = Color.BLACK
            listenToSpeedBtn.text = "ON"
            paramsManager.params.listenToSpeed = true
            speedSpinner.isEnabled = true
        } else {
            mqtt.publish(paramsManager.params.topicPublishSpeed, "0")
            listenToSpeedBtn.background = Color.RED
            listenToSpeedBtn.foreground = Color.WHITE
            listenToSpeedBtn.text = "OFF"
            paramsManager.params.listenToSpeed = false
            speedSpinner.isEnabled = false
        }
    }
}