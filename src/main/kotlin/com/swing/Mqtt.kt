package com.swing

import org.eclipse.paho.mqttv5.client.*
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.packet.MqttProperties


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

        client.setCallback(object : MqttCallback {
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
        })

        // connect
        client.connect(options)
//        client.subscribe("car-interface/publish/all", 2)
    }

    constructor(
        broker: String,
        clientId: String
    ) : this(broker, "username", "password", clientId)

    @Throws(MqttException::class)
    fun publish(topic: String, message: String) {
        // create message and setup QoS
        val mqttMessage = MqttMessage(message.toByteArray())
        mqttMessage.qos = 0
        mqttMessage.isRetained = true

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
