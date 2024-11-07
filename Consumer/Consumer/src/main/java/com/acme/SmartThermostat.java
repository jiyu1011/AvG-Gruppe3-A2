package com.acme;

import org.eclipse.paho.client.mqttv3.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartThermostat implements CommandLineRunner {
    private static final Logger logger = Logger.getLogger(SmartThermostat.class);

    @Value("${roomId}")
    private String roomId;

    public static void main(String[] args) {
        SpringApplication.run(SmartThermostat.class, args);
    }

    @Override
    public void run(String... args) {
        String thermostatTopic = "home/" + roomId + "/thermostat/set"; // 每个恒温器监听其房间的特定主题
        String broker = "tcp://localhost:1883";
        String clientId = "SmartThermostat-" + roomId;

        try {
            MqttClient client = new MqttClient(broker, clientId);
            client.connect();
            client.subscribe(thermostatTopic);
            logger.info("Smart thermostat for " + roomId + " subscribed to topic: " + thermostatTopic);

            client.setCallback(new MqttCallback() {
                public void connectionLost(Throwable cause) {
                    logger.warn("Smart thermostat connection lost", cause);
                }

                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String command = new String(message.getPayload());
                    if ("HEAT_ON".equals(command)) {
                        logger.info("Thermostat for " + roomId + ": Turning heating ON");
                    } else if ("HEAT_OFF".equals(command)) {
                        logger.info("Thermostat for " + roomId + ": Turning heating OFF");
                    } else {
                        logger.warn("Received unknown command for thermostat in room " + roomId + ": " + command);
                    }
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    // No action needed here for receiving messages
                }
            });

        } catch (Exception e) {
            logger.error("Smart thermostat for room " + roomId + " encountered an error", e);
        }
    }
}
