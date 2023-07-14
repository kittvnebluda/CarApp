package com.swing

import com.swing.GbcHelp.createGbc
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.nio.file.*
import java.util.*
import javax.swing.*


class PidPane : JPanel(GridBagLayout()) {
    enum class FieldTitle(val title: String) {
        P("Proportional"),
        I("Integral"),
        D("Derivative")
    }

    private val publishButton = JButton("PUBLISH")

    private val fieldMap: MutableMap<FieldTitle, JTextField> = EnumMap(FieldTitle::class.java)
    private val pidPath: Path = Paths.get(cfgDirPath.toAbsolutePath().toString(), "pid.json")
    @Serializable
    private data class PidParams(var throttleKp: Float,
                                 var throttleKi: Float,
                                 var throttleKd: Float)

    private lateinit var params: PidParams

    init {
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("PID"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        )

        loadParams()

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

        fieldMap[FieldTitle.P]!!.text = params.throttleKp.toString()
        fieldMap[FieldTitle.I]!!.text = params.throttleKi.toString()
        fieldMap[FieldTitle.D]!!.text = params.throttleKd.toString()

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
                params.throttleKp = fieldMap[FieldTitle.P]!!.text.toFloat()
                params.throttleKi = fieldMap[FieldTitle.I]!!.text.toFloat()
                params.throttleKd = fieldMap[FieldTitle.D]!!.text.toFloat()

                mqtt.publish("car/throttle/pid", "${params.throttleKp} ${params.throttleKi} ${params.throttleKd}")
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
        mqtt.publish("car/throttle/pid", "${params.throttleKp} ${params.throttleKi} ${params.throttleKd}")
        publishButton.background = Color.GREEN
        publishButton.foreground = Color.BLACK
    }

    private fun loadParams() {
        try {
            params = Json.decodeFromString(Files.readString(pidPath))
            println("Loaded params: $params")
        } catch (e: NoSuchFileException) {
            println("Config file not found: $pidPath")
            println("Creating new one with default parameters")
            saveParams(default = true)
        } catch (e: SerializationException) {
            println("Something went wrong while deserializing config file: $pidPath")
            println("Using default parameters")
            saveParams(default = true)
        }
    }

    fun saveParams(default: Boolean = false) {
        if (default) {
            params = PidParams(
                throttleKp = 0.5F,
                throttleKi = 0.0F,
                throttleKd = 0.0F
            )
        }
        Files.writeString(pidPath, Json.encodeToString(params), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        println("Saved: $params")
    }
}