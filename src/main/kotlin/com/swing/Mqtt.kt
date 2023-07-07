package com.swing

import org.eclipse.paho.mqttv5.client.MqttClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage

class Mqtt @JvmOverloads constructor(
    broker: String,
    username: String,
    password: String,
    clientId: String,
    timeout: Int = 0,
    keepAlive: Int = 60
) {
    private val client: MqttClient

    init {
        client = MqttClient(broker, clientId, MemoryPersistence())
        val options = MqttConnectionOptions()
        options.userName = username
        options.password = password.toByteArray()
        options.connectionTimeout = timeout
        options.keepAliveInterval = keepAlive

        // connect
        client.connect(options)
    }

    constructor(
        broker: String,
        clientId: String
    ) : this(broker, "username", "password", clientId)

    @Throws(MqttException::class)
    fun publish(topic: String, message: String) {
        // create message and setup QoS
        val mqttMessage = MqttMessage(message.toByteArray())
        val qos = 0
        mqttMessage.qos = qos

        // publish message
        client.publish(topic, mqttMessage)
        println("Message published")
        println("Topic: $topic")
        println("Message content: $message")
    }

    @Throws(MqttException::class)
    fun close() {
        // disconnect
        client.disconnect()
        // close client
        client.close()
    }
}
