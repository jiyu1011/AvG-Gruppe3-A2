package com.acme;

import org.eclipse.paho.client.mqttv3.*;
import org.apache.log4j.Logger;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeControlSystem {
    private static final Logger logger = Logger.getLogger(HomeControlSystem.class);
    private static final double THRESHOLD_LOW = 18.0;
    private static final double THRESHOLD_HIGH = 22.0;

    public static void main(String[] args) {
        String temperatureTopic = "home/temperature_control";
        String broker = "tcp://localhost:1883";
        String clientId = "HomeControlSystem";

        try {
            MqttClient client = new MqttClient(broker, clientId);
            client.connect();
            client.subscribe(temperatureTopic);
            logger.info("Home control system subscribed to topic: " + temperatureTopic);

            client.setCallback(new MqttCallback() {
                public void connectionLost(Throwable cause) {
                    logger.warn("Home control system connection lost", cause);
                }

                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String receivedMessage = new String(message.getPayload());
                    logger.info("Received message: " + receivedMessage);

                    // 使用正则表达式解析 roomId 和 temperature
                    Pattern pattern = Pattern.compile("\\\"roomId\\\":\\\"(\\w+)\\\",\\s*\\\"temperature\\\":([0-9]+\\.?[0-9]*)");
                    Matcher matcher = pattern.matcher(receivedMessage);
                    if (matcher.find()) {
                        String roomId = matcher.group(1);
                        double temperature = Double.parseDouble(matcher.group(2));

                        // Log datei
                        try (FileWriter writer = new FileWriter("temperature.log", true)) {
                            // roomId 和 temperature 变量
                            String logEntry = "RoomID: " + roomId + ", Temperature: " + temperature;
                            writer.write(logEntry + "\n");
                            logger.debug("Logged roomID and temperature to file: " + logEntry);
                        } catch (IOException e) {
                            logger.error("Error writing roomID and temperature to log file", e);
                        }

                        String thermostatTopic = "home/" + roomId + "/thermostat/set";
                        if (temperature < THRESHOLD_LOW) {
                            sendCommand(client, thermostatTopic, "HEAT_ON");
                        } else if (temperature > THRESHOLD_HIGH) {
                            sendCommand(client, thermostatTopic, "HEAT_OFF");
                        }
                    } else {
                        logger.error("Invalid message format: " + receivedMessage);
                    }
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

        } catch (Exception e) {
            logger.error("Home control system encountered an error", e);
        }
    }

    private static void sendCommand(MqttClient client, String topic, String command) {
        try {
            MqttMessage message = new MqttMessage(command.getBytes());
            message.setQos(1);
            client.publish(topic, message);
            logger.info("Sent command to thermostat: " + command + " on topic " + topic);
        } catch (MqttException e) {
            logger.error("Failed to send command", e);
        }
    }
}
