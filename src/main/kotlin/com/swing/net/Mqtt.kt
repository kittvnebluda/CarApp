package com.swing.net

import org.eclipse.paho.mqttv5.client.*
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.packet.MqttProperties


class Mqtt @JvmOverloads constructor(
    broker: String,
    clientId: String,
    username: String = "username",
    password: String = "password",
    timeout: Int = 0,
    keepAlive: Int = 60
) {
    private var client: MqttClient = MqttClient(broker, clientId, MemoryPersistence())

    private val mqttCallback = object : MqttCallback {
        override fun disconnected(disconnectResponse: MqttDisconnectResponse?) {
            println("Disconnected: $disconnectResponse")
        }

        override fun mqttErrorOccurred(exception: MqttException?) {
            if (exception != null) {
                println("MQTT error occurred: ${exception.message}")
            }
        }

        override fun messageArrived(topic: String, message: MqttMessage) {
            println("Message arrived: ${message.payload}, from $topic topic")
        }

        override fun deliveryComplete(token: IMqttToken?) {}

        override fun connectComplete(reconnect: Boolean, serverURI: String?) {
            println("Connected to $serverURI")
        }

        override fun authPacketArrived(reasonCode: Int, properties: MqttProperties?) {}
    }

    init {
        val options = mqttConnectionOptions(username, password, timeout, keepAlive)
        client.setCallback(mqttCallback)
        client.connect(options)
    }

    private fun mqttConnectionOptions(
        username: String,
        password: String,
        timeout: Int,
        keepAlive: Int
    ): MqttConnectionOptions {
        val options = MqttConnectionOptions()
        options.userName = username
        options.password = password.toByteArray()
        options.connectionTimeout = timeout
        options.keepAliveInterval = keepAlive
        return options
    }

    @Throws(MqttException::class)
    fun publish(topic: String, message: String) {
        // create message and setup QoS
        val mqttMessage = MqttMessage(message.toByteArray())
        mqttMessage.qos = 0
        mqttMessage.isRetained = true

        // publish a message
        client.publish(topic, mqttMessage)
        println("Published message: \"$message\" to topic: \"$topic\"")
    }

    @Throws(MqttException::class)
    fun close() {
        client.disconnect()
        client.close()
    }
}
