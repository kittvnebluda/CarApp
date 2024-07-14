package com.swing.windows

import com.swing.ParamsManager
import com.swing.cfgDirPath
import com.swing.clientId
import com.swing.mqtt
import com.swing.net.Mqtt
import kotlinx.serialization.Serializable
import org.eclipse.paho.mqttv5.common.MqttException
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.*
import javax.swing.border.EmptyBorder

object LoginWindow : JFrame() {
    private const val DEFAULT_BROKER = "tcp://localhost:1883"

    private val brokerField = JTextField(20)
    private val connectButton = JButton("Start")

    @Serializable
    data class LoginParams(
        var broker: String
    )

    private val configPath: Path = Paths.get(cfgDirPath.toAbsolutePath().toString(), "login.json")
    private val defaultParams = LoginParams(DEFAULT_BROKER)
    private val paramsManager = ParamsManager(configPath, defaultParams, LoginParams.serializer())

    init {
        setIconImage()
        paramsManager.loadParams()

        title = "Start Window"
        defaultCloseOperation = EXIT_ON_CLOSE

        brokerField.text = paramsManager.params.broker

        // Main panel with padding
        val mainPanel = JPanel()
        mainPanel.layout = BorderLayout(10, 10) // Padding between components
        mainPanel.border = EmptyBorder(10, 10, 10, 10) // Padding around the panel

        val inputPanel = JPanel()
        inputPanel.layout = GridLayout(2, 2, 5, 5) // Padding between rows
        inputPanel.add(JLabel("Broker Address:"))
        inputPanel.add(brokerField)
        mainPanel.add(inputPanel, BorderLayout.CENTER)

        // Button panel
        val buttonPanel = JPanel()
        buttonPanel.layout = FlowLayout(FlowLayout.CENTER)
        buttonPanel.add(connectButton)
        mainPanel.add(buttonPanel, BorderLayout.SOUTH)

        connectButton.addActionListener {
            if (connectToBroker(brokerField.text)) {
                paramsManager.params.broker = brokerField.text
                paramsManager.saveParams()
                isVisible = false
                MainWindow
            }
        }

        contentPane.add(mainPanel, BorderLayout.CENTER)

        pack() // установка размеров фрейма
        setLocationRelativeTo(null)  // center the window
        isResizable = false
        isVisible = true
    }

    private fun connectToBroker(broker: String): Boolean {
        return try {
            mqtt = Mqtt(broker, clientId)
            true
        } catch (e: MqttException) {
            JOptionPane.showMessageDialog(
                LoginWindow,
                "Failed to connect to broker.\nPlease check the address or your broker.",
                "MQTT Error",
                JOptionPane.ERROR_MESSAGE)
            false
        } catch (e: IllegalArgumentException) {
            JOptionPane.showMessageDialog(
                LoginWindow,
                "Broker address is not valid.\nLook at the example address: $DEFAULT_BROKER",
                "Illegal Argument Error",
                JOptionPane.ERROR_MESSAGE)
            false
        }
    }
    private fun setIconImage() {
        val iconURL = this::class.java.getResource("/aqua.png")
        if (iconURL != null) {
            val icon = ImageIcon(iconURL)
            iconImage = icon.image
        } else {
            println("Icon image not found.")
        }
    }
}