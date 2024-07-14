package com.swing;

import com.swing.net.Mqtt;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.junit.jupiter.api.Test;

class MqttTest {
    @Test
    public void mqttTest() throws MqttException {
        Mqtt mqtt = new Mqtt("tcp://localhost:1883", "pid_publisher");

        mqtt.publish("/mqtt/test", "if you see this message, test is successful");
        mqtt.close();
    }
}