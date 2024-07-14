package com.swing.panes

import com.swing.GbcHelp.createGbc
import com.swing.ParamsManager
import com.swing.cfgDirPath
import com.swing.mqtt
import kotlinx.serialization.Serializable
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.nio.file.*
import java.util.*
import javax.swing.*


class PidPane(
    pidName: String = "some"
) : JPanel(GridBagLayout()) {
    enum class FieldTitle(val title: String) {
        P("Proportional"),
        I("Integral"),
        D("Derivative")
    }

    private val publishButton = JButton("PUBLISH")

    private val fieldMap: MutableMap<FieldTitle, JTextField> = EnumMap(FieldTitle::class.java)
    private val pidPath: Path = Paths.get(cfgDirPath.toAbsolutePath().toString(), "${pidName}PID.json")

    @Serializable
    data class PidParams(
        var kp: Float,
        var ki: Float,
        var kd: Float,
        var topic: String)

    private val defaultParams = PidParams(0.5f, 0.0f, 0.0f, "pid/$pidName")

    val paramsManager = ParamsManager(pidPath, defaultParams, PidParams.serializer())

    init {
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("${pidName.run { first().uppercase() + substring(1) }} PID"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        )

        paramsManager.loadParams()

        val pidPane = JPanel(GridBagLayout())

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

        fieldMap[FieldTitle.P]!!.text = paramsManager.params.kp.toString()
        fieldMap[FieldTitle.I]!!.text = paramsManager.params.ki.toString()
        fieldMap[FieldTitle.D]!!.text = paramsManager.params.kd.toString()

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
                paramsManager.params.kp = fieldMap[FieldTitle.P]!!.text.toFloat()
                paramsManager.params.ki = fieldMap[FieldTitle.I]!!.text.toFloat()
                paramsManager.params.kd = fieldMap[FieldTitle.D]!!.text.toFloat()

                mqtt.publish(paramsManager.params.topic, "${paramsManager.params.kp} ${paramsManager.params.ki} ${paramsManager.params.kd}")
                publishButton.background = Color.GREEN
                publishButton.foreground = Color.BLACK

            } catch (e: Exception) {
                publishButton.background = Color.RED
                publishButton.foreground = Color.WHITE
                println(e.message)
            }
        }
        publishAllParams()
    }

    private fun publishAllParams() {
        mqtt.publish(paramsManager.params.topic, "${paramsManager.params.kp} ${paramsManager.params.ki} ${paramsManager.params.kd}")
        publishButton.background = Color.GREEN
        publishButton.foreground = Color.BLACK
    }
}